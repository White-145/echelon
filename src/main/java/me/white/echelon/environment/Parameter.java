package me.white.echelon.environment;

import me.white.echelon.Value;

public class Parameter {
    private Value<?> value;

    public Parameter(Value<?> value) {
        this.value = value;
    }

    public Value<?> get() {
        return value;
    }

    public void modify(Value<?> value) { }

    public static class NamedParameter extends Parameter {
        private final String name;

        public NamedParameter(String name, Value<?> value) {
            super(value);
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
