package me.white.echelon.program;

import me.white.echelon.program.value.ContainerValue;
import me.white.echelon.program.value.Value;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class BuiltinFunction extends Func {
    private Function<List<ContainerValue>, Value<?>> function;

    public BuiltinFunction(int argumentCount, Function<List<ContainerValue>, Value<?>> function) {
        super(null, null, null);
        this.argumentCount = argumentCount;
        this.function = function;
    }

    @Override
    public Value<?> execute(List<ContainerValue> arguments, HashMap<String, Func> functions) {
        return function.apply(arguments);
    }

    @Override
    public String toString() {
        return "{Builtin:" + argumentCount + "}";
    }
}
