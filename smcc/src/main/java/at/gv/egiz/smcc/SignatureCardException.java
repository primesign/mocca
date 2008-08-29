//Copyright (C) 2002 IAIK
//http://jce.iaik.at
//
//Copyright (C) 2003 Stiftung Secure Information and 
//                 Communication Technologies SIC
//http://www.sic.st
//
//All rights reserved.
//
//This source is provided for inspection purposes and recompilation only,
//unless specified differently in a contract with IAIK. This source has to
//be kept in strict confidence and must not be disclosed to any third party
//under any circumstances. Redistribution in source and binary forms, with
//or without modification, are <not> permitted in any case!
//
//THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
//ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
//IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
//ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
//FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
//DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
//OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
//HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
//LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
//OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
//SUCH DAMAGE.
//
//
package at.gv.egiz.smcc;

public class SignatureCardException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * Creates a new instance of this <code>SignatureCardException</code>.
   * 
   */
  public SignatureCardException() {
    super();
  }

  /**
   * Creates a new instance of this <code>SignatureCardException</code>.
   * 
   * @param message
   * @param cause
   */
  public SignatureCardException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Creates a new instance of this <code>SignatureCardException</code>.
   * 
   * @param message
   */
  public SignatureCardException(String message) {
    super(message);
  }

  /**
   * Creates a new instance of this <code>SignatureCardException</code>.
   * 
   * @param cause
   */
  public SignatureCardException(Throwable cause) {
    super(cause);
  }

  
  
}
