/*
 * This file is generated by jOOQ.
 */

package edu.eflerrr.scrapper.domain.jooq.tables;

import edu.eflerrr.scrapper.domain.jooq.DefaultSchema;
import edu.eflerrr.scrapper.domain.jooq.Keys;
import edu.eflerrr.scrapper.domain.jooq.tables.records.BranchRecord;
import java.time.OffsetDateTime;
import java.util.Collection;
import javax.annotation.processing.Generated;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Identity;
import org.jooq.Name;
import org.jooq.PlainSQL;
import org.jooq.QueryPart;
import org.jooq.SQL;
import org.jooq.Schema;
import org.jooq.Select;
import org.jooq.Stringly;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

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
public class Branch extends TableImpl<BranchRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>Branch</code>
     */
    public static final Branch BRANCH = new Branch();

    /**
     * The class holding records for this type
     */
    @Override
    @NotNull
    public Class<BranchRecord> getRecordType() {
        return BranchRecord.class;
    }

    /**
     * The column <code>Branch.id</code>.
     */
    public final TableField<BranchRecord, Long> ID =
        createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>Branch.repository_owner</code>.
     */
    public final TableField<BranchRecord, String> REPOSITORY_OWNER =
        createField(DSL.name("repository_owner"), SQLDataType.VARCHAR(1000000000).nullable(false), this, "");

    /**
     * The column <code>Branch.repository_name</code>.
     */
    public final TableField<BranchRecord, String> REPOSITORY_NAME =
        createField(DSL.name("repository_name"), SQLDataType.VARCHAR(1000000000).nullable(false), this, "");

    /**
     * The column <code>Branch.branch_name</code>.
     */
    public final TableField<BranchRecord, String> BRANCH_NAME =
        createField(DSL.name("branch_name"), SQLDataType.VARCHAR(1000000000).nullable(false), this, "");

    /**
     * The column <code>Branch.last_commit_time</code>.
     */
    public final TableField<BranchRecord, OffsetDateTime> LAST_COMMIT_TIME =
        createField(DSL.name("last_commit_time"), SQLDataType.TIMESTAMPWITHTIMEZONE(6).nullable(false), this, "");

    private Branch(Name alias, Table<BranchRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private Branch(Name alias, Table<BranchRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>Branch</code> table reference
     */
    public Branch(String alias) {
        this(DSL.name(alias), BRANCH);
    }

    /**
     * Create an aliased <code>Branch</code> table reference
     */
    public Branch(Name alias) {
        this(alias, BRANCH);
    }

    /**
     * Create a <code>Branch</code> table reference
     */
    public Branch() {
        this(DSL.name("Branch"), null);
    }

    @Override
    @Nullable
    public Schema getSchema() {
        return aliased() ? null : DefaultSchema.DEFAULT_SCHEMA;
    }

    @Override
    @NotNull
    public Identity<BranchRecord, Long> getIdentity() {
        return (Identity<BranchRecord, Long>) super.getIdentity();
    }

    @Override
    @NotNull
    public UniqueKey<BranchRecord> getPrimaryKey() {
        return Keys.CONSTRAINT_7;
    }

    @Override
    @NotNull
    public Branch as(String alias) {
        return new Branch(DSL.name(alias), this);
    }

    @Override
    @NotNull
    public Branch as(Name alias) {
        return new Branch(alias, this);
    }

    @Override
    @NotNull
    public Branch as(Table<?> alias) {
        return new Branch(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    @NotNull
    public Branch rename(String name) {
        return new Branch(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    @NotNull
    public Branch rename(Name name) {
        return new Branch(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    @NotNull
    public Branch rename(Table<?> name) {
        return new Branch(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @NotNull
    public Branch where(Condition condition) {
        return new Branch(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @NotNull
    public Branch where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @NotNull
    public Branch where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @NotNull
    public Branch where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @NotNull
    @PlainSQL
    public Branch where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @NotNull
    @PlainSQL
    public Branch where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @NotNull
    @PlainSQL
    public Branch where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @NotNull
    @PlainSQL
    public Branch where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @NotNull
    public Branch whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @NotNull
    public Branch whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
