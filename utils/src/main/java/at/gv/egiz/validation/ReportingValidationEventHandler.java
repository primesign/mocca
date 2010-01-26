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
package at.gv.egiz.validation;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class ReportingValidationEventHandler implements ValidationEventHandler {

  protected static final Log log = LogFactory.getLog(ReportingValidationEventHandler.class);
  
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
