package io.smallrye.openapi.runtime.io;

public enum Format {
    JSON("application/json"),
    YAML("application/yaml");

    private final String mimeType;

    Format(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }
}
