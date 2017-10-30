package org.springframework.data.cassandra.crypto.transformer.value;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * @author Sergey S. Sergeev
 */
public class UUIDConverterTest {
    @Test
    public void testConversion() throws Exception {
        UUID uuid = UUID.randomUUID();
        byte[] uuidBytes = UUIDConverter.INSTANCE.toBytes(uuid);
        UUID uuidFromBytes =  UUIDConverter.INSTANCE.fromBytes(uuidBytes);

        assertEquals(uuid, uuidFromBytes);
    }
}