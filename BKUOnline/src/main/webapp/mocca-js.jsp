<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%--

  This JSP page loads a prototypical implementation of a JavaScript version
  of the Mocca applet.
  
 --%>
 
<fmt:bundle basename="web">

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
    <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
    <META HTTP-EQUIV="EXPIRES" CONTENT="Mon, 22 Jul 2002 11:12:01 GMT">
    <META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">
    <title><fmt:message key="title"/></title>
    
	<script src="mocca-js/logging.js"></script>
    
	<script src="<%= request.getContextPath() %>/webjars/jquery/3.3.1/jquery.js"></script>
	<script src="<%= request.getContextPath() %>/webjars/requirejs/2.3.5/require.js"></script> 
  <script type="text/javascript" src="mocca-js/libs/jquery.soap-1.7.2.js"></script>
  
  <link rel="stylesheet" type="text/css" href="mocca-js/libs/bootstrap.min.css" /><!-- Latest compiled and minified CSS -->
  <link rel="stylesheet" type="text/css" href="mocca-js/libs/bootstrap-theme.min.css" /><!-- Optional theme -->
  <script type="text/javascript" src="mocca-js/libs/bootstrap.min.js"></script><!-- Latest compiled and minified JavaScript -->
      
		<script type="text/javascript">
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
		<div id="messageContainer" class="col-xs-12" style="padding-top: 15px;"></div> 
			      
		<script type="text/javascript"> 
      require.config({
        //By default load any module IDs from mocca-js/*
        baseUrl: 'mocca-js',
        paths: {
          app: '../app',
          json: 'libs/json',
          text: 'libs/text'
        }
      });
      require(['libs/workflowexe', 'backend', 'stal', 'stalMock', 'errorHandler', 'lang', 'moccajs'], function (workflowexe, backend, stal, stalMock, errorHandler, lang, moccajs) {
        moccajs.run({
          Locale: '<c:out value="${requestScope.moccaParam.locale}" default=""/>',
          SessionID: '<c:out value="${requestScope.id}"/>',
          ContextPath: '<%= request.getContextPath() %>'
        }, false);
      });
	  </script>
  </body>
</html>
</fmt:bundle>
