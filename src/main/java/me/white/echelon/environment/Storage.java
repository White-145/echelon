package me.white.echelon.environment;

import me.white.echelon.Value;

import java.util.HashMap;
import java.util.Map;

public class Storage {
    private final Map<String, ReferenceValue> names = new HashMap<>();

    public Storage() { }

    public boolean contains(String name) {
        return names.containsKey(name);
    }

    public ReferenceValue get(String name) {
        return names.get(name);
    }

    public void put(String name, ReferenceValue value) {
        names.put(name, value);
    }

    public static class ReferenceValue {
        private final String name;
        private Value<?> value;

        public ReferenceValue(String name, Value<?> value) {
            this.name = name;
            this.value = value;
        }

        public ReferenceValue(String name) {
            this(name, null);
        }

        public String getName() {
            return name;
        }

        public Value<?> getValue() {
            return value;
        }

        public void setValue(Value<?> value) {
            this.value = value;
        }
    }
}
