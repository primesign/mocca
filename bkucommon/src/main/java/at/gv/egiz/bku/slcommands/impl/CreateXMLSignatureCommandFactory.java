/*
* Copyright 2009 Federal Chancellery Austria and
* Graz University of Technology
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
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
