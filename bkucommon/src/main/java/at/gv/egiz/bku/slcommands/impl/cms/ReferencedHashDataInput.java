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

package at.gv.egiz.bku.slcommands.impl.cms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;

import at.buergerkarte.namespaces.securitylayer._1_2_3.ExcludedByteRangeType;
import at.gv.egiz.bku.utils.urldereferencer.URLDereferencer;

public class ReferencedHashDataInput extends CMSHashDataInput {

  private String urlReference;
  private URLDereferencer urlDereferencer;
  private ExcludedByteRangeType excludedByteRange;
  
	public ReferencedHashDataInput(String mimeType, URLDereferencer urlDereferencer, String urlReference, ExcludedByteRangeType excludedByteRange) {
		super(null, mimeType);	
		this.urlDereferencer = urlDereferencer;
		this.urlReference = urlReference;
		this.excludedByteRange = excludedByteRange;
	}
 	
	
	public URLDereferencer getUrlDereferencer() {
		return urlDereferencer;
	}


	public void setUrlDereferencer(URLDereferencer urlDereferencer) {
		this.urlDereferencer = urlDereferencer;
	}

	@Override
	public InputStream getHashDataInput() throws IOException {

		InputStream hashDataInputStream = urlDereferencer.dereference(urlReference).getStream();

		try {
			byte[] content = IOUtils.toByteArray(hashDataInputStream);

			if (excludedByteRange != null) {

				int from = excludedByteRange.getFrom().intValue();
				int to = excludedByteRange.getTo().intValue();

				byte[] signedContent = ArrayUtils.addAll(ArrayUtils.subarray(content, 0, from), ArrayUtils.subarray(content, to, content.length));

				return new ByteArrayInputStream(signedContent);

			} else {
				return new ByteArrayInputStream(content);
			}

		} finally {
			hashDataInputStream.close();
		}
	}
}
