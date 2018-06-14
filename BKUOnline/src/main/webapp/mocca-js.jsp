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
	<!-- <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.js" ></script> -->
	<!-- <script src="https://cdnjs.cloudflare.com/ajax/libs/i18next/8.1.0/i18next.min.js" ></script>   -->
		<!-- Latest compiled and minified CSS -->
		<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css"
			integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous" />
		
		<!-- Optional theme -->
		<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css"
			integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp" crossorigin="anonymous" />
		
		<!-- Latest compiled and minified JavaScript -->
		<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"
			integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>
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
		<h1 id="title"></h1>    
		      
		<script type="text/javascript"> 
			require.config({
	            //By default load any module IDs from mocca-js/*
	            baseUrl: 'mocca-js',
	            paths: {
					app: '../app',
					json: 'libs/json'
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
	          require(['libs/workflowexe', 'moccajs', 'backend', 'stal', 'stalMock', 'errorHandler', 'lang/translation', 'lang/translationDE', 'libs/i18next.min', 'libs/LngDetector' ], function (workflowexe, moccajs, backend, stal, stalMock, errorHandler, translation, translationDE, i18next, LngDetector){
				  moccajs.run(parameters, true);

				//   i18next
				  i18next
				  .use(LngDetector)	  
				  .init({
					detection: {// Order of Language Detection
						order: ['querystring', 'cookie', 'localStorage', 'navigator', 'htmlTag', 'path', 'subdomain'],
					},
					fallbackLng: 'en',
					debug: true,
					resources: {
						  en: {
							translation: translation
						  },
						  de: {
							  translation: translationDE	
						  }
					  }
				  });		
				document.getElementById('title').innerHTML = i18next.t('title');
			});
	    </script>
  </body>
</html>
</fmt:bundle>
