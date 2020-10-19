package io.smallrye.openapi.ui;

/**
 * Available themes
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public enum ThemeHref {
    original,
    feeling_blue,
    flattop,
    material,
    monokai,
    muted,
    newspaper,
    outline;

    @Override
    public String toString() {
        switch (this) {
            case feeling_blue:
                return String.format(FORMAT, "feeling-blue");
            case flattop:
                return String.format(FORMAT, "flattop");
            case material:
                return String.format(FORMAT, "material");
            case monokai:
                return String.format(FORMAT, "monokai");
            case muted:
                return String.format(FORMAT, "muted");
            case newspaper:
                return String.format(FORMAT, "newspaper");
            case outline:
                return String.format(FORMAT, "outline");
            default:
                return null;
        }
    }

    private static final String FORMAT = "theme-%s.css";
}