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

/**
 * Converts between float and byte[]
 */
public class FloatConverter implements BytesConverter<Float> {

    public static final BytesConverter<Float> INSTANCE = new FloatConverter();
    private static final int BYTES = 4;

    static float getFloat(byte[] bytes) {

        if (bytes.length > BYTES) {
            throw new IllegalArgumentException("byte[] is too large for a single float columnCryptoState: " + bytes.length);
        }

        return Float.intBitsToFloat(IntegerConverter.getInt(bytes));
    }

    static byte[] getBytes(float f) {
        return IntegerConverter.getBytes(Float.floatToRawIntBits(f));
    }

    @Override
    public Float fromBytes(byte[] bytes) {
        return getFloat(bytes);
    }

    @Override
    public byte[] toBytes(Float value) {
        return getBytes(value);
    }
}
