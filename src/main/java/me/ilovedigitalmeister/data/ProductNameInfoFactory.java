/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ilovedigitalmeister.data;

//import com.google.common.cache.Cache;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.*;
import java.util.HashMap;
import javax.servlet.ServletException;

/**
 *
 * @author kazuyuf
 */
public class ProductNameInfoFactory {
    
    private static final HashMap<String, ProductNameInfo> productNameInfos = new HashMap();
    private static final Logger logger = Logger.getLogger(ProductNameInfoFactory.class.getName());

    public static final int MAX_NAME_LENTH = 32;

    /**
     * ONLY DISPLY MAX_DISP_CANDIATE_NUM_ITEM
     */
    public static int MAX_DISP_CANDIDATE_NUM_ITEM = 25;
    public static int MAX_DB_LOOKUP_NUM_ITEM = 50000;
    
    public HashMap getProducts() {
        return productNameInfos;
    }
    
    public ProductNameInfoFactory() {
        
        try {
            if(getFromDatabase()) {
                
                logger.log(Level.INFO, "Data created from database.");
                
            } else {
                /** Create dummy data when instance is created **/            
                createDemoDataProducts();
            }
        } catch (ServletException ex) {
            /** Create dummy data when instance is created **/            
            createDemoDataProducts();
            logger.log(Level.SEVERE, "SQL Database error occured. {0}", ex.toString());
        }
    }

    public ProductNameInfoFactory(boolean mode) {
        
        /** Create dummy data when instance is created **/            
        createDemoDataProducts();
    }
    
    private HashMap createDemoDataProducts() {

        logger.log(Level.INFO, "Switching to dummy information ... for demo facilitation purpose... ");
        
        productNameInfos.put("100", new ProductNameInfo("100","Apple iPhone"));
        productNameInfos.put("101", new ProductNameInfo("101","Apple iPhone 3G"));
        productNameInfos.put("102", new ProductNameInfo("102","Apple iPhone 3GS"));
        productNameInfos.put("103", new ProductNameInfo("103","Apple iPhone 4"));
        productNameInfos.put("104", new ProductNameInfo("104","Apple iPhone 4S"));
        productNameInfos.put("105", new ProductNameInfo("105","Apple iPhone 5"));
        productNameInfos.put("106", new ProductNameInfo("106","Apple iPhone 5s"));
        productNameInfos.put("107", new ProductNameInfo("107","Apple iPhone 5C"));
        productNameInfos.put("108", new ProductNameInfo("108","Apple iPhone 6"));
        productNameInfos.put("109", new ProductNameInfo("109","Apple iPhone 6 Plus"));
        productNameInfos.put("110", new ProductNameInfo("110","Apple iPhone 6s"));
        productNameInfos.put("111", new ProductNameInfo("111","Apple iPhone 6s Plus"));
        productNameInfos.put("112", new ProductNameInfo("112","Apple iPhone SE"));
        productNameInfos.put("113", new ProductNameInfo("113","Apple iPhone 7"));
        productNameInfos.put("114", new ProductNameInfo("114","Apple iPhone 7 Plus"));
        productNameInfos.put("115", new ProductNameInfo("115","Apple iPhone 7s"));
        productNameInfos.put("116", new ProductNameInfo("116","Apple iPhone 7s Plus"));
        productNameInfos.put("117", new ProductNameInfo("117","Apple iPhone X"));

        productNameInfos.put("200", new ProductNameInfo("200","Big Blue"));
        productNameInfos.put("300", new ProductNameInfo("300","Compaq iPaq"));
        
        productNameInfos.put("501", new ProductNameInfo("501","Google Pixel 2"));
        productNameInfos.put("502", new ProductNameInfo("502","Google Pixel 2 XL"));
        productNameInfos.put("503", new ProductNameInfo("503","Google Pixel XL"));
        productNameInfos.put("504", new ProductNameInfo("504","Google Pixel"));
        productNameInfos.put("505", new ProductNameInfo("505","Google Pixel C"));

        productNameInfos.put("1000", new ProductNameInfo("1000","HPE"));
        productNameInfos.put("2000", new ProductNameInfo("2000","Digital Equipment Corp."));
        productNameInfos.put("3000", new ProductNameInfo("3000","OpenVMS"));

        productNameInfos.put("4000", new ProductNameInfo("4000","PlayStation"));
        productNameInfos.put("5000", new ProductNameInfo("5000","Xbox"));
        productNameInfos.put("5001", new ProductNameInfo("5001","Xbox One"));
        productNameInfos.put("5002", new ProductNameInfo("5002","Xbox 360"));
        productNameInfos.put("6001", new ProductNameInfo("6001","Nintendo Will"));
        productNameInfos.put("6002", new ProductNameInfo("6002","Nintendo Switch"));
        productNameInfos.put("6003", new ProductNameInfo("6003","Nintendo 3DS"));

        return productNameInfos;
    }

    /**
     * Retrieve all product information into a memory cache
     * 
     * @return false if failed to retrieve from the database, otherwise true
     * @throws ServletException then failed with database operation
     */
    public boolean getFromDatabase() throws ServletException {
        boolean retVal = false;
        HashMap<String, ProductNameInfo> products = productNameInfos;
        // User products for reference to the HasMap (Cache data)
        
        // **********************************************************        
        // JCache memory allocation for 1 DAY (24 hours)
        // **********************************************************
/**
 * 
        Cache cache;
        try {
            CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
            cache = cacheFactory.createCache(Collections.emptyMap());
        } catch (CacheException e) {
            // ...
        }

        CachingProvider cachingProvider = Caching.getCachingProvider();
        CacheManager cacheManager = cachingProvider.getCacheManager();

        Configuration<String, String> config =
            new MutableConfiguration<String, String>()
            .setTypes(String.class, String.class)
            .setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(new Duration(TimeUnit.DAYS, 1)));

        Cache<String, String> cache = cacheManager.createCache("ProductNameInfoCache", config);
*/
    // Access Database here
        StringBuilder sql = new StringBuilder();

        String dbStr = System.getProperty("ae-cloudsql.local-database-url");  //local connection
        
        logger.log(Level.INFO, "LOCAL DB:{0} ", System.getProperty("ae-cloudsql.local-database-url"));
        logger.log(Level.INFO, "  GCP DB:{0} ", System.getProperty("ae-cloudsql.cloudsql-database-url"));
                
        final String sqlStr  = "SELECT "
                                    + "Id,"
                                    + "list_name"
                                        + " FROM autocomplete";

        //sql.append(sqlStr).append(" ORDER BY list_name DESC;");;
        //sql.append(sqlStr).append(" WHERE list_name LIKE 'A%' ORDER BY list_name ASC LIMIT 50;");
        sql.append(sqlStr).append(" ORDER BY list_name ASC LIMIT ").append(MAX_DB_LOOKUP_NUM_ITEM).append(";");
         
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
                    // Put the data into cache here 
                    // Limit the size of list_name to 32 [s.substring(0, Math.min(s.length(), MAX_NAME_LENTH));]
                    String n = rs.getString("list_name");
                    if (n != null) {
                        n = n.trim();
                        if( !(n.isEmpty()) ) {
                            n = n.substring(0, Math.min(n.length(), MAX_NAME_LENTH));
                            productNameInfos.put(rs.getString("Id"), new ProductNameInfo(rs.getString("Id"), n));
                            i++;                            
                        }
                    }
                }
                logger.log(Level.INFO, "Got response from the database engine. {0} records is valid.", i);
                if (i>0) {
                    // records found
                    retVal = true;
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to connect to SQL DB {0}.", dbStr);
            throw new ServletException("SQL error", e);
        }
        return retVal;
    }

        /**
     * Retrieve all product information into a memory cache
     * 
     * @param key keyword to create the String cache
     * @return false if failed to retrieve from the database, otherwise true
     * @throws ServletException then failed with database operation
     */
    public static String getProductNameInfo(String key) throws ServletException {
        
        String retVal = null;
        
        if (key == null) return null;
        if (key.length() < 1) return null;
        
        StringBuilder productNameInfoStr = new StringBuilder();

        // Access Database here
        StringBuilder sql = new StringBuilder();

        String dbStr = System.getProperty("ae-cloudsql.local-database-url");  //local connection
        
        logger.log(Level.INFO, "LOCAL DB:{0} ", System.getProperty("ae-cloudsql.local-database-url"));
        logger.log(Level.INFO, "  GCP DB:{0} ", System.getProperty("ae-cloudsql.cloudsql-database-url"));
                
        final String sqlStr  = "SELECT "
                                    + "Id,"
                                    + "list_name"
                                        + " FROM autocomplete";

        // WHERE list_name LIKE '[key]%' ... list_name starting with key
        sql.append(sqlStr).append(" WHERE list_name LIKE ").append("'").append(key).append("%'");
        sql.append(" ORDER BY list_name ASC LIMIT ").append(MAX_DB_LOOKUP_NUM_ITEM).append(";");
         
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
                    // Put the data into cache here 
                    // Limit the size of list_name to 32 [s.substring(0, Math.min(s.length(), MAX_NAME_LENTH));]
                    String n = rs.getString("list_name");
                    if (n != null) {
                        n = n.trim();
                        if( !(n.isEmpty()) ) {
                            /**
                             * *******************************************************
                             * LIMITING THE PRODUCT NAME HERE
                             * *******************************************************
                             */
                            n = n.substring(0, Math.min(n.length(), MAX_NAME_LENTH));
                            /**
                             * *******************************************************
                             * LIMITING THE PRODUCT NAME HERE (MAY REMOVE IT)
                             * *******************************************************
                             */

                            // XML String created here
                            productNameInfoStr.append("<product>");
                            productNameInfoStr.append("<id>").append(rs.getString("Id")).append("</id>");
                            productNameInfoStr.append("<name>").append(n).append("</name>");
                            productNameInfoStr.append("</product>");
                            i++;                            
                        }
                        
                        if(i>MAX_DISP_CANDIDATE_NUM_ITEM-1) {
                            logger.log(Level.INFO, "Too many entries ... skipping search with limit to {0}.", MAX_DISP_CANDIDATE_NUM_ITEM);                    
                            break;
                        }
                    }
                }
                logger.log(Level.INFO, "Storage Cache: Got response from the database engine. {0} records is valid.", i);
                if (i>0) {
                    // records found
                    retVal = productNameInfoStr.toString();
                    logger.log(Level.INFO, "XML String:{0}", retVal);
                }
                
                logger.log(Level.INFO, " Got {0} candidate for the Storage Cache file against the keyword.",i);
                
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to connect to SQL DB {0}.", dbStr);
            throw new ServletException("SQL error", e);
        }
        return retVal;
    }

    
}
