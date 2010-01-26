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
package at.gv.egiz.bku.slcommands.impl;

import at.gv.egiz.bku.slcommands.SLCommandContext;
import at.gv.egiz.bku.slexceptions.SLCommandException;

/**
 * An abstract base class for implementations of security layer infobox requests.
 * 
 * @author mcentner
 *
 * @param <T>
 */
public abstract class AbstractInfoboxCommandImpl<T> extends SLCommandImpl<T> {

  /**
   * The infobox implementation.
   */
  protected Infobox infobox;
  
  @Override
  public void init(SLCommandContext ctx, Object request)
      throws SLCommandException {
    super.init(ctx, request);
    
    String infoboxIdentifier = getInfoboxIdentifier(getRequestValue());
    
    infobox = InfoboxFactory.getInstance().createInfobox(infoboxIdentifier);
  }
  
  /**
   * Returns the infobox identifier given in <code>request</code>.
   * 
   * @param request the request value
   * 
   * @return the infobox identifier givne in <code>request</code>
   */
  protected abstract String getInfoboxIdentifier(T request);


  public String getInfoboxIdentifier() {
    if (infobox != null) {
      return infobox.getIdentifier();
    } else {
      return null;
    }
  }
  
}
