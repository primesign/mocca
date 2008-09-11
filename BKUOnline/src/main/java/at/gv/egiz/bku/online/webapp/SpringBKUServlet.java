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
package at.gv.egiz.bku.online.webapp;

import javax.servlet.http.HttpServlet;

import at.gv.egiz.bku.binding.BindingProcessorManager;
import at.gv.egiz.bku.online.conf.Configurator;

public abstract class SpringBKUServlet extends HttpServlet {

  public final static String BEAN_NAME="bindingProcessorManager";
  
  protected BindingProcessorManager getBindingProcessorManager() {
    return (BindingProcessorManager) getServletContext().getAttribute(BEAN_NAME);
  }
}
