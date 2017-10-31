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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.cassandra.crypto.CassandraCryptoException;
import org.springframework.data.cassandra.crypto.cipher.CipherFactory;
import org.springframework.data.cassandra.crypto.key.KeySource;

/**
 * A {@link BytesTransformerFactory} that creates transformers depending on the
 * encryption mode specified via properties.
 */
public class DefaultBytesTransformerFactory implements InitializingBean, BytesTransformerFactory {
    private BytesTransformerFactory delegate;
    private boolean compression;
    private boolean useHmac;
    private CipherFactory cipherFactory;
    private KeySource keySource;

    public CipherFactory getCipherFactory() {
        return cipherFactory;
    }

    public void setCipherFactory(CipherFactory cipherFactory) {
        this.cipherFactory = cipherFactory;
    }

    public KeySource getKeySource() {
        return keySource;
    }

    public void setKeySource(KeySource keySource) {
        this.keySource = keySource;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final String mode = cipherFactory.getMode();
        if (mode == null) {
            throw new CassandraCryptoException("Cipher mode is not set");
        }

        final Header encryptionHeader = createEncryptionHeader(keySource);

        if ("CBC".equals(mode)) {
            this.delegate = new CbcBytesTransformerFactory(cipherFactory, keySource, encryptionHeader);
        } else {
            // TODO: ECB and other modes...
            throw new CassandraCryptoException("Unsupported mode: " + mode
                    + ". The following modes are currently supported: CBC");
        }
    }

    public boolean isCompression() {
        return compression;
    }

    public void setCompression(boolean compression) {
        this.compression = compression;
    }

    public boolean isUseHmac() {
        return useHmac;
    }

    public void setUseHmac(boolean useHmac) {
        this.useHmac = useHmac;
    }

    @Override
    public BytesEncryptor encryptor() {
        return delegate.encryptor();
    }

    @Override
    public BytesDecryptor decryptor() {
        return delegate.decryptor();
    }

    private Header createEncryptionHeader(KeySource keySource) {
        return Header.create(keySource.getDefaultKeyAlias(), compression, useHmac);
    }
}
