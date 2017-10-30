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
package org.springframework.data.cassandra.crypto.cipher;

import javax.crypto.Cipher;
import java.security.Provider;

public interface CipherFactory {

    /**
     * Creates and returns a new {@link Cipher} configured using settings known
     * to the factory implementation.
     *
     * @return a new Cipher that is guaranteed to be unused by other callers or
     *         null if the factory does not support cipher-based encryption.
     */
    Cipher cipher();

    String getTransformation();

    String getAlgorithm();

    String getMode();

    String getPadding();

    Provider getProvider();

    /**
     * Returns the block size for the ciphers created by this factory. This
     * information is needed for the callers to presize they various arrays
     * before a cipher is available.
     */
    int blockSize();
}
