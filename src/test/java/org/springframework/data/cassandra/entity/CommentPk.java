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

import java.io.Serializable;
import java.util.UUID;

/**
 * Date: 12.12.13 17:28
 *
 * @author Alexandr V Solomatin
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
