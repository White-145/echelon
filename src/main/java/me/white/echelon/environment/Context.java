package me.white.echelon.environment;

public class Context {
    private final Environment environment;
    private final Storage storage;
    private final Context parent;

    public Context(Environment environment, Storage storage, Context parent) {
        this.environment = environment;
        this.storage = storage;
        this.parent = parent;
    }

    public Context(Environment environment, Storage storage) {
        this(environment, storage, null);
    }

    public Environment getEnvironment() {
        return environment;
    }

    public Storage getStorage() {
        return storage;
    }

    public Context getParent() {
        return parent;
    }
}
