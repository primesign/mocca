package at.gv.egiz.bku.online.webapp;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.binding.BindingProcessor;
import at.gv.egiz.bku.binding.Id;
import at.gv.egiz.bku.binding.IdFactory;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;
import at.gv.egiz.bku.utils.StreamUtil;
import at.gv.egiz.stal.HashDataInput;
import at.gv.egiz.stal.STAL;
import at.gv.egiz.stal.service.impl.STALRequestBroker;
import at.gv.egiz.stal.service.impl.STALRequestBrokerImpl;
import at.gv.egiz.stal.service.impl.STALServiceImpl;

public class HashDataInputServlet extends SpringBKUServlet {

  private static Log log = LogFactory.getLog(HashDataInputServlet.class);

  public HashDataInputServlet() {
  }

  private STALRequestBroker getSTAL(Id id) {
    BindingProcessor bp = getBindingProcessorManager().getBindingProcessor(id);
    if (bp == null) {
      return null;
    }
    STAL stal = bp.getSTAL();
    if (stal instanceof STALRequestBroker) {
      return (STALRequestBroker) stal;
    } else {
      throw new SLRuntimeException("Unexpected STAL type");
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    if ((req.getSession() == null) && (req.getSession().getId() != null)) {
      log.warn("Got request for hashdatainput without session info");
      resp.sendRedirect("expired.html");
      return;
    }
    Id sessionId = IdFactory.getInstance().createId(req.getSession().getId());
    log.debug("Got request for hashdata for session " + sessionId);
    STALRequestBroker rb = getSTAL(sessionId);
    if (rb == null) {
      log.info("STAL instance not found for session: " + sessionId);
      resp.sendRedirect("expired.html");
      return;
    }
    List<HashDataInput> hdi = rb.getHashDataInput();
    log.debug("Got hashdata list with " + hdi.size() + " entries");
    String param = req.getParameter("number");
    int num = 0;
    if (param != null) {
      log.debug("Got request for hashdata#" + num);
      num = Integer.parseInt(param);
    }
    if ((hdi.size()  <= num) || (num < 0)){
      log.warn("Requested hashdatainput exceeds listsize");
      resp.sendError(-1);
      return;
    }
    resp.setCharacterEncoding(req.getCharacterEncoding());
    resp.setContentType(hdi.get(num).getMimeType());
    String charSet = req.getCharacterEncoding();
    if (charSet == null) {
      charSet = "UTF-8";
    }
    Reader r = new InputStreamReader(hdi.get(num).getHashDataInput(), charSet);
    Writer w = new OutputStreamWriter(resp.getOutputStream(), charSet);
    StreamUtil.copyStream(r, w);
    w.close();
    return;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doGet(req, resp);
  }

}
