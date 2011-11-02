<%
  String redirectURL = pageContext.getServletContext().getContextPath();
  if (pageContext.getErrorData().getRequestURI() != null &&
      pageContext.getErrorData().getRequestURI().contains("/help/"))
    redirectURL += "/help/de/404h.html";
  else
    redirectURL += "/help/de/404.html";

  response.sendRedirect(redirectURL);
%>
