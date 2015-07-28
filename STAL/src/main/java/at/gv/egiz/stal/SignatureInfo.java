package at.gv.egiz.stal;

import java.util.ArrayList;
import java.util.List;

import at.gv.egiz.stal.signedinfo.ReferenceType;
import at.gv.egiz.stal.signedinfo.SignatureMethodType;
import at.gv.egiz.stal.signedinfo.SignedInfoType;


/*
 * Copyright 2015 Datentechnik Innovation GmbH and Prime Sign GmbH, Austria
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

/**
 * @author szoescher This class wraps a {@link SignatureInfo} and adds the
 *         additional parameters displayName and mimeType;
 */
public class SignatureInfo {

  private SignedInfoType signedInfo;

  private String displayName;

  private String mimeType;

  public SignatureInfo(SignedInfoType signedInfo, String displayName, String mimeType) {
    this.signedInfo = signedInfo;
    this.displayName = displayName;
    this.mimeType = mimeType;
  }

  public SignedInfoType getSignedInfo() {
    return signedInfo;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getMimeType() {
    return mimeType;
  }

  public SignatureMethodType getSignatureMethod() {
    if (signedInfo != null) {
      return signedInfo.getSignatureMethod();
    }
    return null;
  }

  public String getId() {
    if (signedInfo != null) {
      return signedInfo.getId();
    }
    return null;
  }

  public List<ReferenceType> getReference() {

    if (signedInfo != null && signedInfo.getReference() != null) {

      return signedInfo.getReference();
    }
    return new ArrayList<ReferenceType>();
  }
}
