package me.white.echelon;

import me.white.echelon.environment.Environment;
import me.white.echelon.environment.Storage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    private static final Map<String, Function.Builtin> builtinFunctions = new HashMap<>();

    static {
        builtinFunctions.put("output", new Function.Builtin(1, (context, parameters) -> {
            Value<?> value = parameters.get(0).getValue();
            System.out.println(value);
            return null;
        }));
        builtinFunctions.put("set", new Function.Builtin(2, (context, parameters) -> {
            Storage.ReferenceValue variable = parameters.get(0);
            Value<?> value = parameters.get(1).getValue();
            variable.setValue(value);
            return null;
        }));
        builtinFunctions.put("call", new Function.Builtin(1, (context, parameters) -> {
            Value<?> value = parameters.get(0).getValue();
            if (!(value instanceof Value.FunctionValue)) {
                return value;
            }
            return ((Value.FunctionValue)value).getValue().run(context);
        }));
        builtinFunctions.put("bundle", new Function.Builtin(1, (context, parameters) -> {
            Storage.ReferenceValue reference = parameters.get(0);
            if (reference.getName() == null) {
                return new Value.NumberValue(0);
            }
            String name = reference.getName();
            return switch (name) {
                case ".const" -> new Value.NumberValue(1);
                case ".hello" -> new Value.StringValue("Hello, World!");
                case ".func" -> new Value.FunctionValue(new Function.Builtin(0, (env, params) -> {
                    System.out.println("call from .func!");
                    return null;
                }));
                default -> new Value.NumberValue(0);
            };
        }));
        builtinFunctions.put("if", new Function.Builtin(3, (context, parameters) -> {
            Value<?> conditionValue = parameters.get(0).getValue();
            Value<?> trueValue = parameters.get(1).getValue();
            Value<?> falseValue = parameters.get(2).getValue();
            boolean condition = true;
            if (conditionValue instanceof Value.NumberValue) {
                double value = ((Value.NumberValue)conditionValue).getValue();
                if (value == 0.0 || Double.isNaN(value)) {
                    condition = false;
                }
            }
            if (trueValue instanceof Value.FunctionValue && falseValue instanceof Value.FunctionValue) {
                Function function = ((Value.FunctionValue)(condition ? trueValue : falseValue)).getValue();
                return function.runWithContext(context, List.of());
            }
            return condition ? trueValue : falseValue;
        }));
    }

    // Features for builtin functions:
    // - IO         output, input
    // - Set        =
    // - Arithmetic +, -, *, /
    // - String     substring, length, repeat, concatenate
    // - Flow       if, while, for
    // - Boolean    <, <=, ==, =!, >=, >, and, or, not
    // - Reference  name, ref, deref, get, get local, get global, get function, get builtin
    // - Call       call (enhanced stack function manipulation)
    // - Bundle     add member, remove member (since deref is possible)
    // - Global     define global

    // !
    //  =:a,1
    //  ?:a,!
    //   =:a,0
    //  ~,!
    //   =:a,1
    //  ~
    //  <:a
    // ~

    public static void main(String[] args) {
        String code = """
                !invert:x\\~if:x,0,1
                !extract_bundle:x\\~bundle:x
                !nested\\~!
                ~!
                ~!
                ~"end"
                
                !
                 set:x,69
                 output:x
                 output:invert:x
                 output:invert:invert:x
                 output:extract_bundle:.hello
                 output:nested::::
                ~
                """;
        Environment environment = Environment.parse(code);
        environment.setBuiltinFunctions(builtinFunctions);
        Value<?> value = environment.run(List.of());
        if (value != null) {
            System.out.println(value);
        }
    }
}
