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

public class File {
  public byte[] fid;
  public byte[] file;
  public byte[] fcx;
  public int kid = -1;

  public File(byte[] fid, byte[] file, byte[] fcx) {
    this.fid = fid;
    this.file = file;
    this.fcx = fcx;
  }

  public File(byte[] fid, byte[] file, byte[] fcx, int kid) {
    this.fid = fid;
    this.file = file;
    this.fcx = fcx;
    this.kid = kid;
  }

}