package me.white.echelon.program.value;

public class NumberValue implements Value<Integer> {
    private int value;

    public NumberValue(int value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
