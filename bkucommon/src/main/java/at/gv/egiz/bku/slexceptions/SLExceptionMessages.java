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


package at.gv.egiz.bku.slexceptions;

public final class SLExceptionMessages {

  private SLExceptionMessages() {
  }

  public static final String STANDARD_PREFIX = "ec";
  
  //
  // 3xxx
  //
  // Error in the XML structure of the command request
  
  public static final String EC3000_UNCLASSIFIED = "ec3000.unclassified";
  
  public static final String EC3002_INVALID = "ec3002.invalid";

  //
  // 4xxx
  //
  // Error during command execution
  
  public static final String EC4000_UNCLASSIFIED_INFOBOX_INVALID = "ec4000.infobox.invalid";
  
  public static final String EC4000_UNCLASSIFIED_IDLINK_TRANSFORMATION_FAILED = "ec4000.idlink.transfomation.failed";
  
  public static final String EC4002_INFOBOX_UNKNOWN = "ec4002.infobox.unknown";
  
  public static final String EC4003_NOT_RESOLVED = "ec4003.not.resolved";
  
  public static final String EC4011_NOTIMPLEMENTED = "ec4011.notimplemented";
  
  //
  // Legacy error codes
  //
  
  public static final String LEC2901_NOTIMPLEMENTED = "lec2901.notimplemented";
  
}
