<%-- 
 *******************************************************************************
 * <copyright> Copyright 2018 by PrimeSign GmbH, Graz, Austria </copyright>
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 ****************************************************************************** 
--%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%--

  This JSP page loads an implementation of a JavaScript version
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
      <script src="mocca-js/libs/jquery-3.3.1.min.js"></script>
      <script src="mocca-js/libs/require-2.3.5.min.js"></script> 
      <script type="text/javascript" src="mocca-js/libs/jquery.soap-1.7.2.js"></script>
      <link rel="stylesheet" type="text/css" href="mocca-js/style/main.css" />

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
      <!-- The Bootstrap 3 grid system has four tiers of classes: xs (phones), sm (tablets), md (desktops), and lg (larger desktops) -->
      <div class="container">
        <div id="messageContainer" class="col-xs-12">
          <div id ="alert" class="alert alert-info">
            <p id="paragraph"></p>
          </div>
        </div> 
      </div>
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
          lang.setLocale('<c:out value="${requestScope.moccaParam.locale}" default=""/>');
          document.getElementById('paragraph').innerHTML = mocca_js.lang.translate('info.start');
          moccajs.run({
            SessionID: '<c:out value="${requestScope.id}"/>',
            ContextPath: '<%= request.getContextPath() %>'
          }, false).then(function(){
              var alert = $("#alert");
              alert.text = lang.translate('info.finished');
              alert.removeClass('alert-info');
              alert.addClass('alert-success');
          });
        });
      </script>
    </body>
  </html>
</fmt:bundle>
