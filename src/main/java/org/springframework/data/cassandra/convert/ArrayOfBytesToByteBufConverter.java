package org.springframework.data.cassandra.convert;

import org.springframework.core.convert.converter.Converter;

import java.nio.ByteBuffer;

/**
 * Date: 17.12.13 16:57
 *
 * @author lom
 */
final public class ArrayOfBytesToByteBufConverter implements Converter<byte[], ByteBuffer> {

    public ByteBuffer convert(byte[] source) {
        final ByteBuffer msgBuf = ByteBuffer.allocate(source.length).put(source);
        msgBuf.rewind();

        return msgBuf;
    }
}
