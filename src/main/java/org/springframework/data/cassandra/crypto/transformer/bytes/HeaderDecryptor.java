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

import org.springframework.data.cassandra.crypto.key.KeySource;

import java.security.Key;

class HeaderDecryptor implements BytesDecryptor {
    private KeySource keySource;
    private BytesDecryptor delegate;
    private BytesDecryptor decompressDelegate;

    HeaderDecryptor(BytesDecryptor delegate, BytesDecryptor decompressDelegate, KeySource keySource) {
        this.delegate = delegate;
        this.keySource = keySource;
        this.decompressDelegate = decompressDelegate;
    }

    @Override
    public byte[] decrypt(byte[] input, int inputOffset, Key key) {

        Header header = Header.create(input, inputOffset);

        // ignoring the parameter key... using the key from the first block
        Key inRecordKey = keySource.getKey(header.getKeyName());

        // if compression was used to create a record, filter through GzipDecryptor...
        BytesDecryptor worker = header.isCompressed() ? decompressDelegate : delegate;
        // if record has HMAC, create appropriate decryptor
        if(header.haveHMAC()) {
            worker = new HmacDecryptor(worker, header, inRecordKey);
        }

        return worker.decrypt(input, inputOffset + header.size(), inRecordKey);
    }
}
