package org.springframework.data.cassandra.convert;

import org.springframework.core.convert.converter.Converter;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Date: 17.12.13 16:47
 *
 * @author lom
 */
final public class ByteBufToArrayOfBytesConverter implements Converter<ByteBuffer, byte[]> {

    public byte[] convert(ByteBuffer buffer) {
        final int length = buffer.remaining();

        if (buffer.hasArray())
        {
            final int boff = buffer.arrayOffset() + buffer.position();
            if (boff == 0 && length == buffer.array().length)
                return buffer.array();
            else
                return Arrays.copyOfRange(buffer.array(), boff, boff + length);
        }
        // else, DirectByteBuffer.get() is the fastest route
        final byte[] bytes = new byte[length];
        buffer.duplicate().get(bytes);

        return bytes;
    }

}
