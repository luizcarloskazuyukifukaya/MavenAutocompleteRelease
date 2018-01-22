<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Product Search Home</title>

    <script type="text/javascript" src="javascript.js"></script>
    <link rel="stylesheet" type="text/css" href="stylesheet.css">
  </head>
  <body onload="init()">
    <h1>Product Search Home</h1>
    <a href="http://www.google.com/">
        
        <img src="https://www.google.co.jp/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png" 
             alt="Search external information from here."
             style="width:auto;height:40px;" />
    </a>

    <p>In the form below enter a product name. Possible product names that will be completed are displayed below
        the form. For example, try typing in &quot;<b>PlayStation</b>&quot;, &quot;<b>Xbox&quot;</b> or &quot;<b>Nintendo Switch</b>&quot;
        then click on one of the selections to see product details.</p>

    <form name="autofillform" action="autocomplete">
      <table border="0" cellpadding="5"> 
          <tr>
            <td>
                <strong>Product Name:</strong>
            </td>
          </tr>
          <tr>
            <td>
                <input type="text" size="64" id="complete-field" autocomplete="off" onkeyup="doCompletion()">
            </td>
          </tr>
          <tr>
              <td id="auto-row" colspan="2">
                 <table id="complete-table" class="popupBox" />
              </td>
          </tr>
      </table>
    </form>

    <p><i>powered by NetBeans and Google App Engine.</i></p>
    <p></p>
<!--    
    <hr>
        <table border="0" cellpadding="5"> 
          <tbody>
            <tr>
                <td colspan="4">
                     <div>
                         <textarea name="output" id="output" cols=64 rows=4></textarea>
                    </div>
                </td>
            </tr>
          </tbody>
        </table>
    </p>
-->
  </body>
</html>
