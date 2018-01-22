/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ilovedigitalmeister;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import me.ilovedigitalmeister.data.ProductNameInfo;
import me.ilovedigitalmeister.data.ProductNameInfoFactory;

/**
 *
 * @author kazuyuf
 */
public class AutoCompleteServlet extends HttpServlet {
    
    private ServletContext context;
    public static final int DEFAULT_CACHE_PERIOD_H = 48;
    private static final int MAX_DISP_CANDIDATE_NUM_ITEM = 10; // Max display item number
    private static final int MAX_DISP_CACHE_NUM_ITEM = 100; // Max display item number
    //private static final int MAX_DISP_CACHE_NUM_ITEM = 3000; // Max display item number [FINAL]
    private static boolean debug = false;
    
    private static final Logger logger = Logger.getLogger(AutoCompleteServlet.class.getName());

    // Cache for this Servlet
    // This cache will be updated regulary (Eg. every 24 hours);
    
    /**
     * Cache is with "Id" as a key, and "name" as a value. It is the key of the product and its name value.
     */
    private HashMap<String, ProductNameInfo> _cache;
    private int _cachePeriodHours; //default cache period
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        this.context = config.getServletContext();
        
        // Initiate the cache refresh period here, if needed.
        _cachePeriodHours = DEFAULT_CACHE_PERIOD_H; //default cache period
        if(createCache()) {
            logger.log(Level.INFO, "Cache is created now.");
        } else {
            logger.log(Level.INFO, "No cache is created now.");            
        }

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        // targetId is actuly a key that should match with the searching name in the first few letters.
        String targetId = request.getParameter("id");
        StringBuilder sb = new StringBuilder();

        
        if (action != null && action.equalsIgnoreCase("debug")) {
            showCurrentCache(request, response);
            logger.log(Level.INFO, "Cache to be displayed. For operation purpose.");

        } else if (action != null && action.equalsIgnoreCase("demo")) {
            debug = true;
            logger.log(Level.INFO, "Cache to be with samples.");
            if(refreshCache()) {
                logger.log(Level.INFO, "Cache is created now.");
            }
            context.getRequestDispatcher("/search.jsp").forward(request, response);
        } else if (action != null && action.equalsIgnoreCase("refresh")) {
            if(refreshCache()) {
                logger.log(Level.INFO, "Cache is created now.");
            }
        } else {
            if (targetId != null) {
                // doAutoCompleteGet
                doAutoCompleteGet(targetId.toLowerCase(), request, response);
            } else if(action == null ) {
                context.getRequestDispatcher("/error.jsp").forward(request, response);
            }                    
        }
    }

    
    private void doAutoCompleteGet(String key, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        boolean namesAdded = false;

        StringBuilder sb = null;
                
        HashMap products = getCache();

        // check if user sent empty string
        if (!key.equals("")) {
            logger.log(Level.INFO, "Searching for word starting with [{0}].", key);
            
            sb = getFromStorageCache(key);
            if (sb == null){ // No cache stored
                sb = new StringBuilder();
                Iterator it = products.keySet().iterator();
                int i = 0;
                while (it.hasNext()) {
                    String id = (String) it.next();
                    ProductNameInfo product = (ProductNameInfo) products.get(id);
                    String name = product.getName();

                    logger.log(Level.INFO, "Product Id: {0}", id);
                    logger.log(Level.INFO, "Product Name: {0}", name);

                    if( name != null ) {
                        //trim product name and convert to lower case for compare
                        name = name.toLowerCase();
                        if ( // targetId matches name
                            name.startsWith(key)) {
                            sb.append("<product>");
                            sb.append("<id>").append(product.getId()).append("</id>");
                            sb.append("<name>").append(product.getName()).append("</name>");
                            sb.append("</product>");
                            namesAdded = true;
                            i++;
                        }                    
                        /**
                         * If there are too many entries, we should limit to few entries only
                         * This means we should introduce an algorism to only show that is the next possible letter.
                         * 
                         * For now, just will break after 30 entries
                         */
                        if(i>MAX_DISP_CANDIDATE_NUM_ITEM-1) {
                            logger.log(Level.INFO, "Too many entries ... skipping search with limit to {0}.", MAX_DISP_CANDIDATE_NUM_ITEM);                    
                            break;
                        }

                    } else {
                        logger.log(Level.INFO, "Skip this one since the name is empty.");                    
                    }
                }
                logger.log(Level.INFO, "{0} entries found in the cache.",products.size());
                logger.log(Level.INFO, " {0} entries actualy match with the keyword.",i);
            } else {
                if(sb.length()>0) { //If the XML corresponding to the keyword was already stored in the storage
                    namesAdded= true;
                    logger.log(Level.INFO, "Cache stored in the storage found for reuse. {0}", sb.toString());
                }
            }
        }
        if (namesAdded && (sb != null)) {
            response.setContentType("text/xml");
            response.setHeader("Cache-Control", "no-cache");
            response.getWriter().write("<products>" + sb.toString() + "</products>");
            
            // Save into the Storage Cache so next time it can be used without recreating the data
            putIntoStorageCache(key, sb.toString());

        } else {
            //nothing to show
            logger.log(Level.INFO, "Could not find any entry for the given word.",products.size());
            context.getRequestDispatcher("/error.jsp").forward(request, response);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }
    
    /**
     * 
     * @return true if cache has been updated, otherwise false 
     */
    private boolean refreshCache() {
        boolean retVal;
        // for now, no process to update the cache is to be implemented.
        // Idea is to have a timer to refresh the cacha from the database regulary
        // as defined by the refresh period.
        logger.log(Level.INFO, "Cache refreshed requested ....");
        retVal = createCache();

        return retVal;
    }
    
    private HashMap getCache() {
        return _cache;
    }
    
    private boolean createCache() {
        boolean retVal = false;
        
        /**
         * Here to implement Cache creation process to get data from the database
         */
        
        //HashMap<String, ProductNameInfo> _cache = new HashMap();
        // with _cache
        if(debug) {
            _cache = new ProductNameInfoFactory(debug).getProducts();            
        } else {
            _cache = new ProductNameInfoFactory().getProducts();            
        }
        if(_cache.size()>1) {
            retVal = true;
        }
        logger.log(Level.INFO, "{0} entries created for the cache.",_cache.size());
        
        return retVal;
    }
    
        /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Provide product name information for Autocomplete and the details. ";
    }// </editor-fold>

    private void showCurrentCache(HttpServletRequest request, HttpServletResponse response) {
        StringBuilder sb = new StringBuilder();
                
        HashMap products = getCache();
            
        Iterator it = products.keySet().iterator();
        int cnt = 0;
        int i = 0;
        while (it.hasNext()) {
            String id = (String) it.next();
            ProductNameInfo product = (ProductNameInfo) products.get(id);
            String name = product.getName();

            if( name != null ) {
                sb.append("ID:").append(product.getId()).append("</br>");
                sb.append("NAME:").append(product.getName()).append("</br>");
                i++;

                /**
                 * If there are too many entries, we should limit to few entries only
                 * This means we should introduce an algorism to only show that is the next possible letter.
                 * 
                 * For now, just will break after 30 entries
                 */
                if(i>MAX_DISP_CACHE_NUM_ITEM-1) {
                    logger.log(Level.INFO, "Too many entries ... skipping search with limit to {0}.", MAX_DISP_CANDIDATE_NUM_ITEM);                    
                    break;
                }

            } else {
                logger.log(Level.INFO, "Skip this one since the name is empty.");                    
            }
        }
        logger.log(Level.INFO, "{0} entries found in the cache.",products.size());
        logger.log(Level.INFO, " {0} entries actualy is valid.",i);


            
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            
            out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"stylesheet.css\">");

            out.println("<title>Cache List</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Cached Proudct List</h1>");
            
            /*
             * Response Body here
             */
            out.println("<a href=\"/\">Go back</a></br></hr>");
            
            out.println(sb.toString());
            
            out.println("</br><a href=\"/\">Go back</a>");
            out.println("</body>");
            out.println("</html>");
                        
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private StringBuilder getFromStorageCache(String key) {
        StringBuilder sb = new StringBuilder();
        /**
         * Here we should consider searching on the storage cache XML data created before.
         * Simply we should create a storage cache with the given word (String key) as KEY to retrieve the XML
         * Data should be stored as an XML file
         * <cache>
         * <cachekey>
         *  HERE COMES THE word (ex. "a", "ab", "ac"...)
         * </cachekey>
         * <cachedProducts>
         *  <product>
         *   <id>HERE COMES THE PRODUCT ID</id>
         *   <name>HERE COMES THE PRODUCT NAME</name>
         *  <product>
         * </cachedProducts>
         * </cache>
         */
        // NOT YET USING THE STORAGE TO CACHE THE XML FILES, HENCE, RETURN NULL FOR NOW.
        return null;
    }

    private void putIntoStorageCache(String key, String cache) {
        /**
         * Here store the xml file for future reference without creating the product name information associated with the key
         */
        logger.log(Level.INFO, "key:{0}", key);
        logger.log(Level.INFO, "XML Cache:{0}",cache);

    }

    
}

