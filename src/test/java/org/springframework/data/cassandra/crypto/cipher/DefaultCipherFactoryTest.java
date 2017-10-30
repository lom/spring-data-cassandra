package org.springframework.data.cassandra.crypto.cipher;

import org.junit.Test;
import org.springframework.data.cassandra.crypto.CassandraCryptoException;

import javax.crypto.Cipher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Sergey S. Sergeev
 **/
public class DefaultCipherFactoryTest {

    @Test
    public void testPropertiesSet() throws Exception {
        DefaultCipherFactory f = new DefaultCipherFactory();
        f.setAlgorithm("AES");
        f.setMode("CBC");
        f.setPadding("PKCS5Padding");
        f.afterPropertiesSet();

        assertEquals("AES/CBC/PKCS5Padding", f.getTransformation());
    }

    @Test(expected = CassandraCryptoException.class)
    public void testConstructor_Missing3Props() throws Exception {
        DefaultCipherFactory f = new DefaultCipherFactory();
        f.afterPropertiesSet();
    }

    @Test(expected = CassandraCryptoException.class)
    public void testConstructor_Missing2Props() throws Exception {
        DefaultCipherFactory f = new DefaultCipherFactory();
        f.setAlgorithm("AES");
        f.afterPropertiesSet();
    }

    @Test(expected = CassandraCryptoException.class)
    public void testConstructor_Missing1Props() throws Exception {
        DefaultCipherFactory f = new DefaultCipherFactory();
        f.setAlgorithm("AES");
        f.setMode("CBC");

        f.afterPropertiesSet();
    }

    @Test
    public void testGetCipher() throws Exception {
        DefaultCipherFactory f = new DefaultCipherFactory();
        f.setAlgorithm("AES");
        f.setMode("CBC");
        f.setPadding("PKCS5Padding");
        f.afterPropertiesSet();

        Cipher c = f.cipher();
        assertNotNull(c);
        assertEquals("AES/CBC/PKCS5Padding", c.getAlgorithm());
    }
}
