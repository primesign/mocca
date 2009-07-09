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
<%@ page import="java.io.File"%>
<%
    StringBuilder path = new StringBuilder("/helpfiles/");

    //servlet mapping assures pathInfo[0] == help
    //expect pathinfo /help/<languagecode>/<helpfile>
    String pathInfo[] = (request.getPathInfo() != null) ? request
      .getPathInfo().split("/") : new String[] {};
    if (pathInfo.length < 2) {
      path.append("index.html");
    } else {

      String language = "de";
      if (pathInfo.length > 2 && (new File("/helpfiles/"  +
              pathInfo[1].split("_")[0].toLowerCase())).isDirectory()) {
        language = pathInfo[1].split("_")[0];
      }
      path.append(language);
      path.append('/');
      
      String filename = pathInfo[(pathInfo.length > 2) ? 2 : 1];
      path.append(filename);
    }
    System.out.println(path);
%>

<jsp:include page="<%=path.toString()%>" flush="true"/>
