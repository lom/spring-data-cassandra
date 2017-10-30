package org.springframework.data.cassandra.crypto.transformer.bytes;

import java.security.Key;

/**
 * A fake "cipher" used for unit tests that does simple bytes swapping.
 */
public class SwapBytesTransformer implements BytesEncryptor, BytesDecryptor {

    private static final SwapBytesTransformer instance = new SwapBytesTransformer();

    public static BytesEncryptor encryptor() {
        return instance;
    }

    public static BytesDecryptor decryptor() {
        return instance;
    }

    private SwapBytesTransformer() {
    }

    @Override
    public byte[] decrypt(byte[] input, int inputOffset, Key key) {

        byte[] output = new byte[input.length - inputOffset];
        System.arraycopy(input, inputOffset, output, 0, input.length - inputOffset);

        swap(output, 0, output.length - 1);
        return output;
    }

    @Override
    public byte[] encrypt(byte[] input, int outputOffset, byte[] flags) {

        byte[] output = new byte[input.length + outputOffset];

        System.arraycopy(input, 0, output, outputOffset, input.length);

        swap(output, outputOffset, outputOffset + input.length - 1);

        return output;
    }

    private void swap(byte[] buffer, int start, int end) {

        if (start >= end) {
            return;
        }

        byte b = buffer[end];
        buffer[end] = buffer[start];
        buffer[start] = b;

        swap(buffer, ++start, --end);
    }
}
