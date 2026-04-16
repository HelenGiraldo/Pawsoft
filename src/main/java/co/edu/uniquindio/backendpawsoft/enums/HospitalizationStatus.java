package co.edu.uniquindio.backendpawsoft.enums;

/**
 * Estado de una hospitalización.
 */
public enum HospitalizationStatus {
    ACTIVE("Activa"),
    DISCHARGED("Dada de alta"),
    DECEASED("Fallecida");

    private final String displayName;

    HospitalizationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}