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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Locale;

import at.gv.egiz.bku.slcommands.SLCommandFactory;
import at.gv.egiz.bku.slcommands.SLCommandInvoker;
import at.gv.egiz.stal.STAL;

/**
 * BindingProcessors implement the processing of a specific protocol binding
 * (e.g. HTTP) for Security Layer requests.
 * 
 * @author wbauer, mcentner
 */
public interface BindingProcessor extends Runnable {

  /**
   * Sets the command factory for creating Security Layer. Must be set before
   * {@link #consumeRequestStream(String, InputStream)} is called.
   * 
   * @param slCommandFactory
   *          the command factory for creating Security Layer commands.
   */
  void setSlCommandFactory(SLCommandFactory slCommandFactory);

  /**
   * Sets the preferred locale for user interaction. If the locale is not set
   * the default locale will be used. Should be set before
   * {@link #consumeRequestStream(String, InputStream)} is called to allow for a
   * proper localization.
   * 
   * @param locale
   *          must not be null.
   */
  public void setLocale(Locale locale);

  /**
   * Instructs this BindingProcessor to consume the request
   * <code>inputStream</code>.
   * <p>
   * Implementing classes are assumed to read the entire provided
   * <code>inputStream</code>
   * </p>
   * <p>
   * Any errors are reported via the result produced by this BindingProcessor.
   * </p>
   * 
   * @param url
   *          the URL request is associated with (e.g. has been received on).
   * 
   * @see BindingProcessor#writeResultTo(OutputStream, String)
   */
  public void consumeRequestStream(String url, InputStream aIs);

  /**
   * Initialize this BindingProcessor for processing. This method must be called
   * before {@link #run()} is called.
   * 
   * @param id
   *          the (unique) processing id (usually a HTTP session id)
   * @param stal
   *          the STAL
   * @param commandInvoker
   *          the CommandInvoker
   * @throws NullPointerException
   *           if one of the provided parameters is <code>null</code>
   */
  public void init(String id, STAL stal, SLCommandInvoker commandInvoker);

  /**
   * Returns the unique processing id.
   * 
   * @return the unique processing id or <code>null</code> if not yet assigned.
   */
  public Id getId();

  /**
   * Returns the STAL used for processing.
   * 
   * @return the STAL used for processing or <code>null</code> if not yet
   *         assigned.
   */
  public STAL getSTAL();

  /**
   * Returns the CommandInvoker used for processing.
   * 
   * @return the CommandInvoker used for processing or <code>null</code> if not
   *         yet assigned.
   */
  public SLCommandInvoker getCommandInvoker();

  /**
   * Returns the <code>ContentType</code> of the processing result.
   * 
   * @return the <code>ContentType</code> type of the processing result or
   *         <code>null</code> if a result is not yet available.
   */
  public String getResultContentType();

  /**
   * Writes the processing result to the given <code>outputStream</code> using
   * the given character <code>encoding</code>.
   * 
   * @param outputStream
   *          the OutputStream to write the result to
   * @param encoding
   *          the character encoding to be used
   * @throws IOException
   *           if writing to <code>outputStream</code> fails for any reason
   */
  public void writeResultTo(OutputStream outputStream, String encoding)
      throws IOException;

  /**
   * Returns the time of the last access to this BindingProcessor instance.
   * 
   * @return the time of the last access to this BindingProcessor instance.
   */
  public Date getLastAccessTime();

  /**
   * Updates the time this BindingProcessor was accessed last.
   */
  public void updateLastAccessTime();

}