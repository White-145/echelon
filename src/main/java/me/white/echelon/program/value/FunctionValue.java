package me.white.echelon.program.value;

import me.white.echelon.program.Func;

public class FunctionValue implements Value<Func> {
    private Func value;

    public FunctionValue(Func value) {
        this.value = value;
    }

    @Override
    public Func getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "{Function + '" + value + "'}";
    }
}
