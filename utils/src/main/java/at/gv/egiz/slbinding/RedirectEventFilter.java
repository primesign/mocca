/*
 * Copyright 2011 by Graz University of Technology, Austria
 * MOCCA has been developed by the E-Government Innovation Center EGIZ, a joint
 * initiative of the Federal Chancellery Austria and Graz University of Technology.
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 */


package at.gv.egiz.slbinding;

import java.io.OutputStream;
import java.util.Set;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/*
 * TODO: don't set redirect stream from caller (caller does not know whether redirection will be triggered)
 * rather create on trigger and pass to caller
 */
public class RedirectEventFilter implements EventFilter {

  public static final String DEFAULT_ENCODING = "UTF-8";
  protected XMLEventWriter redirectWriter = null;
  protected Set<QName> redirectTriggers = null;
  private int depth = -1;
  protected NamespaceContext currentNamespaceContext = null;

  /**
   * Event redirection is disabled, set a redirect stream to enable.
   */
  public RedirectEventFilter() {
    redirectWriter = null;
  // redirectTriggers = null;
  }

  /**
   *
   * @param redirectStream
   *          if null, no events are redirected
   * @param redirectTriggers
   *          if null, all events are redirected
   */
  public RedirectEventFilter(OutputStream redirectStream, String encoding)
          throws XMLStreamException { // , List<QName> redirectTriggers
    if (redirectStream != null) {
      XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
      if (encoding == null) {
        encoding = DEFAULT_ENCODING;
      }
      this.redirectWriter = outputFactory.createXMLEventWriter(redirectStream,
              encoding);
    }
  // this.redirectTriggers = redirectTriggers;
  }

  /**
   * All startElement events occuring in the redirectTriggers list will trigger
   * redirection of the entire (sub-)fragment.
   *
   * @param event
   * @return false if an event is redirected
   */
  @Override
  public boolean accept(XMLEvent event) {
    int eventType = event.getEventType();

    if (eventType == XMLStreamConstants.START_ELEMENT) {
      //hopefully, this is a copy
      currentNamespaceContext = event.asStartElement().getNamespaceContext();
    }
    if (redirectWriter == null) {
      return true;
    }
    if (eventType == XMLStreamConstants.START_ELEMENT) {
      if (depth >= 0 || triggersRedirect(event.asStartElement().getName())) {
        depth++;
      }
    } else if (eventType == XMLStreamConstants.END_ELEMENT) {
      if (depth >= 0 && --depth < 0) {
        // redirect the end element of the trigger,
        // but do not redirect the end element of the calling type
        if (redirectTriggers != null) {
          redirectEvent(event);
          return false;
        }
      }
    }
    if (depth >= 0) { //|| (depth == 0 && redirectTriggers == null)) {
      redirectEvent(event);
      return false;
    }
    return true; // depth < 0;

//    switch (event.getEventType()) {
//    case XMLStreamConstants.START_ELEMENT:
//      StartElement startElt = event.asStartElement();
//      if (depth >= 0 || triggersRedirect(startElt.getName())) {
//        depth++;
//      }
//      // namespace context changes only on start elements
//      // (first event might not be startElement, but we don't need CDATA's
//      // namespace context)
//      currentNamespaceContext = startElt.getNamespaceContext();
//      break;
//    case XMLStreamConstants.END_ELEMENT:
//      // if depth switches from positive to negative, this is the closing tag of
//      // the trigger (redirect as well!)
//      if (depth >= 0 && --depth < 0) {
//        redirectEvent(event);
//        return false;
//      }
//      break;
//    }
//    if (depth >= 0) {
//      redirectEvent(event);
//      return false;
//    }
//    return true; // depth < 0;
  }

  /**
   * @param startElt
   * @return true if the set of triggers contains startElement
   * (or no triggers are registered, i.e. everything is redirected)
   */
  private boolean triggersRedirect(QName startElement) {
    if (redirectTriggers != null) {
      return redirectTriggers.contains(startElement);
    }
    return true;
  }

  private void redirectEvent(XMLEvent event) {
    try {
      redirectWriter.add(event);
    } catch (XMLStreamException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Enable/disable redirection of <em>all</em> events from now on.
   * The redirected events will be UTF-8 encoded and written to the stream.
   *
   * @param redirectstream
   *          if null, redirection is disabled
   */
  public void setRedirectStream(OutputStream redirectStream) throws XMLStreamException {
    setRedirectStream(redirectStream, DEFAULT_ENCODING, null);
  }

  /**
   * Enable/disable redirection of <em>all</em> events from now on.
   *
   * @param redirectStream if null, redirection is disabled
   * @param encoding The encoding for the redirect stream
   * @throws javax.xml.stream.XMLStreamException
   */
  public void setRedirectStream(OutputStream redirectStream, String encoding) throws XMLStreamException {
    setRedirectStream(redirectStream, encoding, null);
  }

  /**
   * Enable/disable redirection of all (child) elements contained in redirect triggers.
   * The redirected events will be UTF-8 encoded and written to the stream.
   *
   * @param redirectstream
   *          if null, redirection is disabled
   * @param redirectTriggers elements that trigger the redirection
   */
  public void setRedirectStream(OutputStream redirectStream, Set<QName> redirectTriggers) throws XMLStreamException {
    setRedirectStream(redirectStream, DEFAULT_ENCODING, redirectTriggers);
  }

  /**
   * Enable/disable redirection of all (child) elements contained in redirect triggers.
   *
   * TODO: don't set redirect stream from caller (caller does not know whether redirection will be triggered)
   * rather create on trigger and pass to caller
   * @param redirectstream
   *          if null, redirection is disabled
   * @param encoding The encoding for the redirect stream
   * @param redirectTriggers elements that trigger the redirection
   */
  public void setRedirectStream(OutputStream redirectStream, String encoding, Set<QName> redirectTriggers) throws XMLStreamException {
    if (redirectStream != null) {
      XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
      if (encoding == null) {
        encoding = DEFAULT_ENCODING;
      }
      redirectWriter = outputFactory.createXMLEventWriter(redirectStream,
              encoding);
      if (redirectTriggers == null) {
        // start redirecting
        depth = 0;
      }
      this.redirectTriggers = redirectTriggers;
    } else {
      redirectWriter = null;
      this.redirectTriggers = null;
    }
  }

  /**
   * Enable/disable redirection of fragments (defined by elements in
   * redirectTriggers)
   *
   * @param redirectStream
   *          if null, redirection is disabled
   * @param redirectTriggers
   *          All startElement events occuring in this list will trigger
   *          redirection of the entire fragment. If null, all events are
   *          redirected
   */
  // public void setRedirectStream(OutputStream redirectStream, List<QName>
  // redirectTriggers) throws XMLStreamException {
  // if (redirectStream != null) {
  // XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
  // redirectWriter = outputFactory.createXMLEventWriter(redirectStream);
  // } else {
  // redirectWriter = null;
  // }
  // this.redirectTriggers = (redirectStream == null) ? null : redirectTriggers;
  // }
  /**
   * flushes the internal EventWriter
   *
   * @throws javax.xml.stream.XMLStreamException
   */
  public void flushRedirectStream() throws XMLStreamException {
    redirectWriter.flush();
  }

  /**
   * the namespaceContext of the last startelement event read
   *
   * @return
   */
  public NamespaceContext getCurrentNamespaceContext() {
    return currentNamespaceContext;
  }
}
