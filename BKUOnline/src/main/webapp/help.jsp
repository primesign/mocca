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
<%@ page import="java.util.Locale"%>
<%
    StringBuilder path = new StringBuilder("/helpfiles");

    //servlet mapping assures pathInfo[0] == help
    //expect pathinfo /help/<languagecode>/<helpfile>
    String pathInfo[] = (request.getPathInfo() != null) ? request
      .getPathInfo().split("/") : new String[] {};
    if (pathInfo.length < 2) {
      path.append("/index.html");
    } else {
      String language = "de";
      //System.out.println("locale " + pathInfo[1] + ": " + pathInfo[1].substring(0, 2).toLowerCase()); //new Locale(pathInfo[1]).getLanguage());
      //System.out.println("is dir: " + new File("/helpfiles/de").isDirectory());
              //+ pathInfo[1].substring(0, 2).toLowerCase()).isDirectory());
      if (pathInfo.length > 2 && new File("/helpfiles/"  // + new Locale(pathInfo[1]).getLanguage()).isDirectory())) {
              + pathInfo[1].substring(0, 2).toLowerCase()).isDirectory()) {
        System.out.println("locale " + new Locale(pathInfo[1]));
        language = new Locale(pathInfo[1]).getLanguage();
      }
      path.append('/');
      path.append(language);
      
      String filename = pathInfo[(pathInfo.length > 2) ? 2 : 1];
      path.append('/');
      path.append(filename);
    }
    System.out.println(path);
%>

<jsp:include page="<%=path.toString()%>" flush="true"/>
