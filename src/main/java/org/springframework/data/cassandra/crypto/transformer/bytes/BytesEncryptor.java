/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.cassandra.crypto.transformer.bytes;

public interface BytesEncryptor {

    /**
     * Transform input bytes using default encryption key.
     * 
     * @param input
     *            a buffer with unencrypted bytes.
     * @param outputOffset
     *            how much empty space to leave in the beginning of the returned
     *            output array. This would allow the caller to prepend extra
     *            data to the encrypted array.
     * @param flags
     *            a byte[1] that allows nested encryptors to manipulate header
     *            flags.
     */
    byte[] encrypt(byte[] input, int outputOffset, byte[] flags);

}
