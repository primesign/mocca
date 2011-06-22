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


package at.gv.egiz.validation;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class ReportingValidationEventHandler implements ValidationEventHandler {

  private final Logger log = LoggerFactory.getLogger(ReportingValidationEventHandler.class);
  
  protected ValidationEvent errorEvent;

  /**
   *
   * @param event
   * @return false, terminate the current unmarshal, validate, or marshal operation after handling this warning/error
   *   (except for WARNING validation events)
   */
  @Override
  public boolean handleEvent(ValidationEvent event) {
    switch (event.getSeverity()) {
      case ValidationEvent.WARNING:
        log.info(event.getMessage());
        return true;
      case ValidationEvent.ERROR:
        log.warn(event.getMessage());
        errorEvent = event;
        return false;
      case ValidationEvent.FATAL_ERROR:
        log.error(event.getMessage());
        errorEvent = event;
        return false;
      default:
        log.debug(event.getMessage());
        return false;
    }
  }

  public ValidationEvent getErrorEvent() {
    return errorEvent;
  }
  
}
