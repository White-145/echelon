package me.white.echelon.program;

import me.white.echelon.pipeline.Lexer;
import me.white.echelon.pipeline.Parser;
import me.white.echelon.program.value.Container;
import me.white.echelon.program.value.Value;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Program {
    private Map<String, Func> functions;

    public Program(String code) {
        Lexer lexer = new Lexer(code);
        functions = Parser.parse(lexer);
    }

    public void addBuiltinFunction(String name, int argumentCount, Function<List<Container>, Value<?>> function) {
        if (!functions.containsKey(name)) {
            functions.put(name, new BuiltinFunction(argumentCount, function));
        }
    }

    public Map<String, Func> getFunctions() {
        return functions;
    }

    public void execute() {
        if (functions.containsKey("")) {
            Func mainFunction = functions.get("");
            mainFunction.execute(List.of(), functions);
        }
    }
}
