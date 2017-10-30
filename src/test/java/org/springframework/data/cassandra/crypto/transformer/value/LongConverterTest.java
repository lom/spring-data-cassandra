package org.springframework.data.cassandra.crypto.transformer.value;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Sergey S. Sergeev
 */
public class LongConverterTest {
    @Test
    public void testFromBytes_InIntRange() {
        // 256*256* 6 + 256*7 + 16
        assertEquals(new Long(395024), new LongConverter().fromBytes(new byte[]{0, 6, 7, 16}));
    }

    @Test
    public void testFromBytes_InLongRange() {
        // 6*256*256*256*256 + 7*256*256*256 + 16*256*256 + 17*256 + 99
        assertEquals(new Long(25888297315l), new LongConverter().fromBytes(new byte[]{0, 0, 0, 6, 7, 16, 17, 99}));
    }

    @Test
    public void testToBytes() {
        assertArrayEquals(new byte[]{127, -1, -1, -1, -1, -1, -1, -2}, new LongConverter().toBytes((long) (Long.MAX_VALUE - 1l)));
        assertArrayEquals(new byte[]{127, -1, -1, -2}, new LongConverter().toBytes((long) (Integer.MAX_VALUE - 1)));
        assertArrayEquals(new byte[]{127, -2}, new LongConverter().toBytes(Short.MAX_VALUE - 1l));
        assertArrayEquals(new byte[]{-7}, new LongConverter().toBytes(-7l));
    }
}