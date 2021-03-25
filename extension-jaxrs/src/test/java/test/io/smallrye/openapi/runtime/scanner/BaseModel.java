package test.io.smallrye.openapi.runtime.scanner;

import java.util.Date;
import java.util.UUID;

import javax.json.bind.annotation.JsonbDateFormat;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public abstract class BaseModel {

    protected UUID id;
    @JsonbDateFormat(value = "yyyy-MM-dd'T'HH:mm:ss[.SSS]X")
    @Schema(implementation = String.class, format = "date-time")
    protected Date lastUpdate;

    public BaseModel() {
    }

    public BaseModel(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID uuid) {
        this.id = uuid;
    }

    public void setId(String uuid) {
        this.id = UUID.fromString(uuid);
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [id=" + id + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
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
        BaseModel other = (BaseModel) obj;
        if (id == null && other.id != null) {
            return false;
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

}
