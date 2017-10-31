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

import org.springframework.data.cassandra.crypto.cipher.CipherFactory;
import org.springframework.data.cassandra.crypto.key.KeySource;

import javax.crypto.Cipher;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

class CbcBytesTransformerFactory implements BytesTransformerFactory {

    private final CipherFactory cipherFactory;
    private final Key key;
    private final Header encryptionHeader;
    private final int blockSize;
    private final KeySource keySource;
    private final Queue<SecureRandom> randoms;

    CbcBytesTransformerFactory(CipherFactory cipherFactory, KeySource keySource, Header encryptionHeader) {
        this.randoms = new ConcurrentLinkedQueue<>();
        this.keySource = keySource;

        this.cipherFactory = cipherFactory;
        this.blockSize = cipherFactory.blockSize();
        this.encryptionHeader = encryptionHeader;

        final String keyName = keySource.getDefaultKeyAlias();
        this.key = keySource.getKey(keyName);
    }

    protected byte[] generateSeedIv() {

        final byte[] iv = new byte[blockSize];

        // the idea of a queue of SecureRandoms for concurrency is taken from
        // Tomcat's SessionIdGenerator. Also some code...

        SecureRandom random = randoms.poll();
        if (random == null) {
            random = createSecureRandom();
        }

        random.nextBytes(iv);
        randoms.add(random);

        return iv;
    }

    /**
     * Create a new random number generator instance we should use for
     * generating session identifiers.
     */
    private SecureRandom createSecureRandom() {

        // TODO: allow to customize provider?
        final SecureRandom result = new SecureRandom();

        // Force seeding to take place
        result.nextInt();
        return result;
    }

    @Override
    public BytesEncryptor encryptor() {
        final Cipher cipher = cipherFactory.cipher();
        BytesEncryptor delegate = new CbcEncryptor(cipher, key, generateSeedIv());

        if (encryptionHeader.isCompressed()) {
            delegate = new GzipEncryptor(delegate);
        }

        if (encryptionHeader.haveHMAC()) {
            delegate = new HmacEncryptor(delegate, encryptionHeader, key);
        }

        return new HeaderEncryptor(delegate, encryptionHeader);
    }

    @Override
    public BytesDecryptor decryptor() {
        final Cipher cipher = cipherFactory.cipher();
        final BytesDecryptor cbcDecryptor = new CbcDecryptor(cipher);
        final BytesDecryptor gzipDecryptor = new GzipDecryptor(cbcDecryptor);
        
        return new HeaderDecryptor(cbcDecryptor, gzipDecryptor, keySource);
    }

}
