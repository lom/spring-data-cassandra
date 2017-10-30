/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.cassandra.entity;

import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.Crypto;
import org.springframework.data.cassandra.mapping.Table;
import org.springframework.data.annotation.Id;

import java.util.UUID;

/**
 * Date: 12.12.13 17:26
 *
 * @author Alexandr V Solomatin
 */
@Table
final public class Post {
    @Id
    private UUID id;
    private String title;
    @Column("body_text")
    private String body;
    private PostType type;

    @Crypto(columnState = "crypto")
    private Long cryptoValue;
    private Boolean crypto;
    @Crypto(columnState = "crypto", columnDbType = String.class)
    private String cryptoString;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public PostType getType() {
        return type;
    }

    public void setType(PostType type) {
        this.type = type;
    }

    public Long getCryptoValue() {
        return cryptoValue;
    }

    public void setCryptoValue(Long cryptoValue) {
        this.cryptoValue = cryptoValue;
    }

    public Boolean getCrypto() {
        return crypto;
    }

    public void setCrypto(Boolean crypto) {
        this.crypto = crypto;
    }

    public String getCryptoString() {
        return cryptoString;
    }

    public void setCryptoString(String cryptoString) {
        this.cryptoString = cryptoString;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Post{");
        sb.append("id=").append(id);
        sb.append(", title='").append(title).append('\'');
        sb.append(", body='").append(body).append('\'');
        sb.append(", type=").append(type);
        sb.append(", cryptoValue=").append(cryptoValue);
        sb.append(", crypto=").append(crypto);
        sb.append(", cryptoString='").append(cryptoString).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public enum PostType {
        TYPE1, TYPE2;
    }
}
