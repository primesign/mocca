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
