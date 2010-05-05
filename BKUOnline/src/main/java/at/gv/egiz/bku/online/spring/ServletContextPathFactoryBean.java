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

package at.gv.egiz.bku.online.spring;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.web.context.ServletContextAware;

public class ServletContextPathFactoryBean implements FactoryBean, ServletContextAware {

  private String contextPath;

  @Override
  public void setServletContext(ServletContext servletContext) {
    contextPath = servletContext.getContextPath();
  }

  @Override
  public Object getObject() throws Exception {
    return contextPath;
  }

  @Override
  public Class<?> getObjectType() {
    return String.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

}
