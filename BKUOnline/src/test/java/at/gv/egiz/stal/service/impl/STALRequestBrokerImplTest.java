package at.gv.egiz.stal.service.impl;

import at.gv.egiz.stal.service.types.QuitRequestType;
import at.gv.egiz.stal.service.types.RequestType;
import at.gv.egiz.stal.service.types.ResponseType;
import org.junit.Test;

import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for class {@link STALRequestBrokerImpl}.
 *
 * @date 16.05.2018
 * @see STALRequestBrokerImpl
 **/
public class STALRequestBrokerImplTest {

  @Test
  public void testNextRequestProvidingNull() throws Exception {
    STALRequestBroker stalRequestBroker = new STALRequestBrokerImpl(1);
    List<JAXBElement<? extends RequestType>> jaxbElements = stalRequestBroker.nextRequest(null);

    assertEquals(1, jaxbElements.size());
    assertFalse(jaxbElements.get(0).isNil());
    assertTrue(jaxbElements.get(0).getValue() instanceof QuitRequestType);
  }

  @Test
  public void testNextRequestProvidingEmptyList() throws Exception {
    STALRequestBroker stalRequestBroker = new STALRequestBrokerImpl(1);
    List<JAXBElement<? extends RequestType>> jaxbElements = stalRequestBroker.nextRequest(new ArrayList<JAXBElement<? extends ResponseType>>());

    assertEquals(1, jaxbElements.size());
    assertFalse(jaxbElements.get(0).isNil());
    assertTrue(jaxbElements.get(0).getValue() instanceof QuitRequestType);
  }


}