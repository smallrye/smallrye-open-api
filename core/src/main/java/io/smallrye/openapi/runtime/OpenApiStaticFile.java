package io.smallrye.openapi.runtime;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import io.smallrye.openapi.runtime.io.Format;

/**
 * @author eric.wittmann@gmail.com
 */
public class OpenApiStaticFile implements Closeable {

    private URL locator;
    private Format format;
    private InputStream content;

    /**
     * Constructor.
     */
    public OpenApiStaticFile() {
    }

    public OpenApiStaticFile(URL locator, InputStream content, Format format) {
        this.locator = locator;
        this.content = content;
        this.format = format;
    }

    public OpenApiStaticFile(InputStream content, Format format) {
        this(null, content, format);
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

    public URL getLocator() {
        return locator;
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
