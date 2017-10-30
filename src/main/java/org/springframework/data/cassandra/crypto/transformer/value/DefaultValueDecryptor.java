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
package org.springframework.data.cassandra.crypto.transformer.value;

import org.springframework.data.cassandra.crypto.transformer.bytes.BytesDecryptor;

import java.security.Key;

class DefaultValueDecryptor implements ValueDecryptor {
    private final Key defaultKey;
    private final BytesConverter preConverter;
    private final BytesConverter postConverter;

    public DefaultValueDecryptor(BytesConverter preConverter, BytesConverter postConverter, Key defaultKey) {
        this.preConverter = preConverter;
        this.postConverter = postConverter;
        this.defaultKey = defaultKey;
    }

    BytesConverter getPreConverter() {
        return preConverter;
    }

    BytesConverter getPostConverter() {
        return postConverter;
    }

    @Override
    public Object decrypt(BytesDecryptor bytesDecryptor, Object value) {
        @SuppressWarnings("unchecked")
        byte[] bytes = preConverter.toBytes(value);

        // 'defaultKey' is likely to be ignored by the BytesDecryptor, as the
        // key name is obtained from the record itself
        byte[] transformed = bytesDecryptor.decrypt(bytes, 0, defaultKey);
        return postConverter.fromBytes(transformed);
    }

    @Override
    public Object decode(Object value) {
        if (value == null) {
            return null;
        }
        
        @SuppressWarnings("unchecked")
        byte[] bytes = preConverter.toBytes(value);

        return postConverter.fromBytes(bytes);
    }
}
