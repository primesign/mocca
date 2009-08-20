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
package at.gv.egiz.bku.smccstal;

import at.gv.egiz.smcc.SignatureCardException;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class GetPINStatusException extends SignatureCardException {

    /**
     * Creates a new instance of <code>GetStatusException</code> without detail message.
     */
    public GetPINStatusException() {
    }


    /**
     * Constructs an instance of <code>GetStatusException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public GetPINStatusException(String msg) {
        super(msg);
    }
}
