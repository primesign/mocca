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
package at.gv.egiz.bku.binding;

import java.util.Date;
import java.util.Locale;

import org.apache.commons.configuration.Configuration;
import org.slf4j.MDC;

import at.gv.egiz.bku.slcommands.SLCommandFactory;
import at.gv.egiz.bku.slcommands.SLCommandInvoker;
import at.gv.egiz.bku.utils.urldereferencer.URLDereferencer;
import at.gv.egiz.stal.STAL;

public abstract class AbstractBindingProcessor implements BindingProcessor {
  
  protected Configuration configuration;

  protected SLCommandFactory slCommandFactory;

  protected Locale locale = Locale.getDefault();

  protected Id id;
  protected STAL stal;
  protected SLCommandInvoker commandInvoker;
  
  protected long lastAccessedTime = System.currentTimeMillis();

  protected URLDereferencer urlDereferencer;

  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  public void setSlCommandFactory(SLCommandFactory slCommandFactory) {
    this.slCommandFactory = slCommandFactory;
  }

  @Override
  public void setLocale(Locale locale) {
  	if (locale == null) {
  		throw new NullPointerException("Locale must not be set to null.");
  	}
  	this.locale = locale;
  }

  @Override
  public Locale getLocale() {
    return locale;
  }
  
  @Override
  public void init(String id, STAL stal, SLCommandInvoker commandInvoker) {
    if (id == null) {
      throw new NullPointerException("Id must not be null.");
    }
    if (stal == null) {
      throw new NullPointerException("STAL must not null.");
    }
    if (commandInvoker == null) {
      throw new NullPointerException("CommandInvoker must null.");
    }
    this.id = IdFactory.getInstance().createId(id);
    this.stal = stal;
    this.commandInvoker = commandInvoker;
  }

  @Override
  public Id getId() {
    return id;
  }

  @Override
  public STAL getSTAL() {
    return stal;
  }

  @Override
  public SLCommandInvoker getCommandInvoker() {
    return commandInvoker;
  }

  @Override
  public void updateLastAccessTime() {
    lastAccessedTime = System.currentTimeMillis();
  }

  @Override
  public Date getLastAccessTime() {
    return new Date(lastAccessedTime);
  }

  @Override
  public void run() {
    
    if (this.id != null) {
      MDC.put("id", this.id.toString());
    }
    try {
      process();
    } finally {
      MDC.remove("id");
    }
    
  }
  
  public abstract void process();

  /**
   * @return the urlDereferencer
   */
  public URLDereferencer getUrlDereferencer() {
    return urlDereferencer;
  }

  /**
   * @param urlDereferencer the urlDereferencer to set
   */
  public void setUrlDereferencer(URLDereferencer urlDereferencer) {
    this.urlDereferencer = urlDereferencer;
  }
  
}