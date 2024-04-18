package me.white.echelon.program.value;

public class Container {
    private String name;
    private Value<?> storedValue;

    public Container(String name, Value<?> value) {
        this.name = name;
        this.storedValue = value;
    }

    public String getName() {
        return name;
    }

    public Value<?> getStoredValue() {
        return storedValue;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStoredValue(Value<?> storedValue) {
        this.storedValue = storedValue;
    }
}
