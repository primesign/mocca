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
  import="at.gv.egiz.bku.online.webapp.AppletDispatcher, org.apache.commons.lang.RandomStringUtils, org.apache.commons.lang.StringEscapeUtils" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>MOCCA Applet</title>
        <link rel="shortcut icon" href="img/chip16.ico" type="image/x-icon">
        <script type="text/javascript" src="js/deployJava.js"></script>
        <style type="text/css" media="all">@import "css/applet.css";</style>

        <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
        <META HTTP-EQUIV="EXPIRES" CONTENT="Mon, 22 Jul 2002 11:12:01 GMT">
        <META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">
    </head>
    <%
        String locale = StringEscapeUtils.escapeJavaScript(
            (String) session.getAttribute("locale"));

        int width = session.getAttribute("appletWidth") == null ? 190
                : (Integer) session.getAttribute("appletWidth"); 
        int height = session.getAttribute("appletHeight") == null ? 130
                : (Integer) session.getAttribute("appletHeight");
        String backgroundImg = StringEscapeUtils.escapeJavaScript(
            session.getAttribute("appletBackground") == null 
                ? "../img/chip32.png"
                : (String) session.getAttribute("appletBackground"));
        String backgroundColor = StringEscapeUtils.escapeJavaScript(
            (String) session.getAttribute("appletBackgroundColor"));
        String guiStyle = StringEscapeUtils.escapeJavaScript(
            (String) session.getAttribute("appletGuiStyle"));
        
        String sessionId = StringEscapeUtils.escapeJavaScript(session.getId());
        
        String extension = (String) session.getAttribute("appletExtension");
        String appletClass, appletArchive;
        if ("activation".equalsIgnoreCase(extension)) {
            appletArchive = "BKUAppletExt";
            appletClass = "at.gv.egiz.bku.online.applet.ActivationApplet.class";
        } else if ("pin".equalsIgnoreCase(extension)) {
            appletArchive = "BKUAppletExt";
            appletClass = "at.gv.egiz.bku.online.applet.PINManagementApplet.class";
        } else {
            appletArchive = "BKUApplet";
            appletClass = "at.gv.egiz.bku.online.applet.BKUApplet.class";
        }
     
        // disable applet caching
        boolean disableAppletCaching = false;
        String codebase = "applet";
        
        if (disableAppletCaching)  {
          // run in AppletDispatcher context and
          // append random alphanumeric string to avoid applet caching
          // TODO prepend ../ to all xxxURL applet paramaters 
          codebase += "/" + AppletDispatcher.DISPATCH_CTX;
          String rand = AppletDispatcher.RAND_PREFIX +
                  RandomStringUtils.randomAlphanumeric(16);
          appletArchive += rand;
        }

    %>
    <body id="appletpage" style="width:<%=width%>px">


	<div id="container" style="float: left; width: <%= width %>">
		
		  <form name="increaseform" action="javascript:increaseSize()" method="get">

			<!-- increase font size image -->
			<input type="image" src="img/inc.png" alt="Text vergrößern" width="13px" height="18px" style="float: left;" id="increase_image"/>

		  </form>


		  <form name="decreaseform" action="javascript:decreaseSize()" method="get">

			<!-- decrease font size image -->
			<input type="image" src="img/dec.png" alt="Text verkleinern" width="13px" height="18px" style="float: left;" id="decrease_image"/>		

		  </form>
		

		
		  <form name="helpform" action="help/index.html" method="get" target="_new"
				onsubmit="this.action=document.moccaapplet.getHelpURL(); this.submit(); return false;">

			<!-- invisible input -->
			<input type="image" src="img/help.png" alt=" " width="0px" height="0px" style="float: left;" onFocus="focusToApplet()"/>

			<!-- help image -->
			<input type="image" src="img/help.png" alt="Hilfe" width="13px" height="18px" style="float: right;" id="helpimage"/>
    	
	  
	  </form>
	  
	
	  
      <script type="text/javascript">

        if (!deployJava.versionCheck('1.6.0_04+')) {
          document.write('<p>Diese Anwendung benÃ¶tigt Version 6 Update 4 oder hÃ¶her der <a href="" onclick="deployJava.installLatestJRE();">Java&trade; Laufzeitumgebung</a>.</p>');
        } else {
          var attributes = {
            codebase :'<%=codebase%>',
            code : '<%=appletClass%>',
            archive : '<%=appletArchive +".jar"%>',
            width : <%=width%>,
            height :<%=height%>,
            name : 'moccaapplet',
            id : 'moccaapplet'
          };
          var parameters = {
            GuiStyle : '<%=guiStyle%>',
            Locale : '<%=locale%>',
            Background : '<%=backgroundImg%>',
            BackgroundColor : '<%=backgroundColor%>',
            WSDL_URL : '../stal;jsessionid=<%=sessionId%>?wsdl',
            HelpURL : '../help/',
            SessionID : '<%=sessionId%>',
            RedirectURL : '../bkuResult',
            RedirectTarget: '_parent',
            EnforceRecommendedPINLength: 'true'
          };
          deployJava.runApplet(attributes, parameters, '1.6.0_04');
        }
				
      </script>

		

	  </div>
	  
    </body>
    
    <script>


    
		function focusToBrowser() {
	
			// put focus to window
			// focus can be assigned to any focusable field on the embedding website
			
			//alert("Put focus to browser..");
			//self.focus();
			document.getElementById("helpimage").focus();
	
		}
    
		function focusToApplet() {
			
			//alert('try to set focus to applet');
			if (document != null && document.moccaapplet != null) {
	
				//alert('set focus to applet.');
				document.moccaapplet.getFocusFromBrowser();						
				return true;					
			}
		}


		function increaseSize() {

			var appwidth = document.moccaapplet.width;
			var appheight = document.moccaapplet.height;

			document.moccaapplet.width = (appwidth * 1.2);
	    	document.moccaapplet.height = (appheight * 1.2);
			
			document.getElementById("container").style.width = (appwidth * 1.2);
			
			// TODO: This does not work in firefox, seems that width and height attributes cannot be read
			//       and set from input of type image
			var image_width = document.getElementById("increase_image").width;
			var image_height = document.getElementById("increase_image").height;


			document.getElementById("helpimage").width = (image_width * 1.2);
			document.getElementById("helpimage").height = (image_height * 1.2);

			document.getElementById("increase_image").width = (image_width * 1.2);
			document.getElementById("increase_image").height = (image_height * 1.2);

			document.getElementById("decrease_image").width = (image_width * 1.2);
			document.getElementById("decrease_image").height = (image_height * 1.2);


		}

		function decreaseSize() {

			var appwidth = document.moccaapplet.width;
			var appheight = document.moccaapplet.height;

			document.moccaapplet.width = (appwidth * 0.8333333333333);
	    	document.moccaapplet.height = (appheight * 0.8333333333333);

			document.getElementById("container").style.width = (appwidth * 0.8333333333333);

			// TODO: This does not work in firefox, seems that width and height attributes cannot be read
			//       and set from input of type image
			var image_width = document.getElementById("increase_image").width;
			var image_height = document.getElementById("increase_image").height;


			document.getElementById("helpimage").width = (image_width * 0.8333333333333);
			document.getElementById("helpimage").height = (image_height * 0.8333333333333);

			document.getElementById("increase_image").width = (image_width * 0.8333333333333);
			document.getElementById("increase_image").height = (image_height * 0.8333333333333);

			document.getElementById("decrease_image").width = (image_width * 0.8333333333333);
			document.getElementById("decrease_image").height = (image_height * 0.8333333333333);


		}

    </script>
    
</html>
