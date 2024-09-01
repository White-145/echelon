package me.white.echelon;

import me.white.echelon.environment.Context;
import me.white.echelon.environment.Environment;
import me.white.echelon.environment.Storage;

public abstract class Value<T> {
    private T value;

    public Value(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    public static class NumberValue extends Value<Double> {
        public NumberValue(double value) {
            super(value);
        }
    }

    public static class StringValue extends Value<String> {
        public StringValue(String value) {
            super(value);
        }
    }

    public static class FunctionValue extends Value<Function> {
        public FunctionValue(Function value) {
            super(value);
        }
    }

    public static class IdentifierValue extends Value<String> {
        public IdentifierValue(String value) {
            super(value);
        }

        public Storage.ReferenceValue resolve(Context context) {
            Environment environment = context.getEnvironment();
            Storage storage = context.getStorage();
            String name = getValue();
            if (storage.contains(name)) {
                return storage.get(name);
            }
            if (environment.getGlobals().containsKey(name)) {
                return environment.getGlobals().get(name);
            }
            if (environment.getFunctions().containsKey(name)) {
                return new Storage.ReferenceValue(name, new Value.FunctionValue(environment.getFunctions().get(name)));
            }
            if (environment.getBuiltinFunctions().containsKey( name)) {
                return new Storage.ReferenceValue(name, new Value.FunctionValue(environment.getBuiltinFunctions().get(name)));
            }
            Storage.ReferenceValue value = new Storage.ReferenceValue(name);
            storage.put(name, value);
            return value;
        }
    }
}
