package io.github.joaovitorleal.securecapita.enumeration;

public enum VerificationType {
    ACCOUNT("ACCOUNT"),
    PASSWORD("PASSWORD");

    private final String type;

    VerificationType(String type) {
        this.type = type;
    }

    /**
     * @return the type in lower case}
     * */
    public String getType() {
        return type.toLowerCase();
    }
}
