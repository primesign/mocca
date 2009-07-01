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
package at.gv.egiz.smcc;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import at.gv.egiz.smcc.util.TransparentFileInputStream;
import static org.junit.Assert.*;

public class TransparentFileInputStreamTest {
  
  public class TestTransparentFileInputStream extends TransparentFileInputStream {

    private byte[] data;
    
    public TestTransparentFileInputStream(byte[] data) {
      this.data = data;
    }

    @Override
    protected byte[] readBinary(int offset, int len) throws IOException {
      int l = Math.min(len, data.length - offset);
      byte[] b = new byte[l];
      System.arraycopy(data, offset, b, 0, l);
      return b;
    }
    
  }
  
  protected static byte[] file;
  
  protected static byte[] file_bs;
  
  @BeforeClass
  public static void setUpClass() {
    
    byte b = 0x00;
    file = new byte[1000];
    for (int i = 0; i < file.length; i++) {
      file[i] = b++;
    }
    
    file_bs = new byte[256];
    b = 0x00;
    for (int i = 0; i < file_bs.length; i++) {
      file_bs[i] = b++;
    }
    
  }

  @Test
  public void testReadSeq() throws IOException {
    
    TransparentFileInputStream is = new TestTransparentFileInputStream(file);
    int i = 0;
    int b; 
    while ((b = is.read()) != -1) {
      assertEquals(0xFF & i++, b);
    }
    assertEquals(file.length, i);
    
  }
  
  @Test
  public void testReadBlock() throws IOException {
    
    TransparentFileInputStream is = new TestTransparentFileInputStream(file);
    int i = 0;
    byte[] b = new byte[28];
    int l;
    while ((l = is.read(b)) != -1) {
      for(int j = 0; j < l; j++) {
        assertEquals(0xFF & i++, 0xFF & b[j]);
      }
    }
    assertEquals(file.length, i);
    
  }

  @Test
  public void testReadBlockBS() throws IOException {
    
    TransparentFileInputStream is = new TestTransparentFileInputStream(file_bs);
    int i = 0;
    byte[] b = new byte[28];
    int l;
    while ((l = is.read(b)) != -1) {
      for(int j = 0; j < l; j++) {
        assertEquals(0xFF & i++, 0xFF & b[j]);
      }
    }
    assertEquals(file_bs.length, i);
    
  }
  
  @Test(expected = IOException.class)
  public void testReset() throws IOException {
    
    TransparentFileInputStream is = new TestTransparentFileInputStream(file);
    is.read(new byte[128]);
    is.reset();
    
  }
  
  @Test
  public void testMark() throws IOException {
    
    TransparentFileInputStream is = new TestTransparentFileInputStream(file);
    int i = 0;
    is.mark(12);
    byte[] b = new byte[37];
    int l;
    while ((l = is.read(b)) != -1) {
      for(int j = 0; j < l; j++) {
        assertEquals(0xFF & i++, 0xFF & b[j]);
      }
    }
    assertEquals(file.length, i);
    
  }
  
  @Test
  public void testMarkReset() throws IOException {

    TransparentFileInputStream is = new TestTransparentFileInputStream(file);
    int i = 128;
    is.read(new byte[i]);
    is.mark(512);
    byte[] b = new byte[256];
    is.read(b);
    for(int j = 0; j < b.length; j++) {
      assertEquals(0xFF & i + j, 0xFF & b[j]);
    }
    is.reset();
    int l;
    while ((l = is.read(b)) != -1) {
      for(int j = 0; j < l; j++) {
        assertEquals(0xFF & i++, 0xFF & b[j]);
      }
    }
    assertEquals(file.length, i);
    
  }

  
  @Test(expected = IOException.class)
  public void testMarkResetLimit() throws IOException {

    TransparentFileInputStream is = new TestTransparentFileInputStream(file);
    int i = 128;
    is.read(new byte[i]);
    is.mark(128);
    byte[] b = new byte[256];
    is.read(b);
    for(int j = 0; j < b.length; j++) {
      assertEquals(0xFF & i + j, 0xFF & b[j]);
    }
    is.reset();
    
  }
  
  @Test
  public void testSkipSmall() throws IOException {
    
    TransparentFileInputStream is = new TestTransparentFileInputStream(file);
    int i = 0;
    i+= is.read(new byte[128]);
    i+= is.skip(3);
    byte[] b = new byte[256];
    int l = is.read(b);
    for (int j = 0; j < l; j++) {
      assertEquals(0xFF & i + j, 0xFF & b[j]);
    }
    
  }
  @Test
  public void testSkipBig() throws IOException {
    
    TransparentFileInputStream is = new TestTransparentFileInputStream(file);
    int i = 0;
    i+= is.read(new byte[128]);
    i+= is.skip(300);
    byte[] b = new byte[256];
    int l = is.read(b);
    for (int j = 0; j < l; j++) {
      assertEquals(0xFF & i + j, 0xFF & b[j]);
    }
    
  }

}
