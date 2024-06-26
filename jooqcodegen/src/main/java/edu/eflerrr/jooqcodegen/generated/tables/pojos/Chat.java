/*
 * This file is generated by jOOQ.
 */

package edu.eflerrr.jooqcodegen.generated.tables.pojos;

import jakarta.validation.constraints.Size;
import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.time.OffsetDateTime;
import javax.annotation.processing.Generated;
import org.jetbrains.annotations.NotNull;

/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "https://www.jooq.org",
        "jOOQ version:3.19.6"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({"all", "unchecked", "rawtypes", "this-escape"})
public class Chat implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private OffsetDateTime createdAt;

    public Chat() {
    }

    public Chat(Chat value) {
        this.id = value.id;
        this.username = value.username;
        this.createdAt = value.createdAt;
    }

    @ConstructorProperties({"id", "username", "createdAt"})
    public Chat(
        @NotNull Long id,
        @NotNull String username,
        @NotNull OffsetDateTime createdAt
    ) {
        this.id = id;
        this.username = username;
        this.createdAt = createdAt;
    }

    /**
     * Getter for <code>Chat.id</code>.
     */
    @jakarta.validation.constraints.NotNull
    @NotNull
    public Long getId() {
        return this.id;
    }

    /**
     * Setter for <code>Chat.id</code>.
     */
    public void setId(@NotNull Long id) {
        this.id = id;
    }

    /**
     * Getter for <code>Chat.username</code>.
     */
    @jakarta.validation.constraints.NotNull
    @Size(max = 1000000000)
    @NotNull
    public String getUsername() {
        return this.username;
    }

    /**
     * Setter for <code>Chat.username</code>.
     */
    public void setUsername(@NotNull String username) {
        this.username = username;
    }

    /**
     * Getter for <code>Chat.created_at</code>.
     */
    @jakarta.validation.constraints.NotNull
    @NotNull
    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    /**
     * Setter for <code>Chat.created_at</code>.
     */
    public void setCreatedAt(@NotNull OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Chat other = (Chat) obj;
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        if (this.username == null) {
            if (other.username != null) {
                return false;
            }
        } else if (!this.username.equals(other.username)) {
            return false;
        }
        if (this.createdAt == null) {
            if (other.createdAt != null) {
                return false;
            }
        } else if (!this.createdAt.equals(other.createdAt)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.username == null) ? 0 : this.username.hashCode());
        result = prime * result + ((this.createdAt == null) ? 0 : this.createdAt.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Chat (");

        sb.append(id);
        sb.append(", ").append(username);
        sb.append(", ").append(createdAt);

        sb.append(")");
        return sb.toString();
    }
}
