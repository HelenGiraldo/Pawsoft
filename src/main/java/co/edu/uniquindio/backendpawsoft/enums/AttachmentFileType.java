package co.edu.uniquindio.backendpawsoft.enums;

/**
 * Tipo de archivo adjunto.
 */
public enum AttachmentFileType {
    IMAGE("Imagen"),
    PDF("PDF");

    private final String displayName;

    AttachmentFileType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
