<!--
  Copyright 2008 Federal Chancellery Austria and
  Graz University of Technology

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<%@ page language="java" contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8" 
  import="at.gv.egiz.bku.online.webapp.AppletDispatcher,org.apache.commons.lang.RandomStringUtils,org.apache.commons.lang.StringEscapeUtils" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
   <head>
       <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
       <title>MOCCA</title>
       <link rel="shortcut icon" href="img/chip16.ico" type="image/x-icon">
       <style type="text/css" media="all">@import "css/applet.css";</style>

       <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
       <META HTTP-EQUIV="EXPIRES" CONTENT="Mon, 22 Jul 2002 11:12:01 GMT">
       <META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">
   </head>
   <%
     int width = session.getAttribute("appletWidth") == null ? 190
         : (Integer) session.getAttribute("appletWidth");
     int height = session.getAttribute("appletHeight") == null ? 130
         : (Integer) session.getAttribute("appletHeight");
     String backgroundImg = StringEscapeUtils.escapeJavaScript(session
         .getAttribute("appletBackground") == null ? "../img/chip32.png"
         : (String) session.getAttribute("appletBackground"));
     String backgroundColor = StringEscapeUtils
         .escapeJavaScript((String) session
             .getAttribute("appletBackgroundColor"));

     String sessionId = StringEscapeUtils.escapeJavaScript(session.getId());

     String dataURL = request.getRequestURL().toString();
     dataURL = dataURL.substring(0, dataURL.lastIndexOf('/')) + "/DataURLServer";
   %>
   <body id="appletpage" style="width:<%=width%>px;height:<%=height%>px">
     <div style="width:<%=width%>px;height:<%=height%>px">
		     <form action="http://127.0.0.1:3495/http-security-layer-request" method="post" enctype="application/x-www-form-urlencoded">
		       <input name="SessionID_" value="<%=sessionId%>" type="hidden"/>
		       <input name="DataURL" value="<%=dataURL%>" id="DataURL" type="hidden"/>
		       <input name="XMLRequest" value="<NullOperationRequest xmlns='http://www.buergerkarte.at/namespaces/securitylayer/1.2#'/>" type="hidden"/>
		       <p>Anfrage wird an lokale BKU gesendet</p>
           <div style="text-align: center;">
		        <input type="submit" style="vertical-align: middle"/>
           </div>
		     </form>
     </div>
   </body>
</html>
