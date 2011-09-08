<%
  String redirectURL = pageContext.getServletContext().getContextPath();
  if (pageContext.getErrorData().getRequestURI().contains("/help/"))
    redirectURL += "/help/404h.html";
  else
    redirectURL += "/help/404.html";

  response.sendRedirect(redirectURL);
%>
