/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
var req;
var isIE;
var completeField;
var completeTable;
var autoRow;
var output;

function init() {
    completeField = document.getElementById("complete-field");
    completeTable = document.getElementById("complete-table");
    autoRow = document.getElementById("auto-row");
    completeTable.style.top = getElementY(autoRow) + "px";
    
    //Debug
    output = document.getElementById("output");

    //Temporary solution
    appendProduct("","");
    clearTable();
    
    // Servlet.init() may have not yet been called. So, we should try access the server to initiate the cache if not yet in memory
    //
    requestCacheInitIfnot();
}
function requestCacheInitIfnot() {
    
    var url = "/AutoCompleteServlet?action=refresh"; // No valude specified
    req = initRequest();
    req.open("GET", url, true);
    req.send(null);    
}

function doCompletion() {

    var url = "/AutoCompleteServlet?id=" + escape(completeField.value);
    req = initRequest();
    req.open("GET", url, true);
    req.onreadystatechange = callback;
    req.send(null);        
    //DEBUG
    output.value = completeField.value;
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
    clearTable();

    if (req.readyState === 4) {
        if (req.status === 200) {
            parseMessages(req.responseXML);
        }
    }
    
    //DEBUG
    output.value = req.responseXML.toString();

}

function clearTable() {
    if (completeTable.getElementsByTagName("tr").length > 0) {
        completeTable.style.display = 'none';
        for (loop = completeTable.childNodes.length -1; loop >= 0 ; loop--) {
            completeTable.removeChild(completeTable.childNodes[loop]);
        }
    }
}

function getElementY(element){
    
    var targetTop = 0;
    
    if (element.offsetParent) {
        while (element.offsetParent) {
            targetTop += element.offsetTop;
            element = element.offsetParent;
        }
    } else if (element.y) {
        targetTop += element.y;
    }
    return targetTop;
}

function parseMessages(responseXML) {
    
    // no matches returned
    if (responseXML === null) {
        return false;
    } else {

        var products = responseXML.getElementsByTagName("products")[0];

        if (products.childNodes.length > 0) {
            completeTable.setAttribute("bordercolor", "black");
            completeTable.setAttribute("border", "1");
    
            for (loop = 0; loop < products.childNodes.length; loop++) {
                var product = products.childNodes[loop];
                var name = product.getElementsByTagName("name")[0];
                var id = product.getElementsByTagName("id")[0];
                appendProduct(name.childNodes[0].nodeValue,
                    id.childNodes[0].nodeValue);
            }
        }
    }
}

function appendProduct(name,productId) {

    var row;
    var cell;
    var linkElement;
    
    if (isIE) {
        completeTable.style.display = 'block';
        row = completeTable.insertRow(completeTable.rows.length);
        cell = row.insertCell(0);
    } else {
        completeTable.style.display = 'table';
        row = document.createElement("tr");
        cell = document.createElement("td");
        row.appendChild(cell);
        completeTable.appendChild(row);
    }

    cell.className = "popupCell";

    linkElement = document.createElement("a");
    linkElement.className = "popupItem";
    linkElement.setAttribute("href", "GetDataServlet?id=" + productId);
    linkElement.appendChild(document.createTextNode(name));
    cell.appendChild(linkElement);
}


