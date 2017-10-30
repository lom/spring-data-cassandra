package org.springframework.data.cassandra.crypto.transformer.bytes;

import org.junit.Before;
import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Sergey S. Sergeev
 */
public class CbcDecryptorTest {
    private Cipher cipher;
    private Key key;

    @Before
    public void before() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        byte[] keyBytes = DatatypeConverter.parseHexBinary("a4cb499fa31a6a228e16b7e4741d4fa3");
        this.key = new SecretKeySpec(keyBytes, "AES");

        this.cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        assertEquals(16, cipher.getBlockSize());
    }

    @Test
    public void testIv() {

        CbcDecryptor decryptor = new CbcDecryptor(cipher);

        byte[] input = { 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 };
        byte[] ivBytes = { 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 };

        IvParameterSpec iv = decryptor.iv(input, 5);
        assertArrayEquals(ivBytes, iv.getIV());
    }

    @Test
    public void testDecrypt() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

        CbcDecryptor decryptor = new CbcDecryptor(cipher);

        byte[] plain = { 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 };
        byte[] ivBytes = DatatypeConverter.parseHexBinary("0591849d87c93414f4405d32f4d69220");

        Cipher encCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        encCipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(ivBytes));

        byte[] encrypted = encCipher.doFinal(plain);

        byte[] encryptedWithIv = new byte[encrypted.length + ivBytes.length];
        System.arraycopy(ivBytes, 0, encryptedWithIv, 0, ivBytes.length);
        System.arraycopy(encrypted, 0, encryptedWithIv, ivBytes.length, encrypted.length);

        byte[] decrypted = decryptor.decrypt(encryptedWithIv, 0, key);
        assertArrayEquals(plain, decrypted);
    }
}