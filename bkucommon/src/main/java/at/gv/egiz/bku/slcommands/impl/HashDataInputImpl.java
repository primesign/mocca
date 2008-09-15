/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.gv.egiz.bku.slcommands.impl;

import at.gv.egiz.bku.binding.HttpUtil;
import at.gv.egiz.bku.slcommands.impl.xsect.DataObject;
import at.gv.egiz.stal.HashDataInput;
import java.io.InputStream;

/**
 *
 * @author clemens
 */
public class HashDataInputImpl implements HashDataInput {

    String refId;
    String mimeType;
    String encoding;
    InputStream hashDataInput;

    public HashDataInputImpl(DataObject dataObject) {
        refId = dataObject.getReference().getId();
        String contentType = dataObject.getMimeType();
        mimeType = contentType.split(";")[0].trim();
        encoding = HttpUtil.getCharset(dataObject.getMimeType(), false);
        hashDataInput = dataObject.getReference().getDigestInputStream();
    }
    
    @Override
    public String getReferenceId() {
        return refId;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public InputStream getHashDataInput() {
        return hashDataInput;
    }

    @Override
    public String getEncoding() {
      return encoding;
    }

}
