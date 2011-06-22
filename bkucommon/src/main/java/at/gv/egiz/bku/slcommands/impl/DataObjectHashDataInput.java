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


package at.gv.egiz.bku.slcommands.impl;

import at.gv.egiz.bku.binding.HttpUtil;
import at.gv.egiz.bku.slcommands.impl.xsect.DataObject;
import at.gv.egiz.stal.HashDataInput;
import java.io.InputStream;

/**
 * DataObject-backed HashDataInput
 * If <a href="XMLSignContext.html#Supported Properties">reference caching</a> is enabled,
 * the hashdata input stream can be obtained repeatedly.
 * @author clemens
 */
public class DataObjectHashDataInput implements HashDataInput {

  protected DataObject dataObject;

    public DataObjectHashDataInput(DataObject dataObject) {
      if (dataObject.getReference() == null) 
        throw new NullPointerException("DataObject reference must not be null");
      this.dataObject = dataObject;
    }
    
    @Override
    public String getReferenceId() {
      return dataObject.getReference().getId();
    }

    @Override
    public String getMimeType() {
      String contentType = dataObject.getMimeType();
      return contentType.split(";")[0].trim();
    }

    /**
     * may be called repeatedly
     * @return the pre-digested input stream if reference caching is enabled, null otherwise
     */
    @Override
    public InputStream getHashDataInput() {
        return dataObject.getReference().getDigestInputStream();
    }

    @Override
    public String getEncoding() {
      return HttpUtil.getCharset(dataObject.getMimeType(), false);
    }

    @Override
    public String getFilename() {
      //TODO obtain filename from dataObject, if not set return null or get filename (extension!) from mimetype
      return dataObject.getFilename();
    }

}
