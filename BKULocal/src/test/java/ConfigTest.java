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
import java.net.URL;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationFactory;
import org.junit.Ignore;

@Ignore
public class ConfigTest {
  
  
  private void testConfig() throws ConfigurationException {
    ConfigurationFactory factory = new ConfigurationFactory();
    URL configURL = getClass().getResource("/config.xml");
    factory.setConfigurationURL(configURL);
    Configuration config = factory.getConfiguration();
    System.out.println("-------->: "+config.getInt("hans"));
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    ConfigTest ct = new ConfigTest();
    try {
      ct.testConfig();
    } catch (ConfigurationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
