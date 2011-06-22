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
