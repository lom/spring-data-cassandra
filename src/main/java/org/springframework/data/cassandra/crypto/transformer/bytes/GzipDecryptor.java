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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.Key;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

class GzipDecryptor implements BytesDecryptor {

    private BytesDecryptor delegate;

    public GzipDecryptor(BytesDecryptor delegate) {
        this.delegate = delegate;
    }

    @Override
    public byte[] decrypt(byte[] input, int inputOffset, Key key) {

        byte[] decrypted = delegate.decrypt(input, inputOffset, key);
        try {
            return gunzip(decrypted);
        } catch (IOException e) {
            // really not expecting an error here...
            throw new CassandraCryptoException("Error uncompressing input", e);
        }
    }

    static byte[] gunzip(byte[] input) throws IOException {

        ByteArrayInputStream zipBytes = new ByteArrayInputStream(input);
        GZIPInputStream in = new GZIPInputStream(zipBytes);

        // duplicating ByteArrayOutputStream logic here... too much overhead and
        // inefficiency using ByteArrayOutputStream directly

        byte[] out = new byte[input.length * 2];

        int totalRead = 0;
        int read;
        int resizeBy = input.length;

        while ((read = in.read(out, totalRead, out.length - totalRead - 1)) > 0) {

            totalRead += read;
            if (totalRead + 1 == out.length) {
                out = Arrays.copyOf(out, out.length + resizeBy);
            }
        }

        if (totalRead < out.length) {
            out = Arrays.copyOf(out, totalRead);
        }

        return out;
    }
}
