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
package at.gv.egiz.bku.utils.urldereferencer;

public class SimpleFormDataContextImpl implements URLDereferencerContext {
  protected FormDataURLSupplier formdata;
  
  /**
   * 
   * @param formdata must not be null
   */
  public SimpleFormDataContextImpl(FormDataURLSupplier formdata) {
    if (formdata == null) {
      throw new NullPointerException("FormdataURLSupplier must not be null");
    }
    this.formdata = formdata;
  }

  @Override
  public Object getProperty(Object key) {
    if (key.equals(FormDataURLSupplier.PROPERTY_KEY_NAME)) {
      return formdata;
    }   
    return null;
  }

}
