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


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.gv.egiz.slbinding.impl;

import at.gv.egiz.slbinding.*;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author clemens
 */
public class TransformsInfoType extends at.buergerkarte.namespaces.securitylayer._1_2_3.TransformsInfoType implements RedirectCallback {

    @XmlTransient
    private final Logger log = LoggerFactory.getLogger(TransformsInfoType.class);
    @XmlTransient
    private static final Set<QName> redirectTriggers = initRedirectTriggers(); 
    @XmlTransient
    protected ByteArrayOutputStream redirectOS = null;

    private static Set<QName> initRedirectTriggers() {
        HashSet<QName> dsigTransforms = new HashSet<QName>();
        dsigTransforms.add(new QName("http://www.w3.org/2000/09/xmldsig#", "Transforms"));
        return dsigTransforms;
    }
    
    @Override
    public void enableRedirect(RedirectEventFilter filter) throws XMLStreamException {
        log.trace("enabling event redirection for TransformsInfoType");
        redirectOS = new ByteArrayOutputStream();
        filter.setRedirectStream(redirectOS, redirectTriggers); 
    }

    @Override
    public void disableRedirect(RedirectEventFilter filter) throws XMLStreamException {
        log.trace("disabling event redirection for TransformsInfoType");
        filter.flushRedirectStream();
        filter.setRedirectStream(null);
        if (log.isTraceEnabled()) {
          try {
            log.trace("redirected events (UTF-8): " + redirectOS.toString("UTF-8"));
          } catch (UnsupportedEncodingException ex) {
            log.error("failed to log redirected events", ex);
          }
        }
    }

    @Override
    public ByteArrayOutputStream getRedirectedStream() {
        return redirectOS;
    }
}
