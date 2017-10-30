package org.springframework.data.cassandra.crypto.key;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.security.Key;

import static org.junit.Assert.*;

/**
 * @author Sergey S. Sergeev
 */
public class JceksKeySourceTest {
    public static final char[] TEST_KEY_PASS = "testkeypass".toCharArray();
    public static final String KS1_JCEKS = "test.jceks";

    @Test
    public void testGetKey() throws Exception {
        JceksKeySource ks = new JceksKeySource();
        ks.setDefaultKeyAlias("k2");
        ks.setKeyStoreLocation(new ClassPathResource(KS1_JCEKS));
        ks.setKeyPassword(TEST_KEY_PASS);
        ks.afterPropertiesSet();

        assertNull(ks.getKey("no-such-key"));

        Key k1 = ks.getKey("k1");
        assertNotNull(k1);
        assertEquals("DES", k1.getAlgorithm());

        Key k2 = ks.getKey("k2");
        assertNotNull(k2);
        assertEquals("DES", k2.getAlgorithm());

        Key k3 = ks.getKey("k3");
        assertNotNull(k3);
        assertEquals("AES", k3.getAlgorithm());

        assertEquals("k2", ks.getDefaultKeyAlias());
    }
}