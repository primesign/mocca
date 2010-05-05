/*
* Copyright 2009 Federal Chancellery Austria and
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
