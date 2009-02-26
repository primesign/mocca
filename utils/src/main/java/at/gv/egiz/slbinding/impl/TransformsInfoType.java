/*
* Copyright 2008 Federal Chancellery Austria and
* Graz University of Technology
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.gv.egiz.slbinding.impl;

import at.gv.egiz.slbinding.*;
import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author clemens
 */
public class TransformsInfoType extends at.buergerkarte.namespaces.securitylayer._1.TransformsInfoType implements RedirectCallback {

    @XmlTransient
    private static Log log = LogFactory.getLog(TransformsInfoType.class);
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
        log.debug("enabling event redirection for TransformsInfoType");
        redirectOS = new ByteArrayOutputStream();
        filter.setRedirectStream(redirectOS, redirectTriggers); 
    }

    @Override
    public void disableRedirect(RedirectEventFilter filter) throws XMLStreamException {
        log.debug("disabling event redirection for TransformsInfoType");
        filter.flushRedirectStream();
        filter.setRedirectStream(null);
    }

    @Override
    public ByteArrayOutputStream getRedirectedStream() {
        return redirectOS;
    }
}
