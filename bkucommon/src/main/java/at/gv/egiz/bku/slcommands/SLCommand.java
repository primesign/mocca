/*
* Copyright 2008 Federal Chancellery Austria and
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
package at.gv.egiz.bku.slcommands;

import at.gv.egiz.bku.slexceptions.SLCommandException;

public interface SLCommand {
  
  public final String NAMESPACE_URI = "http://www.buergerkarte.at/namespaces/securitylayer/1.2#";
  
  public final String NAMESPACE_URI_20020225 = "http://www.buergerkarte.at/namespaces/securitylayer/20020225#";
  
  public final String NAMESPACE_URI_20020831 = "http://www.buergerkarte.at/namespaces/securitylayer/20020831#";
  
  public String getName();

  public void init(Object aUnmarshalledRequest) throws SLCommandException;

  public SLResult execute(SLCommandContext commandContext);

}