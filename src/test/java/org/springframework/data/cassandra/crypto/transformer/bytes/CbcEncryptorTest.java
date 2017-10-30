package org.springframework.data.cassandra.crypto.transformer.bytes;

import org.junit.Test;
import org.springframework.data.cassandra.crypto.CassandraCryptoException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Sergey S. Sergeev
 */
public class CbcEncryptorTest {
    @Test(expected = CassandraCryptoException.class)
    public void testConstructor() throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException {

        byte[] iv = { 1, 2, 3, 4, 5, 6 };
        Key key = createMock(Key.class);
        replay(key);

        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        assertEquals(8, cipher.getBlockSize());

        // must throw as IV sie and block size are different
        new CbcEncryptor(cipher, key, iv);
    }

    @Test
    public void testEncrypt_AES() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

        byte[] ivBytes = DatatypeConverter.parseHexBinary("0591849d87c93414f4405d32f4d69220");
        byte[] keyBytes = DatatypeConverter.parseHexBinary("a4cb499fa31a6a228e16b7e4741d4fa3");
        Key key = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        assertEquals(16, cipher.getBlockSize());

        byte[] plain = { 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 };

        // copy ivBytes, as they are reset
        CbcEncryptor encryptor = new CbcEncryptor(cipher, key, ivBytes);

        byte[] encrypted = encryptor.encrypt(plain, 0, new byte[1]);

        assertEquals(16 * 3, encrypted.length);
        assertArrayEquals(ivBytes, Arrays.copyOfRange(encrypted, 0, 16));

        Cipher decCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        decCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(ivBytes));
        byte[] newPlain = decCipher.doFinal(encrypted, 16, encrypted.length - 16);
        assertArrayEquals(plain, newPlain);
    }
}