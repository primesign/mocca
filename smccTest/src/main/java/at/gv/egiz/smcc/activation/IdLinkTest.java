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



package at.gv.egiz.smcc.activation;

import iaik.asn1.ASN1;
import iaik.asn1.ASN1Object;
import iaik.asn1.CodingException;
import iaik.asn1.DerCoder;
import iaik.utils.Base64InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author clemens
 */
public class IdLinkTest {

    public static final String IdLink = "MIICCAIBAQwmaHR0cDovL3d3dy5hLXRydXN0LmF0L3ptci9wZXJzYjIwNC54c2wMKXN6ci5ibWkuZ3YuYXQtQXNzZXJ0aW9uSUQxMjY5MzM5ODczNzM2MjY1DBkyMDEwLTAzLTIzVDExOjI0OjMzKzAxOjAwoDkwNwwYbys5TmQ4bHQ1a09SdUtuaFhUK0h3UT09DANNYXgMCk11c3Rlcm1hbm4MCjE5NDAtMDEtMDEwCqADAgEAoAMCAQEDggEBACkl33tK+BTbHi842hSYTpcn87GH0FNayE70z7Ohg4BDB84hc3eM6e8Nslt2nTMnjInf1sJzJYRVDMopE8VVIDmtSoGnz9Tu5U06tuIM87Lv9tgGUzDqn9fdZIyWs9eTJHHYOIwh16ISvgswKV1iioE55g5GaW3G+fCTbgdq5I0TuE4jRP7YwMXLtLulRes1e3Y4Hvv3PgACDkafzZVbWlD9WpK/eCQ75YTBev9KM0YWJ/w3yaWAMoukI2c2H1T4nmc1/3b/1q+XzuEQPuugDYPbV4myD55/W0J6WqOKA2e3NfWswPqxfr1TLcoR+TZ/18BYrNv2s1ZyQ5ZrZ164UKKgFwMVAN6gGAcGTABXOTAw5MDMsVeS7Tj0oRcDFQARNKXtL0cSdwP8Co74fdURaskUzqIXAxUAlWlQLXBfizrWKBahBj4IcJ+xUl4=";

    protected void decodeIdLink(InputStream idLinkDER) throws UnsupportedEncodingException, IOException, CodingException {
        ASN1Object idLink = DerCoder.decode(idLinkDER);
        System.out.println(ASN1.print(idLink));
    }

    public static void main(String[] args) throws UnsupportedEncodingException, IOException, CodingException {
        
        IdLinkTest test = new IdLinkTest();
        test.decodeIdLink(new Base64InputStream(new ByteArrayInputStream(IdLink.getBytes())));
        
    }

}

