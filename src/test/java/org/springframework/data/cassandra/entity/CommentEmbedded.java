package org.springframework.data.cassandra.entity;

import org.joda.time.DateTime;

/**
 * Date: 17.12.13 19:34
 *
 * @author lom
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
