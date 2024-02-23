package me.white.echelon.program.value;

public class ContainerValue implements Value<String> {
    private String value;
    private Value<?> heldValue = null;

    public ContainerValue(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "{Container '" + value + "' holding " + heldValue + "}";
    }

    public Value<?> getHeldValue() {
        return heldValue;
    }

    public void setHeldValue(Value<?> value) {
        heldValue = value;
    }
}
