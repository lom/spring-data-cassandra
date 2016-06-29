/*
 * Copyright 2013-2014 the original author or authors.
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
package org.springframework.data.cassandra.convert;

import org.springframework.core.convert.converter.Converter;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Date: 17.12.13 16:47
 *
 * @author Alexandr V Solomatin
 */
final public class ByteBufToArrayOfBytesConverter implements Converter<ByteBuffer, byte[]> {

    public byte[] convert(ByteBuffer buffer) {
        final int length = buffer.remaining();

        if (buffer.hasArray()) {
            final int boff = buffer.arrayOffset() + buffer.position();
//            if (boff == 0 && length == buffer.array().length)
//                return buffer.array();
//            else
//                return Arrays.copyOfRange(buffer.array(), boff, boff + length);
            return Arrays.copyOfRange(buffer.array(), boff, boff + length);
        }
        // else, DirectByteBuffer.get() is the fastest route
        final byte[] bytes = new byte[length];
        buffer.duplicate().get(bytes);

        return bytes;
    }

}
