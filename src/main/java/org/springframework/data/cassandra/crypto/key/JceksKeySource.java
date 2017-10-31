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
package org.springframework.data.cassandra.crypto.key;

import com.google.common.collect.Maps;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.data.cassandra.crypto.CassandraCryptoException;

import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.ConcurrentMap;

public class JceksKeySource implements KeySource, InitializingBean {
    // this is the only standard keystore type that supports storing secret keys
    private static final String JCEKS_KEYSTORE_TYPE = "jceks";
    private static final Key NULL_KEY = new Key() {

        private static final long serialVersionUID = 4755682444381893880L;

        @Override
        public String getFormat() {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] getEncoded() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getAlgorithm() {
            throw new UnsupportedOperationException();
        }
    };

    private KeyStore keyStore;
    private char[] keyPassword;
    private String defaultKeyAlias;
    private Resource keyStoreLocation;

    // caching the keys may not be a good idea for security reasons, but
    // re-reading the key from KeyStore for every select row creates a huge
    // bottleneck... And considering we are caching keystore password, it
    // probably doesn't make things that much worse
    private final ConcurrentMap<String, Key> keyCache = Maps.newConcurrentMap();

    public JceksKeySource() {
        /** is ok **/
    }

    public char[] getKeyPassword() {
        return keyPassword;
    }

    public void setKeyPassword(char[] keyPassword) {
        this.keyPassword = keyPassword;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword.toCharArray();
    }

    public void setDefaultKeyAlias(String defaultKeyAlias) {
        this.defaultKeyAlias = defaultKeyAlias;
    }

    public Resource getKeyStoreLocation() {
        return keyStoreLocation;
    }

    public void setKeyStoreLocation(Resource keyStoreLocation) {
        this.keyStoreLocation = keyStoreLocation;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (keyStoreLocation == null) {
            throw new CassandraCryptoException("KeyStore URL is not set");
        }

        if (defaultKeyAlias == null) {
            throw new CassandraCryptoException("Default key alias is not set");
        }

        try {
            this.keyStore = createKeyStore(keyStoreLocation);
        } catch (Exception e) {
            throw new CassandraCryptoException("Error loading keystore at " + keyStoreLocation, e);
        }
    }

    private KeyStore createKeyStore(Resource keyStoreUrl) throws KeyStoreException, IOException,
            NoSuchAlgorithmException, CertificateException {

        final KeyStore keyStore = KeyStore.getInstance(JCEKS_KEYSTORE_TYPE);

        try (final InputStream in = keyStoreUrl.getInputStream()) {
            keyStore.load(in, null);
        }

        return keyStore;
    }

    @Override
    public Key getKey(String alias) {
        Key key = keyCache.get(alias);

        if (key == null) {
            final Key newKey = createKey(alias);
            final Key oldKey = keyCache.putIfAbsent(alias, newKey);
            key = oldKey != null ? oldKey : newKey;
        }

        return key.equals(NULL_KEY) ? null : key;
    }

    protected Key createKey(String alias) {
        try {
            final Key key = keyStore.getKey(alias, keyPassword);
            return key != null ? key : NULL_KEY;
        } catch (Exception e) {
            throw new CassandraCryptoException("Error accessing key for alias: " + alias, e);
        }
    }

    @Override
    public String getDefaultKeyAlias() {
        return defaultKeyAlias;
    }
}
