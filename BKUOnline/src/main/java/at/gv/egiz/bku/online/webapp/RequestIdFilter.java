package at.gv.egiz.bku.online.webapp;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.MDC;

import at.gv.egiz.bku.binding.Id;
import at.gv.egiz.bku.binding.IdFactory;

/**
 * Servlet Filter implementation class BindingProcessorFilter
 */
public class RequestIdFilter implements Filter {
  
  /**
   * @see Filter#destroy()
   */
  public void destroy() {
  }

  /**
   * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
   */
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {

    if (request instanceof HttpServletRequest) {
      HttpSession session = ((HttpServletRequest) request).getSession();
      Id id = IdFactory.getInstance().createId(session.getId());
      MDC.put("id", id.toString());
      request.setAttribute("id", id);
    }

    // pass the request along the filter chain
    try {
      chain.doFilter(request, response);
    } finally {
      MDC.remove("id");
    }
    
  }

  /**
   * @see Filter#init(FilterConfig)
   */
  public void init(FilterConfig fConfig) throws ServletException {
  }

}
