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



package at.gv.egiz.mocca.id;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import at.gv.e_government.reference.namespace.persondata._20020228_.IdentificationType;
import at.gv.e_government.reference.namespace.persondata._20020228_.PhysicalPersonType;

public class IdLinkPersonData {

  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  
  protected PersonalIdentifier identifier;
  
  protected String familyName;
  
  protected String givenName;
  
  protected Date dateOfBirth;

  public IdLinkPersonData(PhysicalPersonType physicalPerson) throws ParseException {
    familyName = physicalPerson.getName().getFamilyName().get(0).getValue();
    givenName = physicalPerson.getName().getGivenName().get(0);
    dateOfBirth = DATE_FORMAT.parse(physicalPerson.getDateOfBirth());
    IdentificationType identificationType = physicalPerson.getIdentification().get(0);
    if (identificationType != null) {
      identifier = new PersonalIdentifier(identificationType.getType(),
          identificationType.getValue().getValue());
    }
  }

  public String getGivenName() {
    return givenName;
  }

  public String getFamilyName() {
    return familyName;
  }

  public Date getDateOfBirth() throws ParseException {
    return dateOfBirth;
  }
  
  public PersonalIdentifier getIdentifier() {
    return identifier;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return familyName + ", " + givenName + ", " + DATE_FORMAT.format(dateOfBirth);
  }
  
  
}
