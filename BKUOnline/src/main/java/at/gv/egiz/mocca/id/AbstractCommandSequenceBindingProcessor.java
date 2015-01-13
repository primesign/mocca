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



package at.gv.egiz.mocca.id;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.binding.AbstractBindingProcessor;
import at.gv.egiz.bku.slcommands.SLCommand;
import at.gv.egiz.bku.slcommands.SLCommandContext;
import at.gv.egiz.bku.slcommands.SLResult;
import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.bku.slexceptions.SLException;
import at.gv.egiz.stal.QuitRequest;

public abstract class AbstractCommandSequenceBindingProcessor extends AbstractBindingProcessor {

  protected static Logger log = LoggerFactory.getLogger(AbstractCommandSequenceBindingProcessor.class);
  
  /**
   * @return the error
   */
  protected Exception getError() {
    return error;
  }

  /**
   * @param error the error to set
   */
  protected void setError(Exception error) {
    this.error = error;
  }

  private Exception error;

  private SLCommandBroker commandBroker = new SLCommandBroker();
  
  /**
   * External processing?
   */
  private boolean external;
  
  /**
   * Constructs a new instance of this IdBindingProcessorImpl with
   * the given ID.
   */
  public AbstractCommandSequenceBindingProcessor() {
    super();
  }

  /**
   * @return the external
   */
  public boolean isExternal() {
    return external;
  }

  /**
   * @param external the external to set
   */
  public void setExternal(boolean external) {
    this.external = external;
  }

  protected abstract SLCommand getNextCommand();
  
  protected abstract void processResult(SLResult result);
  
  @Override
  public synchronized void process() {

    try {
    
      SLCommand command;
      do {
        command = getNextCommand();
        SLCommandContext context = new SLCommandContext(getSTAL(), getUrlDereferencer(), null, locale);
        SLResult result = null;
        if (external) {
          result = commandBroker.execute(command, context, 3 * 60 * 1000);
        } else {
          if (command != null) {
            result = command.execute(context);
          } else {
            stal.handleRequest(Collections.singletonList(new QuitRequest()));
          }
        }
        if (result != null) {
          processResult(result);
        }
      } while (command != null);
      
    } catch (InterruptedException e) {
      setError(new SLException(6000));
    } catch (Exception e) {
      log.info("BindingProcessor error.", e);
      setError(e);
    }
    
  }

  public SLCommand setExternalResult(SLResult slResult) throws SLCommandException, InterruptedException {
    return commandBroker.nextCommand(slResult, 3 * 60 * 1000);
  }

}
