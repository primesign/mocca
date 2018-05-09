<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%--

  This JSP page loads the MOCCA applet into the browser. It is not intended
  to by directly called by the browser, but the UIServlet should be called.
  The UIServlet sets some request attributes and forwards to this page.
  
  The look and behavior of this page and the embedded applet may be 
  customized by a number of parameters handed over in the request. If this
  does not provide enough flexibility you may choose to modify this page
  or provide an alternative customized page. The name of the customized page
  may be specified as parameter in the original request. The UIServlet will
  forward to the given page.
  
 --%>
<%-- Images --%>
<c:set var="defaultButtonWidth" value="16"/>
<c:set var="defaultButtonHeight" value="22"/>
<%-- URLs --%>
<c:set value="./applet" var="codebase"/>
<c:url value="/help/" var="helpUrl"/>
<c:url value="/stal?wsdl" var="wsdlUrl"/>
<c:url value="/result" var="resultUrl"/>
<%-- Messages --%>
<fmt:bundle basename="web">

<%-- 
  Uncomment the following line if you would like to prevent applet caching! 
--%>
<%-- jsp:useBean id="now" class="java.util.Date" scope="request"/ --%>
<c:if test="${! empty now}">
  <c:set var="appletArchive" value="${appletArchive}?no-cache=${now.time}"/>
</c:if>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
  <head>
    <META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
    <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
    <META HTTP-EQUIV="EXPIRES" CONTENT="Mon, 22 Jul 2002 11:12:01 GMT">
    <META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">
    <title><fmt:message key="title"/></title>
    <link rel="shortcut icon" href="img/chip16.ico" type="image/x-icon">
    <style type="text/css" media="all">
      root {
        display: block;
      }
      body {
        background: #ffffff;
        padding: 0;
        margin: 0;
        border-style: none;
        width: <c:out value="${requestScope.moccaParam.appletWidth}" default="${defaultWidth}"/>px;
      }
      #container {
        padding: 0;
        margin: 0;
        width: <c:out value="${requestScope.moccaParam.appletWidth}" default="${defaultWidth}"/>px;
        background: <c:out value="${requestScope.moccaParam.appletBackgroundColor}" default="#eeeeee"/>;
      }
      #container applet{
        padding: 0;
        margin: 0;
      }
      .button {
        width: <c:out value="${defaultButtonWidth}px"/>;
        height: <c:out value="${defaultButtonHeight}px"/>;
        text-decoration: none;
        border-style: none;
      }
      #buttons {
        padding: 0;
        margin: 0;
      }
      #message {
        margin: 0;
        padding: 0.25em 0 0.25em 0;
        clear: both;
      }
      #message p{
        margin: 0;
        padding: 0.25em 0.5em 0.25em 0.5em;
      }
    </style>
    
    <script type="text/javascript" src="js/jquery-3.3.1.min.js"></script>
    <script type="text/javascript" src="js/jquery.soap-1.7.2.js"></script>
    <script type="text/javascript" src="js/xml2json.js"></script>
    
    </head>
    <body>
    	  <h1>Mocca-JS DEMO</h1>
        
        
        <script type="text/javascript">
        
          var parameters = {
            GuiStyle : '<c:out value="${requestScope.moccaParam.guiStyle}" default="simple"/>',
            Locale : '<c:out value="${requestScope.moccaParam.locale}" default=""/>',
            Background : '<c:out value="${requestScope.moccaParam.appletBackground}" default=""/>',
            BackgroundColor : '<c:out value="${requestScope.moccaParam.appletBackgroundColor}" default="#eeeeee"/>',
            WSDL_URL : '<c:out value="${wsdlUrl}"/>',
            HelpURL : '<c:out value="${helpUrl}"/>',
            SessionID : '<c:out value="${requestScope.id}"/>',
            RedirectURL : '<c:out value="${resultUrl}"/>',
            RedirectTarget : '<c:out value="${requestScope.moccaParam.redirectTarget}" default="_parent"/>'
          };
          
//           var connectRequest = ['<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:stal="http://www.egiz.gv.at/stal">',
//           							'<soapenv:Header/>',
//           								'<soapenv:Body>',
//              								'<stal:SessionId>',
//              									'<c:out value="${requestScope.id}"/>',
//              								'</stal:SessionId>',
//           								'</soapenv:Body>',
//        								'</soapenv:Envelope>'];
          
          // Initial Connect Request
          $.soap({
              url: '/BKUOnline/stal',
              method: 'connect',
              namespaceQualifier: 'stal',
              namespaceURL: 'http://www.egiz.gv.at/stal',
              appendMethodToURL: false,
              elementName: 'SessionId',
              
              data: '<c:out value="${requestScope.id}"/>',             
            	  //data: connectRequest.join(''), when using xml string array above
              
              success: function (soapResponse) {
                  // do stuff with soapResponse
                  // if you want to have the response as JSON use soapResponse.toJSON();
                  // or soapResponse.toString() to get XML string
                  // or soapResponse.toXML() to get XML DOM
                  console.log('BKUOnline response: ' + soapResponse.toString());
                  console.log(soapResponse.toJSON());
              },
              error: function (soapResponse) {
                  // show error
                  console.log('BKUOnline error: ' + soapResponse.toString());
              }
          });
          
      </script>

  </body>
</html>
</fmt:bundle>
