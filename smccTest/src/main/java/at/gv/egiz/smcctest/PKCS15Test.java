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


package at.gv.egiz.smcctest;

import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.VerifyAPDUSpec;
import at.gv.egiz.smcc.util.ISO7816Utils;
import at.gv.egiz.smcc.util.TLV;
import at.gv.egiz.smcc.util.TLVSequence;
import iaik.asn1.ASN1;
import iaik.asn1.ASN1Object;
import iaik.asn1.CodingException;
import iaik.asn1.DerCoder;
//import iaik.security.provider.IAIK;
import iaik.security.ecc.provider.ECCProvider;

import iaik.security.provider.IAIK;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;
//import org.junit.After;
//import org.junit.AfterClass;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Ignore;
//import org.junit.Test;
//import org.opensc.pkcs15.asn1.PKCS15Certificate;
//import org.opensc.pkcs15.asn1.PKCS15Objects;
//import org.opensc.pkcs15.asn1.sequence.SequenceOf;



/**
 *
 * @author clemens
 */
public class PKCS15Test {

  CardTerminal ct;
  Card icc;
  boolean liezert;

  public PKCS15Test() {
  }

//  @BeforeClass
  public static void setUpClass() throws Exception {
  }

//  @AfterClass
  public static void tearDownClass() throws Exception {
  }

//  @Before
  public void setUp() throws NoSuchAlgorithmException, CardException {
    
    IAIK.addAsJDK14Provider();
    ECCProvider.addAsProvider();
    
    System.out.println("create terminalFactory...\n");
    TerminalFactory terminalFactory = TerminalFactory.getInstance("PC/SC", null);

    System.out.println("get supported terminals...\n");
    List<CardTerminal> terminals = terminalFactory.terminals().list();

    if (terminals.size() < 1) {
      throw new CardException("no terminals");
    }

    ct = terminals.get(0);
    System.out.println("found " + terminals.size() + " terminals, using " + ct.getName() + "\n");

    System.out.println("connecting " + ct.getName() + "\n");
    icc = ct.connect("*");
    byte[] atr = icc.getATR().getBytes();
    liezert = Arrays.equals(atr, new byte[] {(byte) 0x3b, (byte) 0xbb, (byte) 0x18, (byte) 0x00, (byte) 0xc0, (byte) 0x10, (byte) 0x31, (byte) 0xfe, (byte) 0x45, (byte) 0x80, (byte) 0x67, (byte) 0x04, (byte) 0x12, (byte) 0xb0, (byte) 0x03, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x81, (byte) 0x05, (byte) 0x3c});
    byte[] historicalBytes = icc.getATR().getHistoricalBytes();
    System.out.println("found card " + toString(atr) + " " + new String(historicalBytes, Charset.forName("ASCII")) + "\n\n");
    
  }

//  @After
  public void tearDown() {
  }

//  @Test
//  @Ignore
  public void getEFDIR() throws CardException, SignatureCardException, InstantiationException, CodingException, IOException {
    
    CardChannel basicChannel = icc.getBasicChannel();
    CommandAPDU cmdAPDU;
    ResponseAPDU resp;

    System.out.println("SELECT MF");
    cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x00, 0x0c, new byte[] { 0x3F, 0x00});
    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
    resp = basicChannel.transmit(cmdAPDU);
    System.out.println(" -> " + toString(resp.getBytes()) + "\n");

//    for (int i = 0x1F00; i <= 0xFFFF; i++) {
////    for (int i = 0x5000; i <= 0x6000; i++) {
//      cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x01, 0x00, new byte[] { (byte) ((i >> 8) & 0xFF), (byte) (i & 0xFF)}, 256);
//      resp = basicChannel.transmit(cmdAPDU);
//      if ((i & 0xFF) == 0) {
//        System.out.println(Integer.toHexString(i));
//      }
//      if (resp.getSW() == 0x9000) {
//        System.out.println("found [" + Integer.toHexString((i >> 8) & 0xff) + ":" + Integer.toHexString((i) & 0xff) + "]");
//        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
//        System.out.println(" -> " + toString(resp.getBytes()) + "\n");
//
//        byte[] fcx = new TLVSequence(resp.getBytes()).getValue(0x6f);
//        System.out.println(Integer.toHexString(i) + ": " + new TLVSequence(fcx));
//        cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x00, 0x0C, new byte[] { 0x3F, 0x00});
//        resp = basicChannel.transmit(cmdAPDU);
//      }
//    }

    System.out.println("SELECT DF.CIA");
//    cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, new byte[] { (byte) 0xE8, (byte) 0x28, (byte) 0xBD, (byte) 0x08, (byte) 0x0F }, 256);
    cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, new byte[] { (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x63,(byte) 0x50,(byte) 0x4B,(byte) 0x43,(byte) 0x53,(byte) 0x2D,(byte) 0x31,(byte) 0x35 }, 256);
    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
    resp = basicChannel.transmit(cmdAPDU);
    System.out.println(" -> " + toString(resp.getBytes()) + "\n");

//    for (int i = 0x1F00; i <= 0xFFFF; i++) {
////    for (int i = 0x5000; i <= 0x6000; i++) {
//      cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x02, 0x00, new byte[] { (byte) ((i >> 8) & 0xFF), (byte) (i & 0xFF)}, 256);
//      resp = basicChannel.transmit(cmdAPDU);
//      if ((i & 0xFF) == 0) {
//        System.out.println(Integer.toHexString(i));
//      }
//      if (resp.getSW() == 0x9000) {
//        System.out.println("found [" + Integer.toHexString((i >> 8) & 0xff) + ":" + Integer.toHexString((i) & 0xff) + "]");
//        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
//        System.out.println(" -> " + toString(resp.getBytes()) + "\n");
//
//        byte[] fcx = new TLVSequence(resp.getBytes()).getValue(0x6f);
//        System.out.println(Integer.toHexString(i) + ": " + new TLVSequence(fcx));
//        cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x00, 0x0C, new byte[] { 0x3F, 0x00});
//        resp = basicChannel.transmit(cmdAPDU);
//      }
//    }


    System.out.println("SELECT EF 0x0b 0x02");
    cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x02, 0x00, new byte[] { (byte) 0x0B,(byte) 0x02 }, 256);
    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
    resp = basicChannel.transmit(cmdAPDU);
    System.out.println(" -> " + toString(resp.getBytes()) + "\n");


    System.out.println("SELECT EF.CardInfo (P1=02 P2=00)");
    cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x02, 0x00, new byte[] { (byte) 0x50,(byte) 0x32 }, 256);
    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
    resp = basicChannel.transmit(cmdAPDU);
    System.out.println(" -> " + toString(resp.getBytes()) + "\n");
    
    System.out.println("READ EF.CardInfo");
    byte[] efCardInfo = ISO7816Utils.readTransparentFile(basicChannel, -1);
    System.out.println(toString(efCardInfo));
    ASN1Object efCardInfoASN1 = DerCoder.decode(efCardInfo);
//    try {
//      FileOutputStream os = new FileOutputStream("EF.CardInfo");
//      os.write(efCardInfo);
//      os.close();
//    } catch (FileNotFoundException e) {
//      e.printStackTrace();
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
    System.out.println(ASN1.print(efCardInfoASN1));

    System.out.println("SELECT EF.OD");
    cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x02, 0x00, new byte[] { (byte) 0x50,(byte) 0x31 }, 256);
    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
    resp = basicChannel.transmit(cmdAPDU);
    System.out.println(" -> " + toString(resp.getBytes()) + "\n");

    System.out.println("READ EF.OD");
    byte[] efod = ISO7816Utils.readTransparentFile(basicChannel, -1);
    System.out.println(" " + toString(efod));
    
    for (TLV cio : new TLVSequence(efod)) {

        byte[] val = cio.getValue();
        System.out.println("val: "+ toString(val));
        byte[] path = Arrays.copyOfRange(val, 4, 4+val[3]);
        System.out.println("path: "+ toString(path));

      System.out.println("\n\nTag = " + (cio.getTag() & 0x0f));
      if (cio.getTag() == 0) {
        System.out.println("cannot decode null data");
        continue;
      }

      ASN1Object object = DerCoder.decode(cio.getValue());
      byte[] fid = (byte[]) object.getComponentAt(0).getValue();
      
      System.out.println("SELECT EF fid=" + toString(fid));
      cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x02, 0x04, fid, 256);
      System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
      resp = basicChannel.transmit(cmdAPDU);
      System.out.println(" -> " + toString(resp.getBytes()) + "\n");
      
      byte[] fcx = new TLVSequence(resp.getBytes()).getValue(0x62); //0x62 for FCP, 0x6f for FCI
      byte[] fd = new TLVSequence(fcx).getValue(0x82);

//      System.out.println("cio " + toString(fid) + " fd: " + toString(fd));

      if ((fd[0] & 0x04) > 0) {
        // records
        int records = fd[fd.length - 1];
        
        for (int record = 1; record < records; record++) {
          System.out.println("READ RECORD " + record);
          byte[] ef = ISO7816Utils.readRecord(basicChannel, record);
          System.out.println(" " + toString(ef));
          ASN1Object informationObject = DerCoder.decode(Arrays.copyOfRange(ef, 2, ef.length));
          System.out.println(ASN1.print(informationObject));
          if (cio.getTag() == 0xa0 || cio.getTag() == 0xa1) {
            System.out.println("Path = "
                + toString((byte[]) informationObject.getComponentAt(3)
                    .getComponentAt(0).getComponentAt(0).getComponentAt(0)
                    .getValue()));
          }
        }
        
      } else if (fd[0] == 0x11) {
        System.out.println("transparent structure");

        byte[] ef = ISO7816Utils.readTransparentFile(basicChannel, -1);
//        System.out.println(" " + toString(ef));

        int i = 0;
        int j;

        do {
          System.out.println("tag: 0x" + Integer.toHexString(ef[i]) + ", length: 0x" + Integer.toHexString(ef[i+1]));

            int length = 0;
            int ll = 0;
            if ((ef[i+1] & 0xf0) == 0x80) {
                ll = ef[i+1] & 0x7f;
                for (int it = 0; it < ll; it++) {
                    System.out.println(" + 0x" + Integer.toHexString(ef[i + it+2] & 0xff) );
                    length = (length << 8) + (ef[i+it+2] & 0xff);
                    System.out.println("length: " + length + " (0x" + Integer.toHexString(length) + ")");
                }
            } else {
                length = (ef[i+1] & 0xff);
            }

//          if ((ef[i+1] & 0xff) == 0x81) {
//            length = ef[i+2] & 0xff;
//            j = 3;
////            System.out.println("ef["+(i+1)+"]=0x81, setting length=" + (ef[i+2] & 0xff));
//
//          } else if ((ef[i+1] & 0xff) == 0x82) {
//            length = ((ef[i+2] & 0xff) << 8) | (ef[i+3] & 0xff);
//            j = 4;
////            System.out.println("ef["+(i+1)+"]=0x82, setting length=" + (((ef[i+2] & 0xff) << 8) | (ef[i+3] & 0xff)));
//
//          } else {
//            length = ef[i+1] & 0xff;
//            j = 2;
////            System.out.println("ef["+(i+1)+"]=0x" + Integer.toBinaryString(ef[i+1] & 0xff));
//          }

          System.out.println("setting length: 0x" + Integer.toHexString(length));

//        if (cio.getTag() == 0xa4) {
//          byte[] cert = Arrays.copyOfRange(ef, 0, ef.length-1);
////          System.out.println("cert 1: \n " + toString(cert));

          j = i + 2 + ll + length;
          System.out.println("reading ef[" + i +"-" + (j-1) + "]:\n" + toString(Arrays.copyOfRange(ef, i, j)) );

          ASN1Object informationObject = DerCoder.decode(Arrays.copyOfRange(ef, i, j));
          System.out.println(ASN1.print(informationObject));

          if (Arrays.equals(fid, new byte[] { (byte)0x44, (byte)0x00})) {
              byte[] id = (byte[]) informationObject.getComponentAt(1).getComponentAt(0).getValue();
              byte[] usage = (byte[]) informationObject.getComponentAt(1).getComponentAt(1).getValue();
              byte[] access= (byte[]) informationObject.getComponentAt(1).getComponentAt(2).getValue();
              BigInteger keyRef = (BigInteger) informationObject.getComponentAt(1).getComponentAt(3).getValue();

              System.out.println("key iD " + toString(id) );
              System.out.println("key ref " + keyRef);
              System.out.println("key usage " + toString(usage));
              System.out.println("key access "+ toString(access) );
          } else if (Arrays.equals(fid, new byte[] { (byte)0x44, (byte)0x04})) {
              System.out.println("Certificate (" + informationObject.getComponentAt(0).getComponentAt(0).getValue() + ") path: " + toString((byte[]) informationObject.getComponentAt(2).getComponentAt(0).getComponentAt(0).getComponentAt(0).getValue()) + "\n");

//            iaik.me.asn1.ASN1 obj = new iaik.me.asn1.ASN1(Arrays.copyOfRange(ef, i, j));
//            byte[] contextSpecific = obj.getElementAt(2).getEncoded();
//            System.out.println("JCE ME ASN1 obj: " + toString(contextSpecific));
//            if ((contextSpecific[0] & 0xff) != 0xa1) {
//                System.out.println("WARNING: expected CONTEXTSPECIFIC structure 0xa1, got 0x" + Integer.toHexString(contextSpecific[0]));
//            }
//              System.out.println("(contextSpecific[1] & 0xf0) = 0x" + Integer.toHexString(contextSpecific[1] & 0xf0));
//              System.out.println("(contextSpecific[1] & 0xf0) == 0x80 " + ((contextSpecific[1] & 0xf0) == 0x80));
//              System.out.println("(contextSpecific[1] & 0x0f) = 0x" + Integer.toHexString(contextSpecific[1] & 0x0f) + " = " + (contextSpecific[1] & 0x0f));
//              System.out.println("(contextSpecific[1] & 0x0f) + 2 = 0x" + Integer.toHexString((contextSpecific[1] & 0x0f)+2) + " = " + ((contextSpecific[1] & 0x0f)+2));
//
//              int ll = ((contextSpecific[1] & 0xf0) == 0x80) ? (contextSpecific[1] & 0x0f) + 2 : 2;
//              System.out.println("ll = " + ll);
//              System.out.println(toString(Arrays.copyOfRange(contextSpecific, ll, contextSpecific.length)));
//            if ((contextSpecific[1] & 0xff) == 0x81) {
//                iaik.me.asn1.ASN1 x509CertificateAttributes = new iaik.me.asn1.ASN1(
//                        Arrays.copyOfRange(contextSpecific, ll, contextSpecific.length));
//                System.out.println("path?: " + toString(x509CertificateAttributes.getElementAt(0).getElementAt(0).gvByteArray()));
//
//            }


//            byte[] ef_qcert = obj.getElementAt(2).getElementAt(0).getElementAt(0)
//                    .getElementAt(0).gvByteArray();
//            System.out.println("reading certificate "
//                    + obj.getElementAt(0).getElementAt(0).gvString()
//                    + " from fid=" + toString(ef_qcert));
            }
          i = j;
        } while (i<ef.length && ef[i]>0);
      }
    }


//    System.out.println("SELECT by Path");
//    cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x09, 0x00, new byte[] { (byte) 0x3F, (byte) 0x00, (byte) 0x56, (byte) 0x49 }, 256);
//    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
//    resp = basicChannel.transmit(cmdAPDU);
//    System.out.println(" -> " + toString(resp.getBytes()) + "\n");
//
//    System.out.println(new TLVSequence(new TLVSequence(resp.getData()).getValue(0x6f)));
//
//    byte[] ef = ISO7816Utils.readTransparentFile(basicChannel, -1);
//    System.out.println(toString(ef));
//
//    try {
//      FileOutputStream fileOutputStream = new FileOutputStream("EF.IV");
//      fileOutputStream.write(ef);
//      fileOutputStream.close();
//    } catch (FileNotFoundException e1) {
//      e1.printStackTrace();
//    } catch (IOException e1) {
//      e1.printStackTrace();
//    }
//
//    System.out.println("done.");

  }

//  @Test
//  @Ignore
  public void ecard() throws CardException, SignatureCardException, CodingException {
    CardChannel basicChannel = icc.getBasicChannel();
    CommandAPDU cmdAPDU;
    ResponseAPDU resp;

    System.out.println("SELECT MF");
    cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x00, 0x0c, new byte[] { (byte) 0x3F, (byte) 0x00 });
    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
    resp = basicChannel.transmit(cmdAPDU);
    System.out.println(" -> " + toString(resp.getBytes()) + "\n");

    System.out.println("SELECT EF.CardInfo (P1=02 P2=00)");
    cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x02, 0x00, new byte[] { (byte) 0x50,(byte) 0x32 }, 256);
    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
    resp = basicChannel.transmit(cmdAPDU);
    System.out.println(" -> " + toString(resp.getBytes()) + "\n");

    System.out.println("READ EF.CardInfo");
    byte[] efCardInfo = ISO7816Utils.readTransparentFile(basicChannel, -1);
    System.out.println(toString(efCardInfo));
    ASN1Object efCardInfoASN1 = DerCoder.decode(efCardInfo);
    System.out.println(ASN1.print(efCardInfoASN1));

    cmdAPDU = new CommandAPDU(0x00, 0xa4, 0x04, 0x00, new byte[] { (byte) 0xd0, (byte) 0x40,
      (byte) 0x00, (byte) 0x00, (byte) 0x17, (byte) 0x00, (byte) 0x12,
      (byte) 0x01 }, 256);
    System.out.println("SELECT AID " + toString(cmdAPDU.getData()));
    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
    resp = basicChannel.transmit(cmdAPDU);
    System.out.println(" -> " + toString(resp.getBytes()) + "\n");
    System.out.println(new TLVSequence(new TLVSequence(resp.getData()).getValue(0x6f)));

    System.out.println("SELECT CERTIFICATE");
    cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x02, 0x00, new byte[] { (byte) 0xc0, (byte) 0x00 }, 256);
    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
    resp = basicChannel.transmit(cmdAPDU);
    System.out.println(" -> " + toString(resp.getBytes()) + "\n");

    X509Certificate certificate = null;
    try {
      System.out.println("READ cert?");
      CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
      certificate = (X509Certificate) certificateFactory.generateCertificate(ISO7816Utils.openTransparentFileInputStream(basicChannel, -1));
//      certificate = certificateFactory.generateCertificate(new BASE64DecoderStream(new ByteArrayInputStream(CERT.getBytes())));
//      System.out.println("certificate: \n" + toString(certificate.getEncoded()));
      System.out.println("certificate: \n" + certificate);
    } catch (CertificateException e) {
      e.printStackTrace();
    }

    byte[] fid = new byte[] {(byte) 0x00, (byte) 0x30 };
    System.out.println("SELECT EF FID=" + toString(fid));
    cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x02, 0x04, fid, 256);
    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
    resp = basicChannel.transmit(cmdAPDU);
    System.out.println(" -> " + toString(resp.getBytes()) + "\n");
    System.out.println(new TLVSequence(new TLVSequence(resp.getData()).getValue(0x62)));

    byte[] fcx = new TLVSequence(resp.getBytes()).getValue(0x62); //0x62 for FCP, 0x6f for FCI
    byte[] fd = new TLVSequence(fcx).getValue(0x82);

//      System.out.println("cio " + toString(fid) + " fd: " + toString(fd));

    if ((fd[0] & 0x04) > 0) {
      // records
      int records = fd[fd.length - 1];

      for (int record = 1; record < records-1; record++) {
        System.out.println("READ RECORD " + record);
        byte[] ef = ISO7816Utils.readRecord(basicChannel, record);
        System.out.println(" " + toString(ef));
      }
    }
  }


//  @Test
//  @Ignore
  public void sign() throws CardException, SignatureCardException, InstantiationException, CodingException {
    CardChannel basicChannel = icc.getBasicChannel();
    CommandAPDU cmdAPDU;
    ResponseAPDU resp;

    System.out.println("SELECT DF.CIA");
    cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, new byte[] { (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x63,(byte) 0x50,(byte) 0x4B,(byte) 0x43,(byte) 0x53,(byte) 0x2D,(byte) 0x31,(byte) 0x35 }, 256);
    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
    resp = basicChannel.transmit(cmdAPDU);
    System.out.println(" -> " + toString(resp.getBytes()) + "\n");

    System.out.println("SELECT CERTIFICATE");
    cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x02, 0x00, new byte[] { (byte) 0x0c, (byte) 0x02 }, 256);
    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
    resp = basicChannel.transmit(cmdAPDU);
    System.out.println(" -> " + toString(resp.getBytes()) + "\n");

    Certificate certificate = null;
    try {
      CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
      certificate = certificateFactory.generateCertificate(ISO7816Utils.openTransparentFileInputStream(basicChannel, -1));
//      certificate = certificateFactory.generateCertificate(new BASE64DecoderStream(new ByteArrayInputStream(CERT.getBytes())));
      System.out.println("Certificate: \n===================================\n"
              + toString(certificate.getEncoded())
              + "\n===================================\n"
              + certificate
              + "\n===================================\n");
    } catch (CertificateException e) {
      e.printStackTrace();
    }

    System.out.println("SELECT MF");
    cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x00, 0x0c, new byte[] { (byte) 0x3F, (byte) 0x00 });
    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
    resp = basicChannel.transmit(cmdAPDU);
    System.out.println(" -> " + toString(resp.getBytes()) + "\n");

//    byte[] fid = new byte[] {(byte) 0x50, (byte) 0x15 };
//    System.out.println("SELECT DF FID=" + toString(fid));
//    cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x01, 0x00, fid, 256);
//    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
//    resp = basicChannel.transmit(cmdAPDU);
//    System.out.println(" -> " + toString(resp.getBytes()) + "\n");
//    System.out.println(new TLVSequence(new TLVSequence(resp.getData()).getValue(0x6f)));

    cmdAPDU = (liezert)
            ? new CommandAPDU(0x00, 0xA4, 0x04, 0x04, new byte[] { (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x63,(byte) 0x50,(byte) 0x4B,(byte) 0x43,(byte) 0x53,(byte) 0x2D,(byte) 0x31,(byte) 0x35 }, 256)
            : new CommandAPDU(0x00, 0xa4, 0x04, 0x00, new byte[] { (byte) 0xd2, (byte) 0x76, (byte) 0x00, (byte) 0x00, (byte) 0x66, (byte) 0x01 }, 256);
    System.out.println("SELECT AID " + toString(cmdAPDU.getData()));
    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
    resp = basicChannel.transmit(cmdAPDU);
    System.out.println(" -> " + toString(resp.getBytes()) + "\n");
    System.out.println(new TLVSequence(new TLVSequence(resp.getData()).getValue(0x62)));

    byte kid = (liezert)
             ? (byte) 0x82  // don't set to 0x03 (SO Pin, 63c2)
             : (byte) 0x81; // QuoVadis: 0x81 ?! CommonObjectAttributes.authId = 0x11
    System.out.println("VERIFY kid=" + Integer.toHexString(kid & 0xff));
    cmdAPDU = ISO7816Utils.createVerifyAPDU(new VerifyAPDUSpec(new byte[] {(byte) 0x00, (byte) 0x20, (byte) 0x00, kid}, 0, VerifyAPDUSpec.PIN_FORMAT_ASCII, (liezert) ? 8 : 0), "123456".toCharArray());
    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
    resp = basicChannel.transmit(cmdAPDU);
    System.out.println(" -> " + toString(resp.getBytes()) + "\n");


//    byte[] fid = new byte[] {(byte) 0x00, (byte) 0x30 };
//    System.out.println("SELECT EF FID=" + toString(fid));
//    cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x02, 0x04, fid, 256);
//    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
//    resp = basicChannel.transmit(cmdAPDU);
//    System.out.println(" -> " + toString(resp.getBytes()) + "\n");

//    int seid = 1;
//    System.out.println("RESTORE SE Id " + seid);
//    cmdAPDU = new CommandAPDU(0x00, 0x22, 0xF3, seid);
//    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
//    resp = basicChannel.transmit(cmdAPDU);
//    System.out.println(" -> " + toString(resp.getBytes()) + "\n");


//    byte keyRef = (liezert)
//                ? (byte) 132    //0x84
//                : (byte) 2;     //QuoVadis: 0x02
//    System.out.println("SET DST (key ref: 0x" + Integer.toHexString(keyRef & 0xff) + ")");
//    byte[] dst = new byte[] {
////      (byte) 0x95, (byte) 0x01, (byte) 0x40,
//      (byte) 0x84, (byte) 0x03, (byte) 0x80, (byte) (0x80 ^ keyRef), (byte) 0x00,
//      (byte) 0x89, (byte) 0x03, (byte) 0x13, (byte) 0x23, (byte) 0x10
//    };
//    cmdAPDU = new CommandAPDU(0x00, 0x22, 0x41, 0xb6, dst, 256);
//    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
//    resp = basicChannel.transmit(cmdAPDU);
//    System.out.println(" -> " + toString(resp.getBytes()) + "\n");

    int i = 1;
    byte[] dst = new byte[] {
      // 3 byte keyRef (key number 1)
//      (byte) 0x84, (byte) 0x03, (byte) 0x80, (byte) 0x01, (byte) 0xff,
      // 1 byte keyRef (key number 1)
      (byte) 0x84, (byte) 0x01, (byte) (0x80 | (i & 0x7f)),
      //RSA Authentication
      (byte) 0x89, (byte) 0x02, (byte) 0x23, (byte) 0x13
    };
    cmdAPDU = new CommandAPDU(0x00, 0x22, 0x41, 0xa4, dst);
    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
    resp = basicChannel.transmit(cmdAPDU);
    System.out.println(" -> " + toString(resp.getBytes()) + "\n");

    byte[] oid = new byte[] { (byte) 0x30, (byte) 0x21, (byte) 0x30,
        (byte) 0x09, (byte) 0x06, (byte) 0x05, (byte) 0x2b,
        (byte) 0x0e, (byte) 0x03, (byte) 0x02, (byte) 0x1a,
        (byte) 0x05, (byte) 0x00, (byte) 0x04, (byte) 0x14 };

    byte[] hash;
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      hash = md.digest();
      System.out.println("hash value to be signed:\n " + toString(hash));
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      return;
    }

//    byte[] AI = new byte[] {
//       (byte) 0xF3, (byte) 0x15, (byte) 0x7B, (byte) 0xAC, (byte) 0x94,
//       (byte) 0xCA, (byte) 0x1D, (byte) 0xC1, (byte) 0xE7, (byte) 0x7D,
//       (byte) 0xCA, (byte) 0xF5, (byte) 0xF5, (byte) 0x3A, (byte) 0x80,
//       (byte) 0xEF, (byte) 0x6C, (byte) 0xC2, (byte) 0x1C, (byte) 0xE9 };

    ByteArrayOutputStream data = new ByteArrayOutputStream();

    try {
      // oid
      data.write(oid);
      // hash
      data.write(hash);
    } catch (IOException e) {
      throw new SignatureCardException(e);
    }

    cmdAPDU = new CommandAPDU(0x00, 0x88, 0x00, 0x00, data.toByteArray(), 256);
    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
    resp = basicChannel.transmit(cmdAPDU);
    System.out.println(" -> " + toString(resp.getBytes()) + "\n");




//    for (int i = 1; i < 256; i++) {
//      System.out.println("trying alg id " + Integer.toHexString(i & 0xff));
//
//      final byte[] dst = {
//          (byte) 0x80, // algorithm reference
//  //          (byte) 0x01, (byte) 0x12, // RSASSA-PKCS1-v1.5 using SHA1
//            (byte) 0x01, (byte) (i & 0xff), // RSASSA-PKCS1-v1.5 using SHA1
//          (byte) 0x84, // private key reference
//            (byte) 0x01, (byte) 0x82};
//  //        (byte) 0x91, (byte) 0x00 }; // random num provided by card
//
////      System.out.println("SET DST");
//      cmdAPDU = new CommandAPDU(0x00, 0x22, 0x41, 0xb6, dst);
////      System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
//      resp = basicChannel.transmit(cmdAPDU);
////      System.out.println(" -> " + toString(resp.getBytes()) + "\n");
//
//      if (resp.getSW() != 0x6a80) {
//        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
//        System.out.println(" -> " + toString(resp.getBytes()) + "\n");
//      }
//    }


    
//    byte[] fid = new byte[] {(byte) 0x0f, (byte) 0x01 };
//    System.out.println("SELECT EF FID=" + toString(fid));
//    cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x02, 0x04, fid, 256);
//    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
//    resp = basicChannel.transmit(cmdAPDU);
//    System.out.println(" -> " + toString(resp.getBytes()) + "\n");
//    System.out.println("READ priv key?");
//    byte[] readTransparentFile = ISO7816Utils.readTransparentFile(basicChannel, -1);
//    System.out.println("read: " + toString(readTransparentFile));

//    byte[] hash;
//    try {
//      MessageDigest md = MessageDigest.getInstance("SHA-1");
//      hash = md.digest();
//      System.out.println("hash value to be signed:\n " + toString(hash));
//    } catch (NoSuchAlgorithmException e) {
//      e.printStackTrace();
//      return;
//    }
//
//    System.out.println("HASH");
//    byte[] dataObj = new byte[hash.length+2];
//    dataObj[0] = (byte) 0x90;
//    dataObj[1] = (byte) 0x14;
//    System.arraycopy(hash, 0, dataObj, 2, hash.length);
//    cmdAPDU = new CommandAPDU(0x00, 0x2a, 0x90, 0xa0, dataObj);
//    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
//    resp = basicChannel.transmit(cmdAPDU);
//    System.out.println(" -> " + toString(resp.getBytes()) + "\n");
//
//    System.out.println("PSO COMPUTE DIGITAL SIGNATURE");
//    cmdAPDU = new CommandAPDU(0x00, 0x2A, 0x9E, 0x9A, 256); //data.toByteArray(),
//    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
//    resp = basicChannel.transmit(cmdAPDU);
//    System.out.println(" -> " + toString(resp.getBytes()) + "\n");
//
//    if (resp.getSW() != 0x9000) {
//      byte[] oid = new byte[] { (byte) 0x30, (byte) 0x21, (byte) 0x30,
//          (byte) 0x09, (byte) 0x06, (byte) 0x05, (byte) 0x2b,
//          (byte) 0x0e, (byte) 0x03, (byte) 0x02, (byte) 0x1a,
//          (byte) 0x05, (byte) 0x00, (byte) 0x04, (byte) 0x14 };
//
//      ByteArrayOutputStream data = new ByteArrayOutputStream();
//
//      try {
//        // oid
//        data.write(oid);
//        // hash
//        data.write(hash);
//      } catch (IOException e) {
//        throw new SignatureCardException(e);
//      }
//
//      System.out.println("PSO COMPUTE DIGITAL SIGNATURE");
//      cmdAPDU = new CommandAPDU(0x00, 0x2A, 0x9E, 0x9A, data.toByteArray(), 256);
//      System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
//      resp = basicChannel.transmit(cmdAPDU);
//      System.out.println(" -> " + toString(resp.getBytes()) + "\n");
//    }

    if (resp.getSW() == 0x9000 && certificate != null) {

      try {
        System.out.println("Verifying signature with " + ((X509Certificate) certificate).getIssuerDN());
        Signature signature = Signature.getInstance("SHA/RSA");
        signature.initVerify(certificate.getPublicKey());
        boolean valid = signature.verify(resp.getData());

        System.out.println("Signature is " + ((valid) ? "valid" : "invalid"));
      } catch (InvalidKeyException e) {
        e.printStackTrace();
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
      } catch (SignatureException e) {
        e.printStackTrace();
      }

    }

  }

  private final static String CERT = //"-----BEGIN CERTIFICATE-----" +
          "MIIGFDCCBPygAwIBAgICDOEwDQYJKoZIhvcNAQEFBQAwgYYxCzAJBgNVBAYTAkxJ"
+"MSMwIQYDVQQKExpMaWVjaHRlbnN0ZWluaXNjaGUgUG9zdCBBRzEoMCYGA1UECxMf"
+"SXNzdWluZyBDZXJ0aWZpY2F0aW9uIEF1dGhvcml0eTEoMCYGA1UEAxMfTGllY2h0"
+"ZW5zdGVpbiBQb3N0IFF1YWxpZmllZCBDQTAeFw0xMDA5MDExMjQ5MTJaFw0xMTA5"
+"MDExMjQ5MDdaMIHaMQswCQYDVQQGEwJMSTEOMAwGA1UEBxMFVmFkdXoxLDAqBgNV"
+"BAoTI0xpZWNodGVuc3RlaW5pc2NoZSBMYW5kZXN2ZXJ3YWx0dW5nMUcwRQYDVQQL"
+"Ez5UZXN0IGNlcnRpZmljYXRlIChubyBsaWFiaWxpdHkpIFRlc3R6ZXJ0aWZpa2F0"
+"IChrZWluZSBIYWZ0dW5nKTErMCkGA1UECxMiQW10IGZ1ZXIgUGVyc29uYWwgdW5k"
+"IE9yZ2FuaXNhdGlvbjEXMBUGA1UEAxMOVEVTVCBMTFYgQVBPIDIwggEiMA0GCSqG"
+"SIb3DQEBAQUAA4IBDwAwggEKAoIBAQChDpzPyb0NIuqi+UGCOhypcODFMKas1kTw"
+"HPyLW2ZdtqzmrgO7Q7Y5jm2CpPdCkd61Z+/lswEB+wPgSe+YnnNuytYtM0uYaNv9"
+"UNxc6CmlthIOJTK2+VP9lwIOsS61Jr+boTEXjXszFVwkO288wGJtCB3SG6IZja6l"
+"UD/veXoJckC5OIS43V6CqOKcyz6CNhu+OhKTwgqd07KXzzEdUeLemrgrNP9/qnDz"
+"xnDiRtyu/zocCG9xR7Rq6ZNwX69JNPi6AljsAvMucM7bhdbW8pyPKVUEhBFLduM0"
+"hmQYpodANUnPtpXA5ksxcgSWn/SdTuJ8VbG8SrvSR+1b70Coef0fAgMBAAGjggI0"
+"MIICMDCB/gYDVR0gBIH2MIHzMAgGBgQAizABATCB5gYKKwYBBAG+WAGDEDCB1zCB"
+"ngYIKwYBBQUHAgIwgZEagY5SZWxpYW5jZSBvbiB0aGUgUXVvVmFkaXMgUm9vdCBD"
+"ZXJ0aWZpY2F0ZSBieSBhbnkgcGFydHkgYXNzdW1lcyBhY2NlcHRhbmNlIG9mIHRo"
+"ZSBRdW9WYWRpcyBDZXJ0aWZpY2F0ZSBQb2xpY3kvQ2VydGlmaWNhdGlvbiBQcmFj"
+"dGljZSBTdGF0ZW1lbnQuMDQGCCsGAQUFBwIBFihodHRwOi8vd3d3LnF1b3ZhZGlz"
+"Z2xvYmFsLmNvbS9yZXBvc2l0b3J5MC4GCCsGAQUFBwEDBCIwIDAKBggrBgEFBQcL"
+"AjAIBgYEAI5GAQEwCAYGBACORgEEMHIGCCsGAQUFBwEBBGYwZDAqBggrBgEFBQcw"
+"AYYeaHR0cDovL29jc3AucXVvdmFkaXNnbG9iYWwuY29tMDYGCCsGAQUFBzAChipo"
+"dHRwOi8vdHJ1c3QucXVvdmFkaXNnbG9iYWwuY29tL2xpcHFjYS5jcnQwDgYDVR0P"
+"AQH/BAQDAgbAMB8GA1UdIwQYMBaAFPsbkJP9mNp/kmoaRiY20fOPhwDgMDkGA1Ud"
+"HwQyMDAwLqAsoCqGKGh0dHA6Ly9jcmwucXVvdmFkaXNnbG9iYWwuY29tL2xpcHFj"
+"YS5jcmwwHQYDVR0OBBYEFADlv8IBR5ga0KjxSiByi2T1whHEMA0GCSqGSIb3DQEB"
+"BQUAA4IBAQB4LzgcpNxKcGwxdbep1E6MiXk3gwS6kq06Iaf7Ar/By2SuyLB8l0B7"
+"myk8VvkIGVCP0f+i7WxblUV5xqXP2Itnq7Ynm4A5qdUkBZuXvOGY2sOtjNttqdnv"
+"oemsshz3QIEBwlh10SZZbwtVv7W7uy0xUwbsWFX0r8/jiQyVANyPRQ+KqW+H6U05"
+"13FG5da/AgXvUGGLYVDk66qGYn/TlGBgj8ijvWqqbZ94vvbog/rwGHG+P+0JMRTS"
+"QsNR8hmlgd8OLwWc1SFB5TrDsjkDTCQHce/MJ0n6YNPXQr8EHWpu5And2gzmWrYh"
+"Cx5l+gCuh6N9ITOAFmyc1gleyNdTenEE";
//          +"-----END CERTIFICATE-----";


//  @Ignore
  public void directoryListing(CommandAPDU cmdAPDU, ResponseAPDU resp, CardChannel basicChannel) throws CardException, SignatureCardException {

    byte[] dir = new byte[] {(byte) 0x50, (byte) 0x15};

    System.out.println("SELECT MF");
    cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x00, 0x0c, new byte[] { (byte) 0x3F, (byte) 0x00 }, 256);
    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
    resp = basicChannel.transmit(cmdAPDU);
    System.out.println(" -> " + toString(resp.getBytes()) + "\n");

//    System.out.println("SELECT DF.CIA");
//    cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, new byte[] { (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x63,(byte) 0x50,(byte) 0x4B,(byte) 0x43,(byte) 0x53,(byte) 0x2D,(byte) 0x31,(byte) 0x35 }, 256);
//    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
//    resp = basicChannel.transmit(cmdAPDU);
//    System.out.println(" -> " + toString(resp.getBytes()) + "\n");

    System.out.println("SELECT [50:15]");
    cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x01, 0x04, dir, 256);
    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
    resp = basicChannel.transmit(cmdAPDU);
    System.out.println(" -> " + toString(resp.getBytes()) + "\n");

    for (int i = 0x1F00; i <= 0xFFFF; i++) {
//    for (int i = 0x0000; i <= 0x1F00; i++) {
      cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x02, 0x04, new byte[] { (byte) ((i >> 8) & 0xFF), (byte) (i & 0xFF)}, 256);
      resp = basicChannel.transmit(cmdAPDU);
      if ((i & 0xFF) == 0) {
        System.out.println(Integer.toHexString(i));
      }
      if (resp.getSW() == 0x9000) {
        System.out.println("found [" + Integer.toHexString((i >> 8) & 0xff) + ":" + Integer.toHexString((i) & 0xff) + "]");
        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
        System.out.println(" -> " + toString(resp.getBytes()) + "\n");

//        byte[] fcx = new TLVSequence(resp.getBytes()).getValue(0x6f);
//        System.out.println(Integer.toHexString(i) + ": " + new TLVSequence(fcx));
        cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x00, 0x0C, new byte[] { 0x3F, 0x00});
        resp = basicChannel.transmit(cmdAPDU);
        cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x01, 0x04, dir);
        resp = basicChannel.transmit(cmdAPDU);
      }
    }

  }

//  @Test
//  @Ignore
  public void verify() throws CardException {
    CardChannel basicChannel = icc.getBasicChannel();
    CommandAPDU cmdAPDU;
    ResponseAPDU resp;

    byte kid = (liezert)
             ? (byte) 0x82  // don't set to 0x03 (SO Pin, 63c2)
             : (byte) 0x81; // QuoVadis: 0x81 ?! CommonObjectAttributes.authId = 0x11
    System.out.println("VERIFY kid=" + Integer.toHexString(kid & 0xff));
    cmdAPDU = ISO7816Utils.createVerifyAPDU(new VerifyAPDUSpec(new byte[] {(byte) 0x00, (byte) 0x20, (byte) 0x00, kid}, 0, VerifyAPDUSpec.PIN_FORMAT_ASCII, (liezert) ? 8 : 0), "123456".toCharArray());
    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
    resp = basicChannel.transmit(cmdAPDU);
    System.out.println(" -> " + toString(resp.getBytes()) + "\n");

    cmdAPDU = new CommandAPDU(0x00, 0x20, 0x00, kid);
    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
    resp = basicChannel.transmit(cmdAPDU);
    System.out.println(" -> " + toString(resp.getBytes()) + "\n");


  }

//  @Test
//  @Ignore
  public void selectAndRead() throws CardException, SignatureCardException {
    CardChannel basicChannel = icc.getBasicChannel();
    CommandAPDU cmdAPDU;
    ResponseAPDU resp;

    System.out.println("SELECT MF");
    cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x00, 0x0c, new byte[] { (byte) 0x3F, (byte) 0x00 }, 256);
    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
    resp = basicChannel.transmit(cmdAPDU);
    System.out.println(" -> " + toString(resp.getBytes()) + "\n");

    System.out.println("SELECT DF.CIA");
    cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x04, 0x04, new byte[] { (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x63,(byte) 0x50,(byte) 0x4B,(byte) 0x43,(byte) 0x53,(byte) 0x2D,(byte) 0x31,(byte) 0x35 }, 256);
    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
    resp = basicChannel.transmit(cmdAPDU);
    System.out.println(" -> " + toString(resp.getBytes()) + "\n");

    
//    byte kid = (liezert)
//             ? (byte) 0x82  // don't set to 0x03 (SO Pin, 63c2)
//             : (byte) 0x81; // QuoVadis: 0x81 ?! CommonObjectAttributes.authId = 0x11
//    System.out.println("VERIFY kid=" + Integer.toHexString(kid & 0xff));
//    cmdAPDU = ISO7816Utils.createVerifyAPDU(new VerifyAPDUSpec(new byte[] {(byte) 0x00, (byte) 0x20, (byte) 0x00, kid}, 0, VerifyAPDUSpec.PIN_FORMAT_ASCII, (liezert) ? 8 : 0), "123456".toCharArray());
//    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
//    resp = basicChannel.transmit(cmdAPDU);
//    System.out.println(" -> " + toString(resp.getBytes()) + "\n");

    byte[][] fids = new byte[][] {{(byte)0x00,(byte)0x12},
    {(byte)0x00,(byte)0x13},
    {(byte)0x00,(byte)0x15},
    {(byte)0x00,(byte)0x16},
    {(byte)0x00,(byte)0x30},
    {(byte)0x00,(byte)0x37},
    {(byte)0x0c,(byte)0x02},
    {(byte)0x0e,(byte)0x01},
    {(byte)0x0e,(byte)0x02},
    {(byte)0x0f,(byte)0x01},
    {(byte)0x0f,(byte)0x02},
    {(byte)0x44,(byte)0x00},
    {(byte)0x44,(byte)0x01},
    {(byte)0x50,(byte)0x31},
    {(byte)0x50,(byte)0x32},
    {(byte)0x53,(byte)0x42},
    {(byte)0x53,(byte)0x62},
    {(byte)0xae,(byte)0x0a}};

    for (int i = 0; i < fids.length; i++) {
      System.out.println("SELECT EF " + toString(fids[i]));
      cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x02, 0x04, fids[i], 256);
//      System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
      resp = basicChannel.transmit(cmdAPDU);
//      System.out.println(" -> " + toString(resp.getBytes()) + "\n");

      byte[] fcx = new TLVSequence(resp.getBytes()).getValue(0x62); //0x62 for FCP, 0x6f for FCI
      try {
        readFile(basicChannel, fids[i], fcx);
        
      } catch (Exception ex) {
        System.out.println("************ read failed: " + ex.getMessage());
      }
    }
  }

  protected void readFile(CardChannel channel, byte[] fid, byte[] fcx) throws CardException, SignatureCardException, CodingException {

      byte[] fd = new TLVSequence(fcx).getValue(0x82);

      if ((fd[0] & 0x04) > 0 || fd[0] == 0x12) {
        System.out.println("  records");
        int records = fd[fd.length - 1];

        for (int record = 1; record < records; record++) {
//          System.out.println("    READ RECORD " + record);
          byte[] ef = ISO7816Utils.readRecord(channel, record);
//          System.out.println(" " + toString(ef));
//          ASN1Object informationObject = DerCoder.decode(Arrays.copyOfRange(ef, 2, ef.length));
//          System.out.println(ASN1.print(informationObject));
        }

      } else if (fd[0] == 0x11) {
        System.out.println("  transparent structure");

        byte[] ef = ISO7816Utils.readTransparentFile(channel, -1);
//        System.out.println(" " + toString(ef));

//        int length;
//        int i = 0;
//        int j;
//
//        do {
//          System.out.println("tag: 0x" + Integer.toHexString(ef[i]) + ", length: 0x" + Integer.toHexString(ef[i+1]));
//          if ((ef[i+1] & 0xff) == 0x81) {
//            length = ef[i+2] & 0xff;
//            j = 3;
////            System.out.println("ef["+(i+1)+"]=0x81, setting length=" + (ef[i+2] & 0xff));
//
//          } else if ((ef[i+1] & 0xff) == 0x82) {
//            length = ((ef[i+2] & 0xff) << 8) | (ef[i+3] & 0xff);
//            j = 4;
////            System.out.println("ef["+(i+1)+"]=0x82, setting length=" + (((ef[i+2] & 0xff) << 8) | (ef[i+3] & 0xff)));
//
//          } else {
//            length = ef[i+1] & 0xff;
//            j = 2;
////            System.out.println("ef["+(i+1)+"]=0x" + Integer.toBinaryString(ef[i+1] & 0xff));
//          }
//
//          System.out.println("setting length: 0x" + Integer.toHexString(length));
//
////        if (cio.getTag() == 0xa4) {
////          byte[] cert = Arrays.copyOfRange(ef, 0, ef.length-1);
//////          System.out.println("cert 1: \n " + toString(cert));
//
//          j = i + j + length;
//          System.out.println("reading ef[" + i +"-" + (j-1) + "]:\n" + toString(Arrays.copyOfRange(ef, i, j)) );
//          ASN1Object informationObject = DerCoder.decode(Arrays.copyOfRange(ef, i, j));
//          System.out.println(ASN1.print(informationObject));
//          i = j;
//        } while (i<ef.length && ef[i]>0);
      } else {
        System.out.println("   structure not supported: 0x" + Integer.toHexString(fd[0]));
      }
  }


//  @Ignore
  public void todo(Certificate certificate, CommandAPDU cmdAPDU, ResponseAPDU resp, CardChannel basicChannel) throws CardException, SignatureCardException {

//    System.out.println("SELECT by Path");
//    cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x09, 0x00, new byte[] { (byte) 0x3F, (byte) 0x00, (byte) 0x56, (byte) 0x49 }, 256);
//    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
//    resp = basicChannel.transmit(cmdAPDU);
//    System.out.println(" -> " + toString(resp.getBytes()) + "\n");
//    
////    System.out.println(new TLVSequence(new TLVSequence(resp.getData()).getValue(0x6f)));
//    
//    byte[] ef = ISO7816Utils.readTransparentFile(basicChannel, -1);
//    System.out.println(toString(ef));
//    
//    try {
//      FileOutputStream fileOutputStream = new FileOutputStream("EF.IV");
//      fileOutputStream.write(ef);
//      fileOutputStream.close();
//    } catch (FileNotFoundException e1) {
//      e1.printStackTrace();
//    } catch (IOException e1) {
//      e1.printStackTrace();
//    }
//    
//    System.out.println("done.");

    final byte[] AID = new byte[] {(byte) 0xd2, (byte) 0x76, (byte) 0x00, (byte) 0x00, (byte) 0x66, (byte) 0x01};
    
    System.out.println("SELECT Application (" + toString(AID) + ")");
    cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, AID, 256);
    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
    resp = basicChannel.transmit(cmdAPDU);
    System.out.println(" -> " + toString(resp.getBytes()) + "\n");
    System.out.println(new TLVSequence(new TLVSequence(resp.getData()).getValue(0x6f)));

//    int seid = 1;
//    cmdAPDU = new CommandAPDU(0x00, 0x22, 0xF3, seid);
//    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
//    resp = basicChannel.transmit(cmdAPDU);
//    System.out.println(" -> " + toString(resp.getBytes()) + "\n");
    
    System.out.println("VERIFY");
    cmdAPDU = new CommandAPDU(0x00, 0x20, 0x00, 0x81, "123456".getBytes(Charset.forName("ASCII")), 256);
    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
    resp = basicChannel.transmit(cmdAPDU);
    System.out.println(" -> " + toString(resp.getBytes()) + "\n");
    
    byte[] hash;
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      hash = md.digest();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      return;
    }

    byte[] oid = new byte[] { (byte) 0x30, (byte) 0x21, (byte) 0x30,
        (byte) 0x09, (byte) 0x06, (byte) 0x05, (byte) 0x2b,
        (byte) 0x0e, (byte) 0x03, (byte) 0x02, (byte) 0x1a,
        (byte) 0x05, (byte) 0x00, (byte) 0x04, (byte) 0x14 };
    
    ByteArrayOutputStream data = new ByteArrayOutputStream();
    
    try {
      // oid
      data.write(oid);
      // hash
      data.write(hash);
    } catch (IOException e) {
      throw new SignatureCardException(e);
    }
    
    
    System.out.println("PSO COMPUTE DIGITAL SIGNATURE");
    cmdAPDU = new CommandAPDU(0x00, 0x2A, 0x9E, 0x9A, data.toByteArray(), 256);
    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
    resp = basicChannel.transmit(cmdAPDU);
    System.out.println(" -> " + toString(resp.getBytes()) + "\n");
    
    if (resp.getSW() == 0x9000 && certificate != null) {
      
      try {
        System.out.println("Verifying signature with " + ((X509Certificate) certificate).getSubjectDN());
        Signature signature = Signature.getInstance("SHA/RSA");
        signature.initVerify(certificate.getPublicKey());
        boolean valid = signature.verify(resp.getData());
        
        System.out.println("Signature is " + ((valid) ? "valid" : "invalid"));
      } catch (InvalidKeyException e) {
        e.printStackTrace();
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
      } catch (SignatureException e) {
        e.printStackTrace();
      }
      
    }
    
    
//    final byte[] data = new byte[] {}; //new byte[] {(byte) 0x7B, (byte) 0x02, (byte) 0xB6, (byte) 0x80}; 
//    
//    System.out.println("GET DATA");
//    for (int i = 0x004D; i <= 0x004D; i++) {
//      cmdAPDU = new CommandAPDU(0x00, 0xCA, 0xFF & (i >> 8), 0xFF & i, data , 256);
//      resp = basicChannel.transmit(cmdAPDU);
//      if (resp.getSW() == 0x9000) {
//        if (i == 0x180) {
//          try {
//            System.out.println(new String(resp.getData(), "ASCII"));
//          } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//          }
//        } else {
//          System.out.println(Integer.toHexString(i) + " -> " + toString(resp.getData()));
//        }
//      }
//    }

    
    
//    final byte[] DST = new byte[] {};
//    
//    System.out.println("MSE SET DST (" + toString(DST) + ")");
//    cmdAPDU = new CommandAPDU(0x00, 0x22, 0x04, 0x01, DST);
//    System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
//    resp = basicChannel.transmit(cmdAPDU);
//    System.out.println(" -> " + toString(resp.getBytes()) + "\n");
    
  }
  
  public static String toString(byte[] b) {
    StringBuffer sb = new StringBuffer();
    sb.append('[');
    if (b != null && b.length > 0) {
      sb.append(Integer.toHexString((b[0] & 240) >> 4));
      sb.append(Integer.toHexString(b[0] & 15));
      for (int i = 1; i < b.length; i++) {
        sb.append((i % 32 == 0) ? '\n' : ':');
        sb.append(Integer.toHexString((b[i] & 240) >> 4));
        sb.append(Integer.toHexString(b[i] & 15));
      }
    }
    sb.append(']');
    return sb.toString();
  }

 
  public static void main(String[] args) {
        try {
            System.out.println("manually running pkcs15 test...");
            PKCS15Test test = new PKCS15Test();
            test.setUp();
//            test.getEFDIR();
            test.sign();
            //    test.selectAndRead();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
  }

}
