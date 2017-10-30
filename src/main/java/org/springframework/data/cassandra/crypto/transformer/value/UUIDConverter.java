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

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * @author Sergey S. Sergeev
 */
public class UUIDConverter implements BytesConverter<UUID> {
    public static final BytesConverter<UUID> INSTANCE = new UUIDConverter();

    @Override
    public byte[] toBytes(UUID value) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(value.getMostSignificantBits());
        bb.putLong(value.getLeastSignificantBits());

        return bb.array();
    }

    @Override
    public UUID fromBytes(byte[] bytes) {
        final ByteBuffer bb = ByteBuffer.wrap(bytes);
        return new UUID(bb.getLong(), bb.getLong());
    }
}
