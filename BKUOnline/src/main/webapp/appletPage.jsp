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
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>MOCCA Appletpage</title>
<link rel="shortcut icon" href="img/favicon.ico" type="image/x-icon">
</head>
<body>
<script src="./js/deployJava.js"></script>
<script>
	if (!deployJava.versionCheck('1.6.0_02')) {
		document
				.write('<b>Diese Anwendung benötigt die Java Platform Version 1.6.0_02 oder höher.</b>' + '<input type="submit" value="Java Platform 1.6.0_02 installieren" onclick="deployJava.installLatestJRE();">');
	} else {
		var attributes = {
			codebase :'applet',
			code :'at.gv.egiz.bku.online.applet.BKUApplet.class',
			archive :'BKUApplet-1.0-SNAPSHOT.jar, commons-logging-1.1.1.jar, iaik_jce_me4se-3.04.jar',
			width :400,
			height :200
		};
		var parameters = {
			WSDL_URL :'../stal?wsdl',
			SessionID : '<%= session.getId() %>',
			redirectURL : '../bkuResult'
		};
		var version = '1.6.0_02';
		deployJava.runApplet(attributes, parameters, version);
	}
</script>
<br />
<hr/>
Powered by <img src="img/mocca-t_s.png" alt="mocca-logo" width="100">
</body>
</html>
