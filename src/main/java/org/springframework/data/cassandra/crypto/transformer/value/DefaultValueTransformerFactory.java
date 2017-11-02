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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.cassandra.crypto.key.KeySource;
import org.springframework.data.cassandra.mapping.CassandraPersistentProperty;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A {@link ValueTransformerFactory} that creates encryptors/decryptors that are
 * taking advantage of the JCE (Java Cryptography Extension) ciphers.
 */
public class DefaultValueTransformerFactory implements ValueTransformerFactory, InitializingBean {
    private final Map<String, BytesConverter<?>> objectToBytes = new HashMap<>();
    private final Map<String, BytesConverter<?>> dbToBytes = new HashMap<>();
    private final Map<CassandraPersistentProperty, ValueEncryptor> encryptors = new HashMap<>();
    private final Map<CassandraPersistentProperty, ValueDecryptor> decryptors = new HashMap<>();
    private Key defaultKey;
    private KeySource keySource;

    public KeySource getKeySource() {
        return keySource;
    }

    public void setKeySource(KeySource keySource) {
        this.keySource = keySource;
    }

    @Override
    public ValueDecryptor decryptor(CassandraPersistentProperty a) {
        ValueDecryptor e = decryptors.get(a);

        if (e == null) {
            final ValueDecryptor newTransformer = createDecryptor(a);
            final ValueDecryptor oldTransformer = decryptors.putIfAbsent(a, newTransformer);

            e = oldTransformer != null ? oldTransformer : newTransformer;
        }

        return e;
    }

    @Override
    public ValueEncryptor encryptor(CassandraPersistentProperty a) {
        ValueEncryptor e = encryptors.get(a);

        if (e == null) {
            final ValueEncryptor newTransformer = createEncryptor(a);
            final ValueEncryptor oldTransformer = encryptors.putIfAbsent(a, newTransformer);

            e = oldTransformer != null ? oldTransformer : newTransformer;
        }

        return e;
    }

    ValueEncryptor createEncryptor(CassandraPersistentProperty a) {
        final String type = a.getType().getName();

        final BytesConverter<?> toBytes = objectToBytes.get(type);
        if (toBytes == null) {
            throw new IllegalArgumentException("The type " + type + " for property "
                    + a.getOwner().getName() + "." + a.getName()
                    + " has no object-to-bytes conversion");
        }

        final BytesConverter<?> fromBytes = dbToBytes.get(a.getColumnCryptoDbType().getName());
        if (fromBytes == null) {
            throw new IllegalArgumentException("The type " + a.getColumnCryptoDbType().getName()
                    + " for property " + a.getOwner().getName() + "." + a.getName()
                    + " has no bytes-to-db conversion");
        }

        return new DefaultValueEncryptor(toBytes, fromBytes);
    }

    ValueDecryptor createDecryptor(CassandraPersistentProperty a) {
        final BytesConverter<?> toBytes = dbToBytes.get(a.getColumnCryptoDbType().getName());
        if (toBytes == null) {
            throw new IllegalArgumentException("The type " + a.getColumnCryptoDbType().getName()
                    + " for property " + a.getOwner().getName() + "." + a.getName()
                    + " has no db-to-bytes conversion");
        }

        final String type = a.getType().getName();
        final BytesConverter<?> fromBytes = objectToBytes.get(type);
        if (fromBytes == null) {
            throw new IllegalArgumentException("The type " + type + " for property "
                    + a.getOwner().getName() + "." + a.getName()
                    + " has no bytes-to-object conversion");
        }

        return new DefaultValueDecryptor(toBytes, fromBytes, defaultKey);
    }

    public void putObjectToBytesMap(String string, BytesConverter converter) {
        objectToBytes.put(string, converter);
    }

    public void putDbToBytesMap(String string, BytesConverter converter) {
        dbToBytes.put(string, converter);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        objectToBytes.put(byte[].class.getName(), ByteBufferToBytesConverter.INSTANCE);
        objectToBytes.put(String.class.getName(), Utf8StringConverter.INSTANCE);
        objectToBytes.put(Double.class.getName(), DoubleConverter.INSTANCE);
        objectToBytes.put(Float.class.getName(), FloatConverter.INSTANCE);
        objectToBytes.put(Long.class.getName(), LongConverter.INSTANCE);
        objectToBytes.put(Integer.class.getName(), IntegerConverter.INSTANCE);
        objectToBytes.put(Short.class.getName(), ShortConverter.INSTANCE);
        objectToBytes.put(Byte.class.getName(), ByteConverter.INSTANCE);
        objectToBytes.put(Boolean.class.getName(), BooleanConverter.INSTANCE);
        objectToBytes.put(Date.class.getName(), UtilDateConverter.INSTANCE);
        objectToBytes.put(BigInteger.class.getName(), BigIntegerConverter.INSTANCE);
        objectToBytes.put(BigDecimal.class.getName(), BigDecimalConverter.INSTANCE);
        objectToBytes.put(UUID.class.getName(), UUIDConverter.INSTANCE);

        dbToBytes.put("java.nio.HeapByteBuffer", ByteBufferToBytesConverter.INSTANCE);
        dbToBytes.put(ByteBuffer.class.getName(), ByteBufferToBytesConverter.INSTANCE);
        dbToBytes.put(String.class.getName(), Base64StringConverter.INSTANCE);

        this.defaultKey = keySource.getKey(keySource.getDefaultKeyAlias());
    }
}
