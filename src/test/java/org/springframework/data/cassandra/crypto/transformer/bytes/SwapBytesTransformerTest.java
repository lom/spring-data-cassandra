package org.springframework.data.cassandra.crypto.transformer.bytes;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class SwapBytesTransformerTest {

    @Test
    public void testEncrypt_Odd() {

        BytesEncryptor instance = SwapBytesTransformer.encryptor();

        byte[] input = { 1, 3, 5 };
        byte[] output = instance.encrypt(input, 3, new byte[1]);
        assertArrayEquals(new byte[] { 0, 0, 0, 5, 3, 1 }, output);
    }

    @Test
    public void testEncrypt_Even() {

        BytesEncryptor instance = SwapBytesTransformer.encryptor();

        byte[] input = { 1, 3, 5, 8 };
        byte[] output = instance.encrypt(input, 3, new byte[1]);

        assertArrayEquals(new byte[] { 0, 0, 0, 8, 5, 3, 1 }, output);
    }
}
