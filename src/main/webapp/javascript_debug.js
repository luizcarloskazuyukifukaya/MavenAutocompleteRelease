/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
var req;
var isIE;
var idField;
var output;
var clickHereField;

function init() {

    idField = document.getElementById("id-field");
    clickHereField = document.getElementById("clickhere");

    //Debug
    output = document.getElementById("output");
}

function doLookup() {
    alert( "doLookup() ... with [" + escape(idField.value) +"]" );

    var url = "GetDataServlet?id=" + escape(idField.value);

    //DEBUG
    output.value = url;
    alert( "URL=" + url );

    req = initRequest();
    req.open("GET", url, true);
    req.onreadystatechange = callback;
    req.send(null);
    
}

function initRequest() {
    if (window.XMLHttpRequest) {
        if (navigator.userAgent.indexOf('MSIE') !== -1) {
            isIE = true;
        }
        return new XMLHttpRequest();
    } else if (window.ActiveXObject) {
        isIE = true;
        return new ActiveXObject("Microsoft.XMLHTTP");
    }
}

function callback() {

//DEBUG
alert( "callback() ...RESPONSE=" + req.responseXML);
alert( "callback() ...STATE=" + req.readyState);

    if (req.readyState === 4) {
        if (req.status === 200) {
            
            alert( "callback() ..." + req.responseXML);
        }
    }
    
    //DEBUG
    output.value = req.responseXML;
}

function updateClickHere() {
    clickHereField.setAttribute("href", "GetDataServlet?id=" + idField.value);
}


