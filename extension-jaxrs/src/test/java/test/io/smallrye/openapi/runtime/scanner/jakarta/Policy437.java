package test.io.smallrye.openapi.runtime.scanner.jakarta;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class Policy437 {

    // The ID will be created by code.
    public UUID id;
    @JsonbTransient
    public String customerid;
    @NotNull
    @NotEmpty
    @Schema(description = "Name of the rule. Must be unique per customer account.")
    @Size(max = 150)
    public String name;
    @Schema(description = "A short description of the policy.")
    public String description;
    public boolean isEnabled;
    @Schema(description = "Condition string.", example = "arch = \"x86_64\"")
    @NotEmpty
    @NotNull
    public String conditions;
    @Schema(description = "String describing actions separated by ';' when the policy is evaluated to true."
            + "Allowed values are 'email' and 'webhook'")
    public String actions;
    @Schema(type = SchemaType.STRING, description = "Last update time in a form like '2020-01-24 12:19:56.718', output only", readOnly = true, format = "yyyy-MM-dd hh:mm:ss.ddd", implementation = String.class)
    private Timestamp mtime = new Timestamp(System.currentTimeMillis());
    @Schema(type = SchemaType.STRING, description = "Create time in a form like '2020-01-24 12:19:56.718', output only", readOnly = true, format = "yyyy-MM-dd hh:mm:ss.ddd", implementation = String.class)
    private Timestamp ctime = new Timestamp(System.currentTimeMillis());
    private long lastTriggered;

    @JsonbTransient
    public void setMtime(String mtime) {
        this.mtime = Timestamp.valueOf(mtime);
    }

    public void setMtimeToNow() {
        this.mtime = new Timestamp(System.currentTimeMillis());
    }

    public String getMtime() {
        return mtime.toString();
    }

    public void setLastTriggered(long tTime) {
        lastTriggered = tTime;
    }

    @JsonbTransient
    public long getLastTriggered() {
        return lastTriggered;
    }

    @JsonbTransient
    public void setCtime(String ctime) {
        this.ctime = Timestamp.valueOf(ctime);
    }

    public String getCtime() {
        return ctime.toString();
    }

    public UUID store(String customer, Policy437 policy) {
        if (!customer.equals(policy.customerid)) {
            throw new IllegalArgumentException("Store: customer id do not match");
        }
        return id;
    }

    public void delete(Policy437 policy) {
    }

    public void populateFrom(Policy437 policy) {
        this.id = policy.id;
        this.name = policy.name;
        this.description = policy.description;
        this.actions = policy.actions;
        this.conditions = policy.conditions;
        this.isEnabled = policy.isEnabled;
        this.customerid = policy.customerid;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Policy{");
        sb.append("id=").append(id);
        sb.append(", customerid='").append(customerid).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", mtime=").append(mtime);
        sb.append('}');
        return sb.toString();
    }

    enum SortableColumn {
        NAME("name"),
        DESCRIPTION("description"),
        IS_ENABLED("is_enabled"),
        MTIME("mtime");

        private final String name;

        SortableColumn(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static SortableColumn fromName(String columnName) {
            for (SortableColumn column : SortableColumn.values()) {
                if (column.getName().equals(columnName)) {
                    return column;
                }
            }
            throw new IllegalArgumentException("Unknown Policy.SortableColumn requested: [" + columnName + "]");
        }
    }

    enum FilterableColumn {
        NAME("name"),
        DESCRIPTION("description"),
        IS_ENABLED("is_enabled");

        private final String name;

        FilterableColumn(final String name) {
            this.name = name;
        }

        public static FilterableColumn fromName(String columnName) {
            Optional<FilterableColumn> result = Arrays.stream(FilterableColumn.values())
                    .filter(val -> val.name.equals(columnName)).findAny();
            if (result.isPresent()) {
                return result.get();
            }
            throw new IllegalArgumentException("Unknown Policy.FilterableColumn requested: [" + columnName + "]");
        }
    }

}
