package co.edu.uniquindio.backendpawsoft.enums;

/**
 * Tipo de referencia para un archivo adjunto.
 */
public enum AttachmentReferenceType {
    MEDICAL_RECORD("Registro Médico"),
    HOSPITALIZATION("Hospitalización");

    private final String displayName;

    AttachmentReferenceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
