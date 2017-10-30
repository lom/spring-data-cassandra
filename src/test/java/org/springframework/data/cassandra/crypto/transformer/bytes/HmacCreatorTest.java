package org.springframework.data.cassandra.crypto.transformer.bytes;

import org.junit.Test;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertArrayEquals;

/**
 * @since 4.0
 */
public class HmacCreatorTest {

    /**
     * Sample output from https://en.wikipedia.org/wiki/Hash-based_message_authentication_code
     */
    @Test
    public void createHmac() {
        final byte[] headerData = "The quick".getBytes();
        Header header = createMock(Header.class);

        expect(header.size()).andReturn(headerData.length);
        expect(header.getFlags()).andReturn((byte)0);

        header.store(anyObject(byte[].class), anyInt(), anyByte());

        expectLastCall().andAnswer(() -> {
            byte[] input = (byte[]) getCurrentArguments()[0];
            System.arraycopy(headerData, 0, input, 0, headerData.length);
            return null;
        });

        replay(header);

        Key key = new SecretKeySpec("key".getBytes(), "AES");
        HmacCreator creator = new HmacCreator(header, key) {};

        byte[] hmac = creator.createHmac(" brown fox jumps over the lazy dog".getBytes());
        byte[] hmacExpected = DatatypeConverter.parseHexBinary("f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8");

        assertArrayEquals(hmacExpected, hmac);
        verify(header);
    }

}