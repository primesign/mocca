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


package at.gv.egiz.smcc.util;

import java.io.IOException;
import java.io.InputStream;

public abstract class TransparentFileInputStream extends InputStream {

	// private final int chunkSize = 256;
	private int chunkSize = 256;

	private byte[] buf = new byte[chunkSize];
	private int start = 0;
	private int end = 0;

	private int offset = 0;

	private int length = -1;

	private int limit = -1;

	private int mark = -1;

	private int readlimit = -1;

	public TransparentFileInputStream() {
	}

	public TransparentFileInputStream(int length) {
		this.length = length;
	}

	public TransparentFileInputStream(int length, int chunkSize) {
		this.length = length;
		this.chunkSize = chunkSize;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	private int fill() throws IOException {
		if (start == end && (limit < 0 || offset < limit)) {
			int l;
			if (limit > 0 && limit - offset < chunkSize) {
				l = limit - offset;
			} else if (length > 0) {
				if (length - offset < chunkSize) {
					l = length - offset;
				} else {
					l = chunkSize - 1;
				}
			} else {
				l = chunkSize;
			}
			byte[] b = readBinary(offset, l);
			offset += b.length;
			if (mark < 0) {
				start = 0;
				end = b.length;
				System.arraycopy(b, 0, buf, start, b.length);
			} else {
				if (end - mark + b.length > buf.length) {
					// double buffer size
					byte[] nbuf = new byte[buf.length * 2];
					System.arraycopy(buf, mark, nbuf, 0, end - mark);
					buf = nbuf;
				} else {
					System.arraycopy(buf, mark, buf, 0, end - mark);
				}
				start = start - mark;
				end = end - mark + b.length;
				mark = 0;
				System.arraycopy(b, 0, buf, start, b.length);
			}
			if (l > b.length) {
				// end of file reached
				setLimit(offset);
			}
		}
		return end - start;
	}

	protected abstract byte[] readBinary(int offset, int len)
			throws IOException;

	@Override
	public int read() throws IOException {
		int b = (fill() > 0) ? 0xFF & buf[start++] : -1;
		if (readlimit > 0 && start > readlimit) {
			mark = -1;
			readlimit = -1;
		}
		return b;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		} else if (off < 0 || len < 0 || len > b.length - off) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return 0;
		}

		int count = 0;
		int l;
		while (count < len) {
			if (fill() > 0) {
				l = Math.min(end - start, len - count);
				System.arraycopy(buf, start, b, off, l);
				start += l;
				off += l;
				count += l;
				if (readlimit > 0 && start > readlimit) {
					mark = -1;
					readlimit = -1;
				}
			} else {
				return (count > 0) ? count : -1;
			}
		}

		return count;

	}

	@Override
	public synchronized void mark(int readlimit) {
		this.readlimit = readlimit;
		mark = start;
	}

	@Override
	public boolean markSupported() {
		return true;
	}

	@Override
	public synchronized void reset() throws IOException {
		if (mark < 0) {
			throw new IOException();
		} else {
			start = mark;
		}
	}

	@Override
	public long skip(long n) throws IOException {

		if (n <= 0) {
			return 0;
		}

		if (n <= end - start) {
			start += n;
			return n;
		} else {

			mark = -1;

			long remaining = n - (end - start);
			start = end;

			if (limit >= 0 && limit < offset + remaining) {
				remaining -= limit - offset;
				offset = limit;
				return n - remaining;
			}

			if (length >= 0 && length < offset + remaining) {
				remaining -= length - offset;
				offset = length;
				return n - remaining;
			}

			offset += remaining;

			return n;

		}

	}

}
