package me.white.echelon.program;

import me.white.echelon.program.value.Container;
import me.white.echelon.program.value.Value;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class BuiltinFunction extends Func {
    private Function<List<Container>, Value<?>> function;

    public BuiltinFunction(int argumentCount, Function<List<Container>, Value<?>> function) {
        super(null, null, null);
        this.argumentCount = argumentCount;
        this.function = function;
    }

    @Override
    public Value<?> execute(List<Container> arguments, Map<String, Func> functions) {
        return function.apply(arguments);
    }

    @Override
    public String toString() {
        return "{Builtin:" + argumentCount + "}";
    }
}
