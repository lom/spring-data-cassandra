package org.springframework.data.cassandra.entity;

import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.Table;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import java.net.InetAddress;
import java.util.*;

/**
 * Date: 12.12.13 17:27
 *
 * @author lom
 */
@Table("comments")
final public class Comment {
    @Id
    private CommentPk id;
    @Column("body_text")
    private String text;
    @Transient
    private String title;
    private byte[] fieldBlob;
    private Map<String, UUID> fieldMap;
    private Set<String> fieldSet;
    @Column("fielt_list") // lol
    private List<Integer> fieldList;
    private InetAddress fieldInet;
    private CommentEmbedded commentEmbedded;

    public CommentPk getId() {
        return id;
    }

    public void setId(CommentPk id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public byte[] getFieldBlob() {
        return fieldBlob;
    }

    public void setFieldBlob(byte[] fieldBlob) {
        this.fieldBlob = fieldBlob;
    }

    public Map<String, UUID> getFieldMap() {
        return fieldMap;
    }

    public void setFieldMap(Map<String, UUID> fieldMap) {
        this.fieldMap = fieldMap;
    }

    public Set<String> getFieldSet() {
        return fieldSet;
    }

    public void setFieldSet(Set<String> fieldSet) {
        this.fieldSet = fieldSet;
    }

    public List<Integer> getFieldList() {
        return fieldList;
    }

    public void setFieldList(List<Integer> fieldList) {
        this.fieldList = fieldList;
    }

    public InetAddress getFieldInet() {
        return fieldInet;
    }

    public void setFieldInet(InetAddress fieldInet) {
        this.fieldInet = fieldInet;
    }

    public CommentEmbedded getCommentEmbedded() {
        return commentEmbedded;
    }

    public void setCommentEmbedded(CommentEmbedded commentEmbedded) {
        this.commentEmbedded = commentEmbedded;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Comment{");
        sb.append("id=").append(id);
        sb.append(", text='").append(text).append('\'');
        sb.append(", title='").append(title).append('\'');
        sb.append(", fieldBlob=").append(Arrays.toString(fieldBlob));
        sb.append(", fieldMap=").append(fieldMap);
        sb.append(", fieldSet=").append(fieldSet);
        sb.append(", fieldList=").append(fieldList);
        sb.append(", fieldInet=").append(fieldInet);
        sb.append(", commentEmbedded=").append(commentEmbedded);
        sb.append('}');
        return sb.toString();
    }
}
