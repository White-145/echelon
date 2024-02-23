package me.white.echelon.program;

import me.white.echelon.pipeline.Lexer;
import me.white.echelon.pipeline.Parser;
import me.white.echelon.program.value.ContainerValue;
import me.white.echelon.program.value.Value;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class Program {
    private HashMap<String, Func> functions;

    public Program(String code) {
        Lexer lexer = new Lexer(code);
        functions = Parser.parse(lexer);
    }

    public void addBuiltinFunction(String name, int argumentCount, Function<List<ContainerValue>, Value<?>> function) {
        if (!functions.containsKey(name)) {
            functions.put(name, new BuiltinFunction(argumentCount, function));
        }
    }

    public void execute() {
        if (functions.containsKey("")) {
            Func mainFunction = functions.get("");
            mainFunction.execute(List.of(), functions);
        }
    }
}
