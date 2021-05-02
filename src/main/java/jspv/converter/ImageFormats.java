package jspv.converter;

public enum ImageFormats {

    //WBMP_FORMAT("WBMP"),
    JPG_FORMAT("JPG"),
    GIF_FORMAT("GIF"),
    //BMP_FORMAT("BMP"),
    PNG_FORMAT("PNG");

    private String caption;

    private ImageFormats(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return this.caption;
    }

}
