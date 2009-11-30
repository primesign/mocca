package moaspss;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import at.gv.egiz.bku.utils.URLEncodingWriter;

public class SLClient {
  
  private static class JAXBContextHolder {
    
    private static JAXBContext context;

    {
      String slPkg = at.buergerkarte.namespaces.securitylayer._1.ObjectFactory.class.getPackage().getName();
      String xmldsigPkg = org.w3._2000._09.xmldsig_.ObjectFactory.class.getPackage().getName();
      String samlPkg = oasis.names.tc.saml._1_0.assertion.ObjectFactory.class.getPackage().getName();
      String prPkg = at.buergerkarte.namespaces.personenbindung._20020506_.ObjectFactory.class.getPackage().getName();
      try {
          context = JAXBContext.newInstance(slPkg + ":" + xmldsigPkg + ":" + samlPkg + ":" + prPkg);
      } catch (JAXBException e) {
          throw new RuntimeException("Failed to setup JAXBContext.", e);
      }
    }
    
  }
  
  public static JAXBContext getJAXBContext() {
    return JAXBContextHolder.context;
  }
	
  private URL slUrl;

  private URL slUrlSSL;

  private boolean useSSL = false;

  public SLClient() {
    try {
      slUrl = new URL("http://localhost:3495/http-security-layer-request");
      slUrlSSL = new URL("https://localhost:3496/https-security-layer-request");
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  public Object submitRequest(Element request, Class<?> responseType)
      throws SLException, TransformerException, IOException {
    	
    URL url = (useSSL) ? slUrlSSL : slUrl;

    HttpURLConnection connection;
    int responseCode;
    try {
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      connection.setDoOutput(true);
      connection.setDoInput(true);
      connection.connect();

      OutputStream outputStream = connection.getOutputStream();
      OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream, "ISO-8859-1");
      streamWriter.write("XMLRequest=");
      URLEncodingWriter urlEnc = new URLEncodingWriter(streamWriter);

      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer transformer = tf.newTransformer();
      Source source = new DOMSource(request);
      Result result = new StreamResult(urlEnc);
      transformer.transform(source, result);
      urlEnc.flush();
      streamWriter.flush();
      outputStream.close();
      responseCode = connection.getResponseCode();

    } catch (ProtocolException e) {
      throw new RuntimeException(e);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }

    if (responseCode == 200) {
      String[] contentType = connection.getContentType().split(";", 2);

      if ("text/xml".equals(contentType[0])) {

        Reader streamReader;
        try {
          InputStream inputStream = connection.getInputStream();

          String charset = "ISO-8859-1";
          if (contentType.length > 1
              && (contentType[1].trim()).startsWith("charset=")) {
            charset = contentType[1].split("=", 2)[1];
          }

          streamReader = new InputStreamReader(inputStream, charset);
        } catch (UnsupportedEncodingException e) {
          throw new RuntimeException(e);
        }

        if (JAXBElement.class.isAssignableFrom(responseType)) {
          Object obj;
          try {
            Unmarshaller unmarshaller = getJAXBContext().createUnmarshaller();
            obj = unmarshaller.unmarshal(streamReader);
          } catch (JAXBException e) {
            throw new SLException(9000, e);
          }
          if (obj instanceof JAXBElement<?>) {
            return obj;
          } else {
            throw new SLException(9000, "Got unexpected response.");
          }
        } else if (Element.class.isAssignableFrom(responseType)) {
          DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
          dbf.setNamespaceAware(true);
          Document doc;
          try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(new InputSource(streamReader));
          } catch (ParserConfigurationException e) {
            throw new SLException(9000, e);
          } catch (SAXException e) {
            throw new SLException(9000, e);
          } catch (IOException e) {
            throw new SLException(9000, e);
          }
          return doc.getDocumentElement();
        } else {
          throw new SLException(9000, "Unsupported response type "
              + responseType);
        }

      } else {
        throw new SLException(9000, "Got unexpected content type "
            + contentType + ".");
      }
    } else {
      throw new SLException(9000, "Got unexpected response code "
          + responseCode + ".");
    }

  }
	
}
