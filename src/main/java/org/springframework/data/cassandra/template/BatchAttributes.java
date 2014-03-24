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
package org.springframework.data.cassandra.template;

import com.datastax.driver.core.ConsistencyLevel;

/**
 * Date: 18.03.14 19:24
 *
 * @author Alexandr V Solomatin
 */
final public class BatchAttributes {
    private Long timestamp;
    private ConsistencyLevel consistencyLevel;
    private boolean unlogged;

    public Long getTimestamp() {
        return timestamp;
    }

    public ConsistencyLevel getConsistencyLevel() {
        return consistencyLevel;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setConsistencyLevel(ConsistencyLevel consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
    }

    public boolean isUnlogged() {
        return unlogged;
    }

    public void setUnlogged(boolean unlogged) {
        this.unlogged = unlogged;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BatchAttributes)) return false;

        final BatchAttributes that = (BatchAttributes) o;

        if (unlogged != that.unlogged) return false;
        if (consistencyLevel != that.consistencyLevel) return false;
        if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = timestamp != null ? timestamp.hashCode() : 0;
        result = 31 * result + (consistencyLevel != null ? consistencyLevel.hashCode() : 0);
        result = 31 * result + (unlogged ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BatchAttributes{");
        sb.append("timestamp=").append(timestamp);
        sb.append(", consistencyLevel=").append(consistencyLevel);
        sb.append(", unlogged=").append(unlogged);
        sb.append('}');
        return sb.toString();
    }
}
