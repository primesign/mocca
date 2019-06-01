package at.gv.egiz.bku.smccstal;

import at.gv.egiz.stal.*;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for class {@link StatusRequestHandler}.
 *
 * @see StatusRequestHandler
 **/
public class StatusRequestHandlerTest {

  @Test
  public void testHandleRequestProvidingEmptyStatusRequest() throws InterruptedException {
    StatusRequestHandler statusRequestHandler = new StatusRequestHandler();
    StatusRequest statusRequest = new StatusRequest();
    StatusResponse statusResponse = (StatusResponse)statusRequestHandler.handleRequest(statusRequest);

    assertFalse(statusRequestHandler.requireCard());
    assertFalse(statusResponse.isCardReady());
  }

  @Test
  public void testHandleRequestProvidingSignRequest() throws InterruptedException {
    StatusRequestHandler statusRequestHandler = new StatusRequestHandler();
    SignRequest signRequest = new SignRequest();
    STALResponse response = statusRequestHandler.handleRequest(signRequest);

    assertTrue(response instanceof ErrorResponse);
    assertEquals(1000, ((ErrorResponse) response).getErrorCode());
  }

  @Test
  public void testRequireCardIsSetToFalse() {
    StatusRequestHandler statusRequestHandler = new StatusRequestHandler();
    assertFalse(statusRequestHandler.requireCard());
  }

}