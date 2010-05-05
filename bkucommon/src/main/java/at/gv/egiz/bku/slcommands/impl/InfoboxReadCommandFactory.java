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

import at.gv.egiz.bku.slcommands.SLCommand;
import at.gv.egiz.bku.slexceptions.SLCommandException;

public class InfoboxReadCommandFactory extends AbstractInfoboxCommandFactory {

  @Override
  public SLCommand createSLCommand(JAXBElement<?> element) throws SLCommandException {
    
    InfoboxReadCommandImpl command = new InfoboxReadCommandImpl();
    command.setInfoboxFactory(infoboxFactory);
    command.init(element);
    return command;
    
  }

}
