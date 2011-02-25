package at.gv.egiz.smcc.util;



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

public class TLV {

  private byte[] bytes;
  private int start;

  public TLV(byte[] bytes, int start) {
    if (bytes.length - start < 2) {
      throw new IllegalArgumentException("TLV must at least consit of tag and length.");
    }
    this.bytes = bytes;
    this.start = start;
  }

  /**
   * @return the tag
   */
  public int getTag() {
    return 0xFF & bytes[start];
  }

	public int getLengthFieldLength() {
		if ((bytes[start + 1] & 0x80) > 0) {
			// ISO 7816 allows length fields of up to 5 bytes
			return 1 + (bytes[start + 1] & 0x07);
		}
		return 1;
	}

  /**
   * @return the length
   */
  public int getLength() {

		if ((bytes[start + 1] & 0x80) > 0) {
			int length = 0;
			for (int i = 0; i < (bytes[start + 1] & 0x07); i++) {
				length <<= 8;
				length += bytes[start + 2 + i] & 0xff;
			}
			return length;
		}
		return bytes[start + 1] & 0x7f;
  }

  /**
   * @return the value
   */
  public byte[] getValue() {
    byte[] value = new byte[getLength()];
    System.arraycopy(bytes, start + 1 + getLengthFieldLength(), value, 0, value.length);
    return value;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Tag = " + Integer.toHexString(getTag()) + ", Length = " + getLength() + ", Value = " + toString(getValue());
  }

  public static String toString(byte[] b) {
    StringBuffer sb = new StringBuffer();
    sb.append('[');
    if (b != null && b.length > 0) {
      sb.append(Integer.toHexString((b[0] & 240) >> 4));
      sb.append(Integer.toHexString(b[0] & 15));
      for (int i = 1; i < b.length; i++) {
        sb.append((i % 32 == 0) ? '\n' : ':');
        sb.append(Integer.toHexString((b[i] & 240) >> 4));
        sb.append(Integer.toHexString(b[i] & 15));
      }
    }
    sb.append(']');
    return sb.toString();
  }

}
