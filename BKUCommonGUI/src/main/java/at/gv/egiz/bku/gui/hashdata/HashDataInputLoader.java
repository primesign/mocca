package at.gv.egiz.bku.gui.hashdata;

import at.gv.egiz.stal.HashDataInput;

public interface HashDataInputLoader {

  
  
  /**
   * Loads input data of referenced HashDataInput.
   * @param hashDataInput HashDataInput without content that references a HashDataInput at server side with digest or referenceId.
   * @return HashDataInput Referenced HashDataInput from server-side including content.
   * @throws Exception
   */
  HashDataInput getHashDataInput(HashDataInput hashDataInput) throws Exception;
}
