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

import java.security.Key;

/**
 * Encryptor that stores authentication code into output.
 * HMAC is formed from full header concatenated with unencrypted input.
 */
class HmacEncryptor extends HmacCreator implements BytesEncryptor {

    BytesEncryptor delegate;

    HmacEncryptor(BytesEncryptor delegate, Header header, Key key) {
        super(header, key);
        this.delegate = delegate;
    }

    @Override
    public byte[] encrypt(byte[] input, int outputOffset, byte[] flags) {
        byte[] hmac = createHmac(input);
        byte[] encrypted = delegate.encrypt(input, outputOffset + hmac.length + 1, flags);
        encrypted[outputOffset++] = (byte)hmac.length; // store HMAC length
        System.arraycopy(hmac, 0, encrypted, outputOffset, hmac.length);
        return encrypted;
    }
}
