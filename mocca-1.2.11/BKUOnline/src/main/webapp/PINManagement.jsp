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
         import="at.gv.egiz.org.apache.tomcat.util.http.AcceptLanguage"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>MOCCA PIN Management</title>
        <link rel="shortcut icon" href="img/chip16.ico" type="image/x-icon">
        <script type="text/javascript" src="js/deployJava.js"></script>
        <style type="text/css" media="all">@import "css/applet.css";</style>

        <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
        <META HTTP-EQUIV="EXPIRES" CONTENT="Mon, 22 Jul 2002 11:12:01 GMT">
        <META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">
    </head>
    <%
        String locale = request.getParameter("locale");
        if (locale == null) {
            String acceptLanguage = request.getHeader("Accept-Language");
            locale = AcceptLanguage.getLocale(acceptLanguage).toString();
        }
        String widthP = request.getParameter("appletWidth");
        String heightP = request.getParameter("appletHeight");
        int width = (widthP == null) ? 295
                : Integer.parseInt(widthP);
        int height = (heightP == null) ? 200
                : Integer.parseInt(heightP);
        String guiStyle = request.getParameter("appletGuiStyle");
        if (guiStyle == null) {
            guiStyle = "advanced";
        }
        String backgroundImg = request.getParameter("appletBackground");
    %>
    <body id="appletpage" style="width:<%=width%>" onFocus="focusToApplet()">
            <script>
            
            	// avoid selection of applet before it is completely loaded
				var allowSelectionByJS = false;            
            
                if (!deployJava.versionCheck('1.6.0_04+')) {
                    document
                    .write('<b>Diese Anwendung benötigt die Java Platform Version 1.6.0_04 oder höher.</b>' + '<input type="submit" value="Java Platform 1.6.0_02 installieren" onclick="deployJava.installLatestJRE();">');
                } else {
                    var attributes = {
                        codebase :'applet',
                        code : 'at.gv.egiz.bku.online.applet.PINManagementApplet.class',
                        archive : 'BKUAppletExt.jar, commons-logging.jar, iaik_jce_me4se.jar',
                        width : <%=width%>,
                        height :<%=height%>,
                        name : 'moccaapplet',
                        id : 'moccaapplet'
                    };
                    var parameters = {
                        GuiStyle : '<%=guiStyle%>',
                        Locale : '<%=locale%>',
                        Background : '<%=backgroundImg%>',
                        HelpURL : '../help/',
                        SessionID : '<%=request.getSession().getId()%>',
                        RedirectURL : '../',
                        RedirectTarget: '_parent',
                        EnforceRecommendedPINLength: 'true'
                    };
                    var version = '1.6.0_04';
                    deployJava.runApplet(attributes, parameters, version);
                }
            </script>
    </body>
    
    <script>

		function focusToApplet() {
	
			//alert('try to set focus to applet');
			if (document != null && document.moccaapplet != null && allowSelectionByJS) {
	
				//alert('set focus to applet.');
				document.moccaapplet.getFocusFromBrowser();						
				return true;					
			}
			allowSelectionByJS = true;
		}
	
		function focusToBrowser() {

			// put focus to window
			// focus can be assigned to an arbitrary focusable field on the embedding website as well
			self.focus();
	
		}		
    
    </script>
    
</html>
