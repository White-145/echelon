package me.white.echelon;

import me.white.echelon.program.Func;
import me.white.echelon.program.Program;
import me.white.echelon.program.value.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            System.out.print("> ");
            String code = SCANNER.nextLine();
            if (code.equals("")) {
                break;
            }
            try {
                Program program = new Program(code);
                program.addBuiltinFunction("<", 1, (arguments) -> {
                    Value<?> value = arguments.get(0).getStoredValue();
                    System.out.println(value.getValue());
                    return new NumberValue(0);
                });
                program.addBuiltinFunction("*", 0, (arguments) -> new StringValue("Hello, World!"));
                program.addBuiltinFunction("=", 2, (arguments) -> {
                    Container container = arguments.get(0);
                    Value<?> value = arguments.get(1).getStoredValue();
                    container.setStoredValue(value);
                    return value;
                });
                program.addBuiltinFunction("+", 2, (arguments) -> {
                    Value<?> value1 = arguments.get(0).getStoredValue();
                    Value<?> value2 = arguments.get(1).getStoredValue();
                    if (!(value1 instanceof NumberValue) || !(value2 instanceof NumberValue)) {
                        return new NumberValue(0);
                    }
                    return new NumberValue(((NumberValue)value1).getValue() + ((NumberValue)value2).getValue());
                });
                program.addBuiltinFunction("&", 1, (arguments) -> {
                    Value<?> value = arguments.get(0).getStoredValue();
                    if (value instanceof FunctionValue functionValue) {
                        return functionValue.getValue().execute(List.of(), program.getFunctions());
                    } else {
                        System.out.println("cringe");
                    }
//                    System.out.println(value);
                    return new NumberValue(0);
                });
                program.addBuiltinFunction("?", 2, (arguments) -> {
                    Value<?> value1 = arguments.get(0).getStoredValue();
                    Value<?> value2 = arguments.get(1).getStoredValue();
                    if (!(value1 instanceof FunctionValue) || !(value2 instanceof FunctionValue)) {
                        return new NumberValue(0);
                    }
                    Func func1 = ((FunctionValue) value1).getValue();
                    Func func2 = ((FunctionValue) value2).getValue();
                    if (func1.getArgumentCount() != 0 || func2.getArgumentCount() != 0) {
                        return new NumberValue(0);
                    }
                    System.out.println("Executing function 1");
                    func1.execute(new ArrayList<>(), program.getFunctions());
                    System.out.println("Executing function 2");
                    func2.execute(new ArrayList<>(), program.getFunctions());
                    return new NumberValue(0);
                });
                program.execute();
            } catch (IllegalStateException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
