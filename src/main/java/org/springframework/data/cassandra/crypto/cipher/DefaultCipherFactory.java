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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.cassandra.crypto.CassandraCryptoException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;

public class DefaultCipherFactory implements CipherFactory, InitializingBean {
    private String transformation;
    private String algorithm;
    private String mode;
    private String padding;
    private int blockSize;
    private Provider provider;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (algorithm == null) {
            throw new CassandraCryptoException("Cipher algorithm is not set");
        }

        if (mode == null) {
            throw new CassandraCryptoException("Cipher mode is not set");
        }

        if (padding == null) {
            throw new CassandraCryptoException("Cipher padding is not set");
        }

        this.transformation = algorithm + "/" + mode + "/" + padding;
    }

    @Override
    public String getTransformation() {
        return transformation;
    }

    public void setTransformation(String transformation) {
        this.transformation = transformation;
    }

    @Override
    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    @Override
    public String getPadding() {
        return padding;
    }

    public void setPadding(String padding) {
        this.padding = padding;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    @Override
    public Provider getProvider() {
        return provider;
    }

    @Override
    public Cipher cipher() {
        try {
            if (provider != null)
                return Cipher.getInstance(transformation, provider);

            return Cipher.getInstance(transformation);
        } catch (NoSuchAlgorithmException e) {
            throw new CassandraCryptoException("Error instantiating a cipher - no such algorithm: " + transformation, e);
        } catch (NoSuchPaddingException e) {
            throw new CassandraCryptoException("Error instantiating a cipher - no such padding: " + transformation, e);
        }
    }

    @Override
    public int blockSize() {
        // lazy init to prevent Cipher initialization in constructor.
        // synchronization is not required - if we do it multiple times ,
        // shouldn't be a big deal...
        if (blockSize == 0) {
            this.blockSize = cipher().getBlockSize();
        }

        return blockSize;
    }
}
