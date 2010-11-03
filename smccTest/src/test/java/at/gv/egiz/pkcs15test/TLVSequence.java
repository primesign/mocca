package at.gv.egiz.pkcs15test;

import java.util.Iterator;
import java.util.NoSuchElementException;

/*
 * Copyright 2009 Federal Chancellery Austria and
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

public class TLVSequence implements Iterable<TLV> {
  
  private byte[] bytes;
  
  public TLVSequence(byte[] bytes) {
    this.bytes = bytes;
  }

  @Override
  public Iterator<TLV> iterator() {
    return new TLVIterator();
  }
  
  public byte[] getValue(int tag) {
    for (TLV tlv : this) {
      if (tlv.getTag() == tag) {
        return tlv.getValue();
      }
    }
    return null;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (TLV tlv : this) {
      sb.append(tlv).append('\n');
    }
    return sb.toString();
  }

  private class TLVIterator implements Iterator<TLV> {

    private int pos = 0;
    
    @Override
    public boolean hasNext() {
      return (bytes.length - pos > 2);
    }

    @Override
    public TLV next() {
      if (hasNext()) {
        TLV tlv = new TLV(bytes, pos);
        pos += tlv.getLength() + 2;
        return tlv;
      } else {
        throw new NoSuchElementException();
      }
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
    
  }
  
}
