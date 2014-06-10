/*
 * Copyright 2014 the original author or authors.
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

import org.joda.time.DateTime;
import org.springframework.core.convert.converter.GenericConverter.ConvertiblePair;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashSet;

/**
 * Date: 06.06.14 15:29
 *
 * @author Alexandr V Solomatin
 */
final public class CassandraPersistentTypeResolver {
    final private static HashSet<ConvertiblePair> pairs;

    static {
        pairs = new HashSet<>();
        pairs.add(new ConvertiblePair(DateTime.class, Date.class));
        pairs.add(new ConvertiblePair(byte[].class, ByteBuffer.class));
        pairs.add(new ConvertiblePair(Enum.class, String.class));
        pairs.add(new ConvertiblePair(URI.class, String.class));
    }

    public Class<?> getPersistentType(Class<?> clazz) {
        for (final ConvertiblePair pair: pairs) {
            if (pair.getSourceType().isAssignableFrom(clazz))
                return pair.getTargetType();
        }

        return clazz;
    }

}
