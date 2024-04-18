package me.white.echelon.program.value;

public class IdentifierValue implements Value<String> {
    private String value;

    public IdentifierValue(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "{Reference `"+ value + "`}";
    }
}
