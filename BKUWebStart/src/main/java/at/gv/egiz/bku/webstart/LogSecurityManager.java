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
package at.gv.egiz.bku.webstart;

import com.sun.javaws.security.JavaWebStartSecurity;
import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JVM argument -Djava.security.debug=access,failure
 * (passed as attribute to java element in jnlp) is ignored.
 * 
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class LogSecurityManager extends SecurityManager {

  protected static final Log log = LogFactory.getLog(LogSecurityManager.class);
  JavaWebStartSecurity sm;

  public LogSecurityManager(JavaWebStartSecurity sm) {
    this.sm = sm;
//    AppPolicy policy = AppPolicy.getInstance();
//    SecurityManager sm = System.getSecurityManager();
  }

  @Override
  public void checkAccept(String host, int port) {
    try {
      sm.checkAccept(host, port);
    } catch (SecurityException ex) {
      log.warn("checkAccept(" + host + ", " + port + "): " + ex.getMessage(), ex);
      throw ex;
    }
  }

  @Override
  public void checkAccess(Thread g) {
    try {
      sm.checkAccess(g);
    } catch (SecurityException ex) {
      log.warn("checkAccess(" + g + "): " + ex.getMessage(), ex);
      throw ex;
    }
  }

  @Override
  public void checkAccess(ThreadGroup g) {
    try {
      sm.checkAccess(g);
    } catch (SecurityException ex) {
      log.warn("checkAccess(" + g + "): " + ex.getMessage(), ex);
      throw ex;
    }

  }

  @Override
  public void checkAwtEventQueueAccess() {
    try {
      sm.checkAwtEventQueueAccess();
    } catch (SecurityException ex) {
      log.warn("checkAwtEventQAccess():" + ex.getMessage(), ex);
      throw ex;
    }

  }

  @Override
  public void checkConnect(String host, int port) {
    try {
      sm.checkConnect(host, port);
    } catch (SecurityException ex) {
      log.warn("checkConnect(" + host + ", " + port + "): " + ex.getMessage(), ex);
      throw ex;
    }
  }

  @Override
  public void checkConnect(String host, int port, Object context) {
    try {
      sm.checkConnect(host, port, context);
    } catch (SecurityException ex) {
      log.warn("checkConnect(" + host + ", " + port + ", " + context + "): " + ex.getMessage(), ex);
      throw ex;
    }
  }

  @Override
  public void checkCreateClassLoader() {
    try {
      sm.checkCreateClassLoader();
    } catch (SecurityException ex) {
      log.warn("checkCreateClassLoader(): " + ex.getMessage(), ex);
      throw ex;
    }
  }

  @Override
  public void checkDelete(String file) {
    try {
      sm.checkDelete(file);
    } catch (SecurityException ex) {
      log.warn("checkDelete(" + file + "): " + ex.getMessage(), ex);
      throw ex;
    }
  }

  @Override
  public void checkExec(String cmd) {
    try {
      sm.checkExec(cmd);
    } catch (SecurityException ex) {
      log.warn("checkExec(" + cmd + "): " + ex.getMessage(), ex);
      throw ex;
    }
  }

  @Override
  public void checkExit(int status) {
    try {
      sm.checkExit(status);
    } catch (SecurityException ex) {
      log.warn("checkExit(" + status + "): " + ex.getMessage(), ex);
      throw ex;
    }
  }

  @Override
  public void checkLink(String lib) {
    try {
      sm.checkLink(lib);
    } catch (SecurityException ex) {
      log.warn("checkLink(" + lib + "): " + ex.getMessage(), ex);
      throw ex;
    }
  }

  @Override
  public void checkListen(int port) {
    try {
      sm.checkListen(port);
    } catch (SecurityException ex) {
      log.warn("checkListen(" + port + "): " + ex.getMessage(), ex);
      throw ex;
    }
  }

  @Override
  public void checkMemberAccess(Class<?> clazz, int which) {
    try {
      sm.checkMemberAccess(clazz, which);
    } catch (SecurityException ex) {
      log.warn("checkMemberAccess(" + clazz + "): " + ex.getMessage(), ex);
      throw ex;
    }
  }

  @Override
  public void checkMulticast(InetAddress maddr) {
    try {
      sm.checkMulticast(maddr);
    } catch (SecurityException ex) {
      log.warn("checkMulticast(" + maddr + "): " + ex.getMessage(), ex);
      throw ex;
    }
  }

  @Override
  public void checkMulticast(InetAddress maddr, byte ttl) {
    try {
      sm.checkMulticast(maddr,ttl);
    } catch (SecurityException ex) {
      log.warn("checkMulticast(" + maddr + "," + ttl + "): " + ex.getMessage(), ex);
      throw ex;
    }
  }

  @Override
  public void checkPackageAccess(String pkg) {
    try {
      sm.checkPackageAccess(pkg);
    } catch (SecurityException ex) {
      log.warn("checkPackageAccess(" + pkg + "): " + ex.getMessage(), ex);
      throw ex;
    }
  }

  @Override
  public void checkPackageDefinition(String pkg) {
    try {
      sm.checkPackageDefinition(pkg);
    } catch (SecurityException ex) {
      log.warn("checkPackageDefinition(" + pkg + "): " + ex.getMessage(), ex);
      throw ex;
    }
  }

  @Override
  public void checkPermission(Permission perm) {
    try {
      sm.checkPermission(perm);
    } catch (SecurityException ex) {
      log.warn("checkPermission(" + perm.toString() + "): " + ex.getMessage(), ex);
      throw ex;
    }
  }

  @Override
  public void checkPermission(Permission perm, Object context) {
    try {
      sm.checkPermission(perm, context);
    } catch (SecurityException ex) {
      log.warn("checkPermission(" + perm.toString() + ", ctx): " + ex.getMessage(), ex);
      throw ex;
    }
  }

  @Override
  public void checkPrintJobAccess() {
    try {
      sm.checkPrintJobAccess();
    } catch (SecurityException ex) {
      log.info("checkPrintJobAccess(): " + ex.getMessage(), ex);
      throw ex;
    }
  }

  /**
   * allowed
   */
  @Override
  public void checkPropertiesAccess() {
    try {
      sm.checkPropertiesAccess();
    } catch (SecurityException ex) {
      log.info("checkPropertiesAccess(): " + ex.getMessage(), ex);
      throw ex;
    }
  }

  /**
   * access to all properties allowed
   * @param key
   */
  @Override
  public void checkPropertyAccess(String key) {
    try {
      sm.checkPropertyAccess(key);
    } catch (SecurityException ex) {
      log.info("checkPropertyAccess(" + key + "): " + ex.getMessage());
      throw ex;
    }
  }

  @Override
  public void checkRead(FileDescriptor fd) {
    try {
      sm.checkRead(fd);
    } catch (SecurityException ex) {
      log.warn("checkRead(" + fd + ") " + ex.getMessage(), ex);
      throw ex;
    }
  }

  @Override
  public void checkRead(String file) {
    try {
      sm.checkRead(file);
    } catch (SecurityException ex) {
      log.warn("checkRead(" + file + ") " + ex.getMessage(), ex);
      throw ex;
    }
  }

  @Override
  public void checkRead(String file, Object context) {
    try {
      sm.checkRead(file, context);
    } catch (SecurityException ex) {
      log.warn("checkRead(" + file + ") " + ex.getMessage(), ex);
      throw ex;
    }
  }

  @Override
  public void checkSecurityAccess(String target) {
    try {
      sm.checkSecurityAccess(target);
    } catch (SecurityException ex) {
      log.info("checkSecurityAccess(" + target + "): " + ex.getMessage(), ex);
      throw ex;
    }
  }

  @Override
  public void checkSetFactory() {
    log.info("checkSetFactory() ");
    try {
      sm.checkSetFactory();
    } catch (SecurityException ex) {
      log.warn("checkSetFactroy(): " + ex.getMessage(), ex);
      throw ex;
    }

  }

  @Override
  public void checkSystemClipboardAccess() {
    try {
      sm.checkSystemClipboardAccess();
    } catch (SecurityException ex) {
      log.info("checkSystemClipboardAccess(): " + ex.getMessage(), ex);
      throw ex;
    }
  }

  @Override
  public boolean checkTopLevelWindow(Object window) {
    log.info("checkTopLevelWindow(Object window)");
    try {
      return sm.checkTopLevelWindow(window);
    } catch (SecurityException ex) {
      log.warn("checkTopLevelWindow(" + window + "): " + ex.getMessage(), ex);
      throw ex;
    }

  }

  @Override
  public void checkWrite(FileDescriptor fd) {
    try {
      sm.checkWrite(fd);
    } catch (SecurityException ex) {
      log.info("checkWrite(" + fd + "): " + ex.getMessage(), ex);
    }
  }

  @Override
  public void checkWrite(String file) {
    try {
      sm.checkWrite(file);
    } catch (SecurityException ex) {
      log.info("checkWrite(" + file + "): " + ex.getMessage(), ex);
    }
  }

//  @Override
//  protected int classDepth(String name) {
//    log.info("classDepth(String name)"); return this.classDepth(name);
//  }
//
//  @Override
//  protected int classLoaderDepth() {
//    log.info("classLoaderDepth"); return sm.classLoaderDepth();
//  }
//
//  @Override
//  protected Object clone() throws CloneNotSupportedException {
//    log.info("clone"); return sm.clone();
//  }
//
//  @Override
//  protected ClassLoader currentClassLoader() {
//    log.info("currentClassLoader"); return sm.currentClassLoader();
//  }
//
//  @Override
//  protected Class<?> currentLoadedClass() {
//    log.info("currentLoadedClass"); return sm.currentLoadedClass();
//  }
  @Override
  public boolean equals(Object obj) {
    log.info("equals");
    return sm.equals(obj);
  }

//  @Override
//  protected void finalize() throws Throwable {
//    log.info("finalize"); sm.finalize();
//  }
//  @Override
//  protected Class[] getClassContext() {
//    log.info("getClassContext"); return sm.getClassContext();
//  }
  @Override
  public boolean getInCheck() {
    log.info("getInCheck");
    return sm.getInCheck();
  }

  @Override
  public Object getSecurityContext() {
    log.info("getSecurityContext");
    return sm.getSecurityContext();
  }

  @Override
  public ThreadGroup getThreadGroup() {
    log.info("getThreadGroup");
    return sm.getThreadGroup();
  }

  @Override
  public int hashCode() {
    log.info("hashCode");
    return sm.hashCode();
  }

//  @Override
//  protected boolean inClass(String name) {
//    log.info("inClass"); return sm.inClass(name);
//  }
//
//  @Override
//  protected boolean inClassLoader() {
//    log.info(""); return sm.inClassLoader();
//  }
  @Override
  public String toString() {
    log.info("toString");
    return sm.toString();
  }
}
