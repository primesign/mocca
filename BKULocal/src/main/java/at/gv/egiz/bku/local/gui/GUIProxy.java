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
package at.gv.egiz.bku.local.gui;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import javax.swing.JFrame;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class GUIProxy implements InvocationHandler {

  JFrame frame;
  BKUGUIFacade delegate;

  static public Object newInstance(BKUGUIFacade gui, JFrame frame, Class<?>[] interfaces) {
    return java.lang.reflect.Proxy.newProxyInstance(gui.getClass().getClassLoader(),
            interfaces,
            new GUIProxy(gui, frame));
  }

  private GUIProxy(BKUGUIFacade delegate, JFrame frame) {
    this.frame = frame;
    this.delegate = delegate;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

    if (method.getName().startsWith("show")) {
      frame.setVisible(true);
      frame.toFront();
      return method.invoke(delegate, args);
    } else { //if (method.getName().startsWith("get")) {
      return method.invoke(delegate, args);
    }
  }
}
