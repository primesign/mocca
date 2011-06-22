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



package at.gv.egiz.bku.slcommands.impl;

import javax.xml.bind.JAXBElement;

import org.apache.commons.configuration.Configuration;

import at.gv.egiz.bku.conf.MoccaConfigurationFacade;
import at.gv.egiz.bku.slcommands.AbstractSLCommandFactory;
import at.gv.egiz.bku.slcommands.SLCommand;
import at.gv.egiz.bku.slcommands.impl.xsect.DataObject;
import at.gv.egiz.bku.slexceptions.SLCommandException;

public class CreateXMLSignatureCommandFactory extends AbstractSLCommandFactory {

  private ConfigurationFacade configurationFacade = new ConfigurationFacade();
  
  private class ConfigurationFacade implements MoccaConfigurationFacade {
    public static final String VALIDATE_HASH_DATA_INPUTS = "ValidateHashDataInputs";

    public boolean getValidateHashDataInputs() {
      return configuration.getBoolean(VALIDATE_HASH_DATA_INPUTS, true);
    }
  }

  @Override
  public SLCommand createSLCommand(JAXBElement<?> element) throws SLCommandException {
    
    CreateXMLSignatureCommandImpl command = new CreateXMLSignatureCommandImpl();
    command.init(element);
    command.setConfiguration(configuration);
    return command;
    
  }

  /* (non-Javadoc)
   * @see at.gv.egiz.bku.slcommands.AbstractSLCommandFactory#setConfiguration(org.apache.commons.configuration.Configuration)
   */
  @Override
  public void setConfiguration(Configuration configuration) {
    // static configuration
    super.setConfiguration(configuration);
    DataObject.enableHashDataInputValidation(configurationFacade.getValidateHashDataInputs());
  }

  
  
}
