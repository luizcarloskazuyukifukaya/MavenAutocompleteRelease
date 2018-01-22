<%-- 
    Document   : debug
    Created on : Jan 20, 2018, 11:08:30 PM
    Author     : kazuyuf
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

        <!-- This is for easy Javascript debuging -->
        <meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate" />
        <meta http-equiv="Pragma" content="no-cache" />
        <meta http-equiv="Expires" content="0" />
        <!-- This is for easy Javascript debuging -->
        
        <title>MySQL Connection Page</title>
        
        <script type="text/javascript" src="javascript_debug.js"></script>
        <link rel="stylesheet" type="text/css" href="stylesheet.css">

    </head>
    <body onload="init()">
        <h1>Welcome!</h1>
        
        <table border="0">
            <thead>
                <tr>
                    <th>Product Information Search</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>Please specify the product id below:</td>
                </tr>
                <tr>
                    <td>
                        Product id :
                        <input type="text" size="8" name="id-field" id="id-field" autocomplete="off" onkeyup="updateClickHere()" />
                        <a href="GetDataServlet?action=complete&id=" name="clickhere" id="clickhere">Click Here</a></br>
<!--
                        <p name="getdata" onclick="doLookup()">Get Details</p>
                        <input type="button" value="lookup" name="lookup" onclick="doLookup()" />
                        <hr/>
                        <textarea name="output" id="output" rows="10" cols="15"></textarea>                           
-->
                    </td>
                </tr>
            </tbody>
        </table>
    </body>
</html>
