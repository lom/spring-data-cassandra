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

import org.joda.time.DateTime;

/**
 * Date: 17.12.13 19:34
 *
 * @author Alexandr V Solomatin
 */
final public class CommentEmbedded {
    private DateTime fieldTimestamp;
    private Double fieldDouble;

    public DateTime getFieldTimestamp() {
        return fieldTimestamp;
    }

    public void setFieldTimestamp(DateTime fieldTimestamp) {
        this.fieldTimestamp = fieldTimestamp;
    }

    public Double getFieldDouble() {
        return fieldDouble;
    }

    public void setFieldDouble(Double fieldDouble) {
        this.fieldDouble = fieldDouble;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CommentEmbedded{");
        sb.append("fieldTimestamp=").append(fieldTimestamp);
        sb.append(", fieldDouble=").append(fieldDouble);
        sb.append('}');
        return sb.toString();
    }
}
