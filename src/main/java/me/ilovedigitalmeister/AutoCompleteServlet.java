/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ilovedigitalmeister;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import me.ilovedigitalmeister.data.ProductNameInfoFactory;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Blob.BlobSourceOption;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import static java.nio.charset.StandardCharsets.UTF_8;


/**
 *
 * @author kazuyuf
 */
public class AutoCompleteServlet extends HttpServlet {
    
    private ServletContext context;
    public static final int DEFAULT_CACHE_PERIOD_H = 48;
    private static final int MAX_DISP_CANDIDATE_NUM_ITEM = 25; // Max display item number
    private static final int MAX_DISP_CACHE_NUM_ITEM = 200; // Max display item number
    private static final String FILE_EXTENSTION_NAME = ".xml";
    private static final String BUCKET_UNIQUE_NAME = "autocomplete_xml_cache";
    private static final int TRIE_TREE_PRE_FETCH_DEPTH = 3; // To process pre-fetch on plus that depth
    
    //private static final int MAX_DISP_CACHE_NUM_ITEM = 3000; // Max display item number [FINAL]
    private static boolean debug = false;
    
    private static final Logger logger = Logger.getLogger(AutoCompleteServlet.class.getName());
    final String bucketName = BUCKET_UNIQUE_NAME; // Bucket name

    // Cache for this Servlet
    // This cache will be updated regulary (Eg. every 24 hours);
    
    /**
     * Cache is with "Id" as a key, and "name" as a value. It is the key of the product and its name value.
     */
//    private HashMap<String, ProductNameInfo> _cache;
    private int _cachePeriodHours;
    /**
     * Index to all xml file name on Google Storage
     */ //default cache period

    /**
     * This is to be implemented as GAE Memcache so the Batch App which is triggered by the Task Queue can update the XML Index.
     * 
     */
    private final HashMap<String, String> _storageXMLCacheIndex;;

    public AutoCompleteServlet() {
        this._storageXMLCacheIndex = new HashMap();

        // Initiate the cache refresh period here, if needed.
        _cachePeriodHours = DEFAULT_CACHE_PERIOD_H; //default cache period
    }
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        this.context = config.getServletContext();
                
        // SHOULD BE CONSIDER OTHER TIMING TO CREATE THE STORAGE CACHE
        // target demo key (Memo: AAXA - P*, Backbeat Books)
        refreshStorageCache();
        
    }
    
    private void refreshStorageCache() {
        
        String[] keywords = { 
            "a", "aa", "aax", "aaxa",
            "b", "ba", "bac", "back", "backb", "backbe", "backbea", "backbeat",
            "c",
            "d",
            "e",
            "f",
            "g",
            "h",
            "i",
            "j",
            "k",
            "l",
            "m",
            "n",
            "o",
            "p",
            "q",
            "r",
            "s",
            "t",
            "u",
            "v",
            "w",
            "x",
            "y",
            "z"
        };
        for(String keyword : keywords) {
            createStorageCache(keyword);            
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

        if (targetId != null) {
            // doAutoCompleteGet
            doAutoCompleteGet(targetId.toLowerCase(), request, response);
        } else if(action == null ) {
            context.getRequestDispatcher("/error.jsp").forward(request, response);
        }                    
    }
    
    private void doAutoCompleteGet(String key, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        boolean addToXMLCache = false;

        final String blobName = key+FILE_EXTENSTION_NAME;
        StringBuilder sb = null;
                
        logger.log(Level.INFO, "key:{0}", key);

        // check if user sent empty string
        if (!key.equals("")) {
            
            logger.log(Level.INFO, "Pre-fetch with {0} ....", key);
            requestPreFetch(key);

            logger.log(Level.INFO, "Searching for word starting with [{0}].", key);
            
            sb = getFromStorageCache(key);
            if (sb == null){ // No cache stored
                //Now search on DB and store the XML on storage cache
                try {
                    //eventual to call putIntoStorageCache
                    String xmlStr = ProductNameInfoFactory.getProductNameInfo(key);

                    // Save xml data into Google Storage
                    if(xmlStr != null) {
                        if(putIntoStorageCache(key, xmlStr)) {
                            // Put entry on Storage Cache manager
                            // KEY: key, VALUE: filename ([key].xml
                            _storageXMLCacheIndex.put(key, blobName);                        
                            logger.log(Level.INFO,"XML {0} cache registered.", blobName);                
                        }
                    } else {
                        logger.log(Level.INFO," Entries matching {0} could not be found. Thus no XML cache was created in the Storage Cache.", key);                
                    }

                } catch (ServletException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }                
            } else {
                if(sb.length()>0) { //If the XML corresponding to the keyword was already stored in the storage
                    logger.log(Level.INFO, "Cache stored in the storage found for reuse. {0}", sb.toString());
                } else {
                    logger.log(Level.INFO, "Cache stored in the storage found but no entry exist. {0}", sb.toString());
                    sb = null;
                }
            }
        }
        if (sb != null) {
            response.setContentType("text/xml");
            response.setHeader("Cache-Control", "no-cache");
            response.getWriter().write("<products>" + sb.toString() + "</products>");            
        } else {
            //nothing to show
            context.getRequestDispatcher("/error.jsp").forward(request, response);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
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


    private StringBuilder getFromStorageCache(String key) {
        StringBuilder sb = null;
        /**
         * Here we should consider searching on the storage cache XML data created before.
         * Simply we should create a storage cache with the given word (String key) as KEY to retrieve the XML
         * Data should be stored as an XML file
         *  HERE COMES THE word (ex. "a", "ab", "ac"...)
         *  FILENAME: [keyword].xml
         *  <product>
         *   <id>HERE COMES THE PRODUCT ID</id>
         *   <name>HERE COMES THE PRODUCT NAME</name>
         *  <product>
         */
        
        // _storageXMLCacheIndex.put(key, key+FILE_EXTENSTION_NAME);
        final String xmlFileName = _storageXMLCacheIndex.get(key);
        final String blobName = key+FILE_EXTENSTION_NAME;

        // no cache registered on Storage Cache
        if(xmlFileName == null) {
            logger.log(Level.INFO, "No XML cache found in the Storage Cache.");                    
            return null;
        }

        //xml file stored on Storage Cache
        /**
         * get from Google Storage here
         * REFERENCE: https://github.com/GoogleCloudPlatform/google-cloud-java/tree/master/google-cloud-storage
         */
        Storage storage = StorageOptions.getDefaultInstance().getService();
        
        try {
            // Retrieve a blob from the bucket        
            BlobId blobId = BlobId.of(bucketName, blobName);
            Blob blob = storage.get(blobId);
            if(blob != null) {
                byte[] content = storage.readAllBytes(blobId);
                String contentString = new String(content, UTF_8);                
                if(contentString.length() > 0) {
                    logger.log(Level.INFO, "Blob {0} includes product information data.", blobName);
                    sb = new StringBuilder();
                    sb.append(contentString);
                    logger.log(Level.INFO, "XML content: {0}", sb.toString());
                } else {
                    logger.log(Level.INFO, "Blob {0} exist but DOES NOT include product information data.", blobName);
                    _storageXMLCacheIndex.remove(key);
                    logger.log(Level.INFO, "Removed {0} from XML index.", blobName);
                }
            } else {
                logger.log(Level.INFO, "{0} not found.", blobName);
                _storageXMLCacheIndex.remove(key);
                logger.log(Level.INFO, "{0} was registered but since the file is not found in the Googole Storage let's remove from XML index.", blobName);
            }
        } catch(com.google.cloud.storage.StorageException e) {
            logger.log(Level.SEVERE, "{0}", e.toString());                        
        }        
        return sb;
    }

    /**
     * This is to request creation of the cache on the Google Storage
     * Product name information will be retrieved from the database and put into the xml file and stored in the cache
     * 
     * @param key This is the keyword on which product name start with. XML file will be named after the key value and the product name information will be stored in the xml file.
     */
    public void createStorageCache(String key) {
        /**
         * Here store the xml file for future reference without creating the product name information associated with the key
         */
        logger.log(Level.INFO, "key:{0}", key);
        final String blobName = key+FILE_EXTENSTION_NAME;
        
        if(key == null) {
            // this should never happen
            logger.log(Level.SEVERE," Wrong call with createStorageCache. The keyword is not specified.");                            
        } else {        
            try {
                //eventual to call putIntoStorageCache
                String xmlStr = ProductNameInfoFactory.getProductNameInfo(key);

                // Save xml data into Google Storage
                if(xmlStr != null) {
                    if(putIntoStorageCache(key, xmlStr)) {
                        // Put entry on Storage Cache manager
                        // KEY: key, VALUE: filename ([key].xml
                        _storageXMLCacheIndex.put(key, blobName);                        
                        logger.log(Level.INFO,"XML {0} cache registered.", blobName);                
                    }
                } else {
                    logger.log(Level.INFO," Entries matching {0} could not be found. Thus no XML cache was created in the Storage Cache.", key);                
                }

            } catch (ServletException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    private boolean putIntoStorageCache(String key, String cache) {
        boolean retVal = false;
        if(key == null) return false;

        final String blobName = key+FILE_EXTENSTION_NAME;

        /**
         * Here store the xml file for future reference without creating the product name information associated with the key
         */
        logger.log(Level.INFO, "key:{0}", key);
        logger.log(Level.INFO, "XML Cache:{0}",cache);
        /**
         * put on Google Storage here
         * REFERENCE: https://github.com/GoogleCloudPlatform/google-cloud-java/tree/master/google-cloud-storage
         */
        Storage storage = StorageOptions.getDefaultInstance().getService();

        logger.log(Level.INFO, "Target Bucket:{0}", bucketName);
        logger.log(Level.INFO, "Target blob:{0}", blobName);

        try {
            // Create a bucket
            if(storage.get(bucketName, Storage.BucketGetOption.fields()) == null) { //Does not exist
                storage.create(BucketInfo.of(bucketName));  // create new bucket
                logger.log(Level.INFO, "Bucket {0} was created.", bucketName);
            }

            // Upload a blob to the newly created bucket
            BlobId blobId = BlobId.of(bucketName, blobName);
            Blob blob = storage.get(blobId);
            if(blob != null) { //blog found, let us delete it.
                boolean deleted = blob.delete(BlobSourceOption.generationMatch());
                if(deleted) { //existed and deleted
                    logger.log(Level.INFO, "Blob {0} was found and deleted.", blobName);
                } else {
                    logger.log(Level.INFO, "Blob {0} was NOT found.", blobName);            
                }            
            }
            // create new one
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();
            storage.create(blobInfo, cache.getBytes(UTF_8));            
            logger.log(Level.INFO, "Blob {0} was created.", blobName);

            retVal = true;
        } catch(com.google.cloud.storage.StorageException e) {
            logger.log(Level.SEVERE, "{0}", e.toString());                        
        }
        return retVal;
    }

    /**
     * Pre-fetch based on the given keyword
     * @param keyword is the String word that is should be the base for other keywords that start from that. This method is to do pre-fetch for all keywords till PRE-FETCH DEPTH on the Trie Tree Data Structure.
     */
    private void requestPreFetch(String keyword) {
        /**
         * TODO PRE-FETCH
         * The idea is to put pre-fetch request on Task QUEUE so the Batch App search for the possible next keywords, and then create the XML cache.
         */
        
        
        /**
         * put request on TASK QUEUE
         * 
         * Pre-fetch depth/level is defined by TRIE_TREE_PRE_FETCH_DEPTH
         * This should be processed on the Batch App, but this is for the memo for future reference.
         */
        
        
        
        
        
    }
}

