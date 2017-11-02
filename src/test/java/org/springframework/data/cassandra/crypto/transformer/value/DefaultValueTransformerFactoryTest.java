package org.springframework.data.cassandra.crypto.transformer.value;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.crypto.key.KeySource;
import org.springframework.data.cassandra.crypto.transformer.bytes.DefaultBytesTransformerFactory;
import org.springframework.data.cassandra.mapping.*;

import java.nio.ByteBuffer;
import java.security.Key;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Sergey S. Sergeev
 */
@RunWith(EasyMockRunner.class)
public class DefaultValueTransformerFactoryTest {
    @Mock
    Key defaultKey;
    @Mock
    KeySource keySource;

    @TestSubject
    DefaultValueTransformerFactory valueTransformerFactory = new DefaultValueTransformerFactory();

    CassandraMappingContext mappingContext;
    DefaultBytesTransformerFactory bytesTransformerFactory;

    @Before
    public void setUp() throws Exception {
        mappingContext = new CassandraMappingContext();
    }

    @Test
    public void testEncodeTransformation() throws Exception {
        final CassandraPersistentEntity<TestEntity> cpe =
                (CassandraPersistentEntity<TestEntity>) mappingContext.getPersistentEntity(TestEntity.class);

        valueTransformerFactory.putObjectToBytesMap(byte[].class.getName(), ByteBufferToBytesConverter.INSTANCE);
        valueTransformerFactory.putDbToBytesMap(ByteBuffer.class.getName(), ByteBufferToBytesConverter.INSTANCE);

        final ValueEncryptor valueEncryptor =
                valueTransformerFactory.encryptor(cpe.getPersistentProperty("cryptoBytes"));

        Object object = valueEncryptor.encode(ByteBuffer.wrap(new byte[] {1,2,3}));
        assertTrue(object instanceof ByteBuffer);
        assertArrayEquals(new byte[] {1,2,3}, ByteBuffer.class.cast(object).array());
    }

    @Table
    final public class TestEntity {
        @Id
        private UUID id;
        private String title;
        @Column("body_text")
        private String body;
        @Crypto(columnState = "crypto")
        private Long cryptoValue;
        private Boolean crypto;
        @Crypto(columnState = "crypto", columnDbType = String.class)
        private String cryptoString;
        @Crypto(columnState = "crypto")
        private byte[] cryptoBytes;
    }
}