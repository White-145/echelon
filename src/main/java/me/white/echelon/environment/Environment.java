package me.white.echelon.environment;

import me.white.echelon.Function;
import me.white.echelon.Value;
import me.white.echelon.lexer.Lexer;
import me.white.echelon.parser.Parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Environment {
    private final Function mainFunction;
    private final Map<String, Function> functions;
    private final Map<String, Storage.ReferenceValue> globals = new HashMap<>();
    private Map<String, Function.Builtin> builtinFunctions = new HashMap<>();

    public Environment(Function mainFunction, Map<String, Function> functions) {
        this.mainFunction = mainFunction;
        this.functions = functions;
    }

    public static Environment parse(String code) {
        Lexer lexer = new Lexer(code);
        Parser parser = new Parser(lexer);
        return parser.parse();
    }

    public Map<String, Function> getFunctions() {
        return functions;
    }

    public Map<String, Storage.ReferenceValue> getGlobals() {
        return globals;
    }

    public Map<String, Function.Builtin> getBuiltinFunctions() {
        return builtinFunctions;
    }

    public void setBuiltinFunctions(Map<String, Function.Builtin> builtinFunctions) {
        this.builtinFunctions = builtinFunctions;
    }

    public void addBuiltinFunction(String name, Function.Builtin function) {
        builtinFunctions.put(name, function);
    }

    public Value<?> run(Function function, List<Value<?>> parameters) {
        Context context = new Context(this, new Storage());
        List<Storage.ReferenceValue> references = parameters.stream().map(value -> new Storage.ReferenceValue(null, value)).toList();
        return function.run(context, references);
    }

    public Value<?> run(List<Value<?>> parameters) {
        if (mainFunction == null) {
            throw new IllegalStateException("no main function");
        }
        return run(mainFunction, parameters);
    }

    public Value<?> run(String name, List<Value<?>> parameters) {
        if (!functions.containsKey(name)) {
            throw new IllegalStateException("no function");
        }
        return run(functions.get(name), parameters);
    }
}
