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

<%
    StringBuilder path = new StringBuilder("/helpfiles/");

    //servlet mapping assures pathInfo[0] == help
    //expect pathinfo /help/<languagecode>/<helpfile>
    String pathInfo[] = (request.getPathInfo() != null) ? request
      .getPathInfo().split("/") : new String[] {};
    if (pathInfo.length < 2) {
      path.append("index.html");
    } else {
      if (pathInfo.length > 2) {
        //new Locale(pathInfo[1]).getLanguage() returns de_at
        //anyway, Locale uses _two-letter_ codes as defined by ISO-639
        String language = pathInfo[1].substring(0, 2).toLowerCase();
        if(getServletContext().getResource(path.toString() + language) != null) {
          //System.out.println("help available for requested language " + language);
          path.append(language);
          path.append('/');
        } else {
          //System.out.println("no help available for requested language " + language);
          path.append("de/");
        }
        path.append(pathInfo[2]);
      } else {
        //System.out.println("no language requested");
        path.append("de/");
        path.append(pathInfo[1]);
      }
    }
    //System.out.println(path);
%>

<jsp:include page="<%=path.toString()%>" flush="true"/>
