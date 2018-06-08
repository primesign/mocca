<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%--

  This JSP page loads a prototypical implementation of a JavaScript version
  of the Mocca applet.
  
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

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
  <head>
    <META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
    <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
    <META HTTP-EQUIV="EXPIRES" CONTENT="Mon, 22 Jul 2002 11:12:01 GMT">
    <META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">
    <title><fmt:message key="title"/></title>
    
    
	<script data-main="scripts/main" src="<%= request.getContextPath() %>/webjars/jquery/3.3.1/jquery.js"></script>
	<script data-main="scripts/main" src="<%= request.getContextPath() %>/webjars/requirejs/2.3.5/require.js"></script>
    <script type="text/javascript" src="mocca-js/libs/jquery.soap-1.7.2.js"></script>
    <script>
		inIframe = false;
		try{
	        if (top.location != window.location) {
	            inIframe = true;
	        }
		} catch(e) {
	        inIframe = true;
		}
	    document.write('<base href="' + document.location.pathname + '" />');
	    document.write("<script type='text/javascript' src='mocca-js/moccajs.js'><\/script>");
        
    </script>
    
    </head>
    <body>
    	  <h1>Mocca-JS DEMO</h1>     
        
        <script type="text/javascript">
	        require.config({
	            //By default load any module IDs from mocca-js/*
	            baseUrl: 'mocca-js',
	            paths: {
	                app: '../app'
	            }
	        });
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
	          require(['libs/workflowexe', 'moccajs', 'backend', 'stal', 'stalMock', 'errorHandler'], function (workflowexe, moccajs){
	        	  moccajs.run(parameters, true);
	        });
	    </script>
  </body>
</html>
</fmt:bundle>
