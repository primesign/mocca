<!--
  Copyright 2011 by Graz University of Technology, Austria
  MOCCA has been developed by the E-Government Innovation Center EGIZ, a joint
  initiative of the Federal Chancellery Austria and Graz University of Technology.

  Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
  the European Commission - subsequent versions of the EUPL (the "Licence");
  You may not use this work except in compliance with the Licence.
  You may obtain a copy of the Licence at:
  http://www.osor.eu/eupl/

  Unless required by applicable law or agreed to in writing, software
  distributed under the Licence is distributed on an "AS IS" basis,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the Licence for the specific language governing permissions and
  limitations under the Licence.

  This product combines work with different licenses. See the "NOTICE" text
  file for details on the various modules and licenses.
  The "NOTICE" text file is part of the distribution. Any derivative works
  that you distribute must include a readable copy of the "NOTICE" text file.
-->
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<c:set var="defaultWidth" value="190"/>
<c:set var="defaultHeight" value="130"/>
<%-- URLs --%>
<c:url value="/DataURLServer" var="dataUrl"/>
<%-- Messages --%>
<fmt:bundle basename="web">
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
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
        width: <c:out value="${requestScope.moccaParam.appletWidth}" default="${defaultWidth}"/>px;
        height: <c:out value="${requestScope.moccaParam.appletHeight}" default="${defaultHeight}"/>px;
      }
      #container {
        padding: 0;
        margin: 0;
        width: <c:out value="${requestScope.moccaParam.appletWidth}" default="${defaultWidth}"/>px;
        background: <c:out value="${requestScope.moccaParam.appletBackgroundColor}" default="#eeeeee"/>;
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
  </head>
  <body>
    <div id="container">
      <form action="http://localhost:3495/http-security-layer-request" method="post" enctype="application/x-www-form-urlencoded">
        <input name="SessionID_" value="${requestScope.id}" type="hidden"/>
        <input name="DataURL" value="${dataUrl}" id="DataURL" type="hidden"/>
        <input name="XMLRequest" value="<NullOperationRequest xmlns='http://www.buergerkarte.at/namespaces/securitylayer/1.2#'/>" type="hidden"/>
        <div id="message">
          <p><fmt:message key="local"/></p>
          <p style="text-align: right;">
            <input type="submit" style="vertical-align: middle"/>
          </p>
        </div>
      </form>
    </div>
  </body>
</html>
</fmt:bundle>
