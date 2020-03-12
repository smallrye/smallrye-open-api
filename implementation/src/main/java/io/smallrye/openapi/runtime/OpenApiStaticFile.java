package io.smallrye.openapi.runtime;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import io.smallrye.openapi.runtime.io.OpenApiSerializer.Format;

/**
 * @author eric.wittmann@gmail.com
 */
public class OpenApiStaticFile implements Closeable {

    private Format format;
    private InputStream content;

    /**
     * Constructor.
     */
    public OpenApiStaticFile() {
    }

    public OpenApiStaticFile(InputStream content, Format format) {
        this.setContent(content);
        this.setFormat(format);
    }

    /**
     * @see java.io.Closeable#close()
     */
    @Override
    public void close() throws IOException {
        if (this.getContent() != null) {
            this.getContent().close();
        }
    }

    /**
     * @return the format
     */
    public Format getFormat() {
        return format;
    }

    /**
     * @param format the format to set
     */
    public void setFormat(Format format) {
        this.format = format;
    }

    /**
     * @return the content
     */
    public InputStream getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(InputStream content) {
        this.content = content;
    }

}
