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
package at.gv.egiz.slbinding;

import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enables event redirection before marshalling a target of type RedirectCallback.
 * It is up to the target class to implement the redirection (default implementation in RedirectCallback).
 * Disables event redirection after marshalling (when the closing tag occurs).
 * @author clemens
 */
public class RedirectUnmarshallerListener extends Unmarshaller.Listener {

    private final Logger log = LoggerFactory.getLogger(RedirectUnmarshallerListener.class);
    protected RedirectEventFilter eventFilter;

    public RedirectUnmarshallerListener(RedirectEventFilter eventFilter) {
        this.eventFilter = eventFilter;
    }

    @Override
    public void beforeUnmarshal(Object target, Object parent) {
        if (target instanceof RedirectCallback) {
            try {
                ((RedirectCallback) target).enableRedirect(eventFilter);
            } catch (XMLStreamException ex) {
                log.error("failed to enable event redirection for " + target.getClass().getName() + ": " + ex.getMessage(), ex);
            }
        }
        if (target instanceof NamespaceContextCallback) {
            ((NamespaceContextCallback) target).preserveNamespaceContext(eventFilter);
        }
    }

    @Override
    public void afterUnmarshal(Object target, Object parent) {
        if (target instanceof RedirectCallback) {
            try {
                ((RedirectCallback) target).disableRedirect(eventFilter);
            } catch (XMLStreamException ex) {
                log.error("failed to disable event redirection for " + target.getClass().getName() + ": " + ex.getMessage(), ex);
            }
        }
    }
}
