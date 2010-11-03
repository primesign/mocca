/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.gv.egiz.smcc.activation;

import iaik.security.provider.IAIK;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;

/**
 *
 * @author clemens
 */
public class ActivationTest {

  CardTerminal ct;
  Card icc;

  public void setUp() throws NoSuchAlgorithmException, CardException {

    IAIK.addAsJDK14Provider();

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
    byte[] historicalBytes = icc.getATR().getHistoricalBytes();
    System.out.println("found card " + toString(atr) + " " + new String(historicalBytes, Charset.forName("ASCII")) + "\n\n");

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

  public static void main(String[] args) throws NoSuchAlgorithmException, CardException {

    ActivationTest test = new ActivationTest();
    test.setUp();

  }

}
