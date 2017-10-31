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

import org.springframework.data.cassandra.crypto.CassandraCryptoException;

import java.security.Key;
import java.util.Arrays;

/**
 * This class not only parse HMAC but also verifies it
 * and throws {@link org.springframework.data.cassandra.crypto.CassandraCryptoException} in case it is invalid.
 */
class HmacDecryptor extends HmacCreator implements BytesDecryptor {
    final BytesDecryptor delegate;

    HmacDecryptor(BytesDecryptor delegate, Header header, Key key) {
        super(header, key);
        this.delegate = delegate;
    }

    @Override
    @SuppressWarnings("PMD")
    public byte[] decrypt(byte[] input, int inputOffset, Key key) {
        final byte hmacLength = input[inputOffset++];
        if(hmacLength <= 0) {
            throw new CassandraCryptoException("Input is corrupted: invalid HMAC length.");
        }

        final byte[] receivedHmac = new byte[hmacLength];
        final byte[] decrypted = delegate.decrypt(input, inputOffset + hmacLength, key);
        final byte[] realHmac = createHmac(decrypted);

        System.arraycopy(input, inputOffset, receivedHmac, 0, hmacLength);
        if(!Arrays.equals(receivedHmac, realHmac)) {
            throw new CassandraCryptoException("Input is corrupted: wrong HMAC.");
        }
        return decrypted;
    }
}
