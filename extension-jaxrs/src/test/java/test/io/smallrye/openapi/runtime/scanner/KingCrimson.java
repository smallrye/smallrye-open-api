package test.io.smallrye.openapi.runtime.scanner;

import java.util.Date;

import javax.json.bind.annotation.JsonbDateFormat;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class KingCrimson extends BaseModel {

    public enum Status {
        unknown,
        success,
        failure
    }

    @JsonbDateFormat(value = "yyyy-MM-dd'T'HH:mm:ss[.SSS]X")
    @Schema(implementation = String.class, format = "date-time")
    Date timestamp;
    Magma environment;
    Status status;

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Magma getEnvironment() {
        return environment;
    }

    public void setEnvironment(Magma environment) {
        this.environment = environment;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
