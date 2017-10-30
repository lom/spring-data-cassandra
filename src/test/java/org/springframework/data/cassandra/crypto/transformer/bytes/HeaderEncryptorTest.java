/*****************************************************************
 *   Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 ****************************************************************/
package org.springframework.data.cassandra.crypto.transformer.bytes;

import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertArrayEquals;

public class HeaderEncryptorTest {

    @Test
    public void testTransform() throws UnsupportedEncodingException {

        Header encryptionHeader = Header.create("mykey", false, false);

        BytesEncryptor delegate = SwapBytesTransformer.encryptor();

        byte[] input = { 1, 2, 3, 4, 5, 6, 7, 8 };

        // intentionally non-standard block size..
        HeaderEncryptor encryptor = new HeaderEncryptor(delegate, encryptionHeader);

        byte[] output1 = encryptor.encrypt(input, 0, new byte[1]);
        assertArrayEquals(new byte[] { 'C', 'C', '1', 10, 0, 'm', 'y', 'k', 'e', 'y', 8, 7, 6, 5, 4, 3, 2, 1 }, output1);
        
        byte[] output2 = encryptor.encrypt(input, 1, new byte[1]);
        assertArrayEquals(new byte[] { 0, 'C', 'C', '1', 10, 0, 'm', 'y', 'k', 'e', 'y', 8, 7, 6, 5, 4, 3, 2, 1 }, output2);
    }

}
