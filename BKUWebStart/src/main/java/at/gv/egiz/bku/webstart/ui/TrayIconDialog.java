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
package at.gv.egiz.bku.webstart.ui;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TrayIconDialog implements TrayIconDialogInterface {
  public static final String TRAYICON_RESOURCE = "at/gv/egiz/bku/webstart/ui/trayicon.png";
  public static final String TRAYMENU_SHUTDOWN = "TrayMenu.Shutdown";
  public static final String TRAYMENU_TOOLTIP = "TrayMenu.Tooltip";

  private static Log log = LogFactory.getLog(TrayIconDialog.class);
  private static TrayIconDialogInterface instance;
  private boolean isSupported;
  private BKUControllerInterface shutDown;
  private TrayIcon trayIcon = null;
  private ResourceBundle resourceBundle = null;

  private TrayIconDialog() {
  }

  private void displayTrayMsg(String captionID, String messageID,
      TrayIcon.MessageType type) {
    if ((isSupported) && (resourceBundle != null)) {
      try {
        trayIcon.displayMessage(resourceBundle.getString(captionID),
            resourceBundle.getString(messageID), type);
      } catch (Exception ex) {
        log.error(ex);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * at.gv.egiz.bku.local.ui.TrayIconDialogInterface#displayInfo(java.lang.String
   * , java.lang.String)
   */
  public void displayInfo(String captionID, String messageID) {
    displayTrayMsg(captionID, messageID, TrayIcon.MessageType.INFO);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * at.gv.egiz.bku.local.ui.TrayIconDialogInterface#displayWarning(java.lang
   * .String, java.lang.String)
   */
  public void displayWarning(String captionID, String messageID) {
    displayTrayMsg(captionID, messageID, TrayIcon.MessageType.WARNING);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * at.gv.egiz.bku.local.ui.TrayIconDialogInterface#displayError(java.lang.
   * String, java.lang.String)
   */
  public void displayError(String captionID, String messageID) {
    displayTrayMsg(captionID, messageID, TrayIcon.MessageType.ERROR);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * at.gv.egiz.bku.local.ui.TrayIconDialogInterface#init(java.util.ResourceBundle
   * )
   */
  public void init(ResourceBundle resourceBundel) {
    this.resourceBundle = resourceBundel;
    isSupported = SystemTray.isSupported();
    log.info("Trayicon supported: " + isSupported);
    try {
      if (isSupported) {
        SystemTray tray = SystemTray.getSystemTray();
        Image image = ImageIO.read(getClass().getClassLoader()
            .getResourceAsStream(TRAYICON_RESOURCE));
        PopupMenu popup = new PopupMenu();
        MenuItem exitItem = new MenuItem(resourceBundel
            .getString(TRAYMENU_SHUTDOWN));
        popup.add(exitItem);
        exitItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            log.info("Calling Shutdown");
            if (shutDown != null) {
              shutDown.shutDown();
            }
          }
        });

        trayIcon = new TrayIcon(image, "BKULogo", popup);
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip(resourceBundel.getString(TRAYMENU_TOOLTIP));
        try {
          tray.add(trayIcon);
        } catch (AWTException e) {
          log.error("TrayIcon could not be added.", e);
          isSupported = false;
        }
      }
    } catch (IOException e) {
      log.error(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * at.gv.egiz.bku.local.ui.TrayIconDialogInterface#setShutdownHook(at.gv.egiz
   * .bku.local.ui.BKUControllerInterface)
   */
  public void setShutdownHook(BKUControllerInterface shutDown) {
    this.shutDown = shutDown;
  }

  @SuppressWarnings("unchecked")
  public synchronized static TrayIconDialogInterface getInstance() {
    ClassLoader cl = TrayIconDialog.class.getClassLoader();
    if (instance == null) {
      if (cl.toString().equals(cl.getParent().toString())) {
        instance = new TrayIconDialog();
        return instance;
      }
      ClassLoader parent = cl;
      while (!parent.toString().equals(cl.getParent().toString())) {
        parent = parent.getParent();
      }
      try {
        Class<TrayIconDialog> otherClassInstance = (Class<TrayIconDialog>) parent
            .loadClass(TrayIconDialog.class.getName());
        Method getInstanceMethod = otherClassInstance.getDeclaredMethod(
            "getInstance", new Class[] {});
        Object otherSingleton = getInstanceMethod.invoke(null, new Object[] {});
        instance = (TrayIconDialogInterface) Proxy.newProxyInstance(cl,
            new Class[] { TrayIconDialogInterface.class },
            new PassThroughProxyHandler(otherSingleton));
      } catch (ClassNotFoundException ce) {
        instance = new TrayIconDialog();
      } catch (Exception e) {
        log.error(e);
        instance = new TrayIconDialog();
      }
      return instance;
    }
    return instance;
  }

  /**
   * 
   * Only works for public methods
   * 
   */
  static class PassThroughProxyHandler implements InvocationHandler {
    private final Object delegate;

    public PassThroughProxyHandler(Object delegate) {
      this.delegate = delegate;
    }

    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {
      Method delegateMethod = delegate.getClass().getMethod(method.getName(),
          method.getParameterTypes());
      return delegateMethod.invoke(delegate, args);
    }
  }

}
