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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

class GzipEncryptor implements BytesEncryptor {
    static final int GZIP_THRESHOLD = 150;
    private final BytesEncryptor delegate;

    public GzipEncryptor(BytesEncryptor delegate) {
        this.delegate = delegate;
    }

    @Override
    @SuppressWarnings("PMD")
    public byte[] encrypt(byte[] input, int outputOffset, byte[] flags) {
        final boolean compressed = input.length >= GZIP_THRESHOLD;

        if (compressed) {
            try {
                input = gzip(input);
            } catch (IOException e) {
                // really not expecting an error here...
                throw new CassandraCryptoException("Error compressing input", e);
            }
        }

        flags[0] = Header.setCompressed(flags[0], compressed);
        return delegate.encrypt(input, outputOffset, flags);
    }

    static byte[] gzip(byte[] input) throws IOException {
        final ByteArrayOutputStream zipBytes = new ByteArrayOutputStream(input.length);
        try (final GZIPOutputStream out = new GZIPOutputStream(zipBytes)) {
            out.write(input, 0, input.length);
        }

        return zipBytes.toByteArray();
    }

}
