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


package at.gv.egiz.bku.smccstal;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iaik.me.asn1.ASN1;

public class IdentityLinkExtractor {
	
	private static final Logger log = LoggerFactory.getLogger(IdentityLinkExtractor.class);
	
	private static ASN1 getData(ASN1 identityLink) throws IOException
	{
		if(identityLink.getSize() > 4 )
		{
			ASN1 data = identityLink.getElementAt(4).gvASN1();
			if(data.getTagClass() != 0)
			{
				log.error("CorporateBodyData currently not supported.");
				return null;
			}
			return data;
		}
		else
		{
			log.error("IdentityLink ASN1 invalid size = " + identityLink.getSize());
			return null;
		}
	}  
	
	public static String getFirstName(ASN1 identityLink) throws IOException
	{
		ASN1 personData = getData(identityLink);
		if(personData != null)
		{
			return personData.getElementAt(1).gvString();
		}
		return null;
	}
	
	public static String getLastName(ASN1 identityLink) throws IOException
	{
		ASN1 personData = getData(identityLink);
		if(personData != null)
		{
			return personData.getElementAt(2).gvString();
		}
		return null;
	}
	
	public static String getDateOfBirth(ASN1 identityLink) throws IOException
	{
		ASN1 personData = getData(identityLink);
		if(personData != null)
		{
			return personData.getElementAt(3).gvString();
		}
		return null;
	}
}
