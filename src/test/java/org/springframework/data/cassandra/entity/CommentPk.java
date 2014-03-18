package org.springframework.data.cassandra.entity;

import java.io.Serializable;
import java.util.UUID;

/**
 * Date: 12.12.13 17:28
 *
 * @author lom
 */
final public class CommentPk implements Serializable {
    private UUID postId;
    private UUID commentId;

    public CommentPk() {
    }

    public CommentPk(UUID postId, UUID commentId) {
        this.postId = postId;
        this.commentId = commentId;
    }

    public UUID getPostId() {
        return postId;
    }

    public void setPostId(UUID postId) {
        this.postId = postId;
    }

    public UUID getCommentId() {
        return commentId;
    }

    public void setCommentId(UUID commentId) {
        this.commentId = commentId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CommentPk{");
        sb.append("postId=").append(postId);
        sb.append(", commentId=").append(commentId);
        sb.append('}');
        return sb.toString();
    }

}
