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
package at.gv.egiz.smcc.ccid;

import javax.smartcardio.Card;
import javax.smartcardio.CardTerminal;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Fails with ACOS cards (Problem might be 'short' VERIFY which is not supported by ACOS)
 * TODO
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class OMNIKEYCardMan3621 extends DefaultReader {

  protected static final Log log = LogFactory.getLog(OMNIKEYCardMan3621.class);
  
  public OMNIKEYCardMan3621(Card icc, CardTerminal ct) {
    super(icc, ct);
    log.warn("This card reader does not support ACOS cards.");
    log.debug("TODO: fall back to software pin entry");
  }
}
