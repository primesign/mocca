/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.gv.egiz.bku.binding;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * BindingContext?
 * RequestBindingContext?
 * 
 * @author clemens
 */
public class ProcessingContext {

  public static final String BINDING_PROCESSOR = "binding.processor";
  public static final String FUTURE = "future";
  
  protected Map<String, Object> properties = new Hashtable<String, Object>();

  public ProcessingContext(BindingProcessor bp, Future future) {
    properties.put(BINDING_PROCESSOR, bp);
    properties.put(FUTURE, future);
  }
  
  
  
  public BindingProcessor getBindingProcessor() {
    return (BindingProcessor) properties.get(BINDING_PROCESSOR);
  }
  
  public Future getFuture() {
    return (Future) properties.get(FUTURE); 
  }
}
