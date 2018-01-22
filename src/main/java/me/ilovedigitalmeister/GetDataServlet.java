/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ilovedigitalmeister;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 *
 * @author kazuyuf
 */
public class GetDataServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(GetDataServlet.class.getName());
    private ServletContext context;

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.context = config.getServletContext();
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws java.sql.SQLException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        
        String targetId = request.getParameter("id");
        StringBuilder sb = new StringBuilder();
        StringBuilder sql = new StringBuilder();

        String dbStr = System.getProperty("ae-cloudsql.local-database-url");  //local connection
        
        logger.log(Level.INFO, "LOCAL DB:{0} ", System.getProperty("ae-cloudsql.local-database-url"));
        logger.log(Level.INFO, "  GCP DB:{0} ", System.getProperty("ae-cloudsql.cloudsql-database-url"));
                
        final String sqlFirstPart  = "SELECT "
                                    + "Id,"
                                    + "list_sku,"
                                    + "list_name,"
                                    + "list_price,"
                                    + "list_description,"
                                    + "list_url,"
                                    + "list_image"
                                        + " FROM autocomplete";

                                        //+ " FROM products";

        String path = request.getRequestURI();
        if (path.startsWith("/favicon.ico")) {
          return; // ignore the request for favicon.ico
        }

        sql.append(sqlFirstPart);

        if (targetId != null) {
            targetId = targetId.trim().toLowerCase();
            if(isNumeric(targetId)) {
                // Id is numeric
                targetId = targetId.trim().toLowerCase();
                sql.append(" WHERE Id=").append(targetId).append(" ORDER BY list_name DESC;");

            } else {
                // Do not specify Id and list all
                sql.append(" ORDER BY list_name DESC LIMIT 100;");
            }

        } else {
            context.getRequestDispatcher("/error.jsp").forward(request, response);
        }
         
        if (System.getProperty("com.google.appengine.runtime.version").startsWith("Google App Engine/")) {
           // Check the System properties to determine if we are running on appengine or not
           // Google App Engine sets a few system properties that will reliably be present on a remote
           // instance.
           dbStr = System.getProperty("ae-cloudsql.cloudsql-database-url");
           try {
             // Load the class that provides the new "jdbc:google:mysql://" prefix.
             Class.forName("com.mysql.jdbc.GoogleDriver");
           } catch (ClassNotFoundException e) {
             throw new ServletException("Error loading Google JDBC Driver", e);
           }
        }
        logger.log(Level.INFO, "connecting to:{0} ", dbStr);
        logger.log(Level.INFO, "SQL Statement:{0} ", sql.toString());
        
        try(Connection conn = DriverManager.getConnection(dbStr);) {

         try (ResultSet rs = conn.prepareStatement(sql.toString()).executeQuery()) {
             int i = 0;
             while (rs.next()) {
               sb.append("<h2>ID: [").append(rs.getString("Id")).append("] </h2></br>");
               sb.append("SKU:").append(rs.getString("list_sku")).append("</br>");
               sb.append("NAME:").append(rs.getString("list_name")).append("</br>");
               sb.append("IMAGE URL:").append(rs.getString("list_image")).append("</br>");
               sb.append("DESC:").append(rs.getString("list_description")).append("</br>");
               i++;
           }
           logger.log(Level.INFO, "got response from the database engine. {0} records found.", i);
         }
       } catch (SQLException e) {
         throw new ServletException("SQL error", e);
       }
                
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            
            out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"stylesheet.css\">");

            out.println("<title>Product Details</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Product Details</h1>");
            
            /*
             * Response Body here
             */
/*
            out.println("REQUEST URI =" + request.getRequestURI() + "</br>");
            out.println("REQUEST TYPE =" + action + "</br>");
            out.println("REQUEST ID =" + targetId + "</br>");
*/
            out.println("<a href=\"/\">Go back</a></br></hr>");
            
            out.println(sb.toString());
            
            out.println("</br><a href=\"/\">Go back</a>");
            out.println("</body>");
            out.println("</html>");
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
        try {
            processRequest(request, response);
        } catch (SQLException ex) {
            Logger.getLogger(GetDataServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (SQLException ex) {
            Logger.getLogger(GetDataServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    public static boolean isNumeric(String str)
    {   
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }
}
