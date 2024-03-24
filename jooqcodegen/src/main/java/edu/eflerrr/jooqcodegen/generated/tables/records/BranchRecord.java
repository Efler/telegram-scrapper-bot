/*
 * This file is generated by jOOQ.
 */

package edu.eflerrr.jooqcodegen.generated.tables.records;

import edu.eflerrr.jooqcodegen.generated.tables.Branch;
import jakarta.validation.constraints.Size;
import java.beans.ConstructorProperties;
import java.time.OffsetDateTime;
import javax.annotation.processing.Generated;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;

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
public class BranchRecord extends UpdatableRecordImpl<BranchRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>Branch.id</code>.
     */
    public void setId(@Nullable Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>Branch.id</code>.
     */
    @Nullable
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>Branch.link_id</code>.
     */
    public void setLinkId(@NotNull Long value) {
        set(1, value);
    }

    /**
     * Getter for <code>Branch.link_id</code>.
     */
    @jakarta.validation.constraints.NotNull
    @NotNull
    public Long getLinkId() {
        return (Long) get(1);
    }

    /**
     * Setter for <code>Branch.repository_owner</code>.
     */
    public void setRepositoryOwner(@NotNull String value) {
        set(2, value);
    }

    /**
     * Getter for <code>Branch.repository_owner</code>.
     */
    @jakarta.validation.constraints.NotNull
    @Size(max = 1000000000)
    @NotNull
    public String getRepositoryOwner() {
        return (String) get(2);
    }

    /**
     * Setter for <code>Branch.repository_name</code>.
     */
    public void setRepositoryName(@NotNull String value) {
        set(3, value);
    }

    /**
     * Getter for <code>Branch.repository_name</code>.
     */
    @jakarta.validation.constraints.NotNull
    @Size(max = 1000000000)
    @NotNull
    public String getRepositoryName() {
        return (String) get(3);
    }

    /**
     * Setter for <code>Branch.branch_name</code>.
     */
    public void setBranchName(@NotNull String value) {
        set(4, value);
    }

    /**
     * Getter for <code>Branch.branch_name</code>.
     */
    @jakarta.validation.constraints.NotNull
    @Size(max = 1000000000)
    @NotNull
    public String getBranchName() {
        return (String) get(4);
    }

    /**
     * Setter for <code>Branch.last_commit_time</code>.
     */
    public void setLastCommitTime(@NotNull OffsetDateTime value) {
        set(5, value);
    }

    /**
     * Getter for <code>Branch.last_commit_time</code>.
     */
    @jakarta.validation.constraints.NotNull
    @NotNull
    public OffsetDateTime getLastCommitTime() {
        return (OffsetDateTime) get(5);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    @NotNull
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached BranchRecord
     */
    public BranchRecord() {
        super(Branch.BRANCH);
    }

    /**
     * Create a detached, initialised BranchRecord
     */
    @ConstructorProperties({"id", "linkId", "repositoryOwner", "repositoryName", "branchName", "lastCommitTime"})
    public BranchRecord(
        @Nullable Long id,
        @NotNull Long linkId,
        @NotNull String repositoryOwner,
        @NotNull String repositoryName,
        @NotNull String branchName,
        @NotNull OffsetDateTime lastCommitTime
    ) {
        super(Branch.BRANCH);

        setId(id);
        setLinkId(linkId);
        setRepositoryOwner(repositoryOwner);
        setRepositoryName(repositoryName);
        setBranchName(branchName);
        setLastCommitTime(lastCommitTime);
        resetChangedOnNotNull();
    }

    /**
     * Create a detached, initialised BranchRecord
     */
    public BranchRecord(edu.eflerrr.jooqcodegen.generated.tables.pojos.Branch value) {
        super(Branch.BRANCH);

        if (value != null) {
            setId(value.getId());
            setLinkId(value.getLinkId());
            setRepositoryOwner(value.getRepositoryOwner());
            setRepositoryName(value.getRepositoryName());
            setBranchName(value.getBranchName());
            setLastCommitTime(value.getLastCommitTime());
            resetChangedOnNotNull();
        }
    }
}
