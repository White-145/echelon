package me.white.echelon;

import me.white.echelon.program.Program;
import me.white.echelon.program.value.ContainerValue;
import me.white.echelon.program.value.NumberValue;
import me.white.echelon.program.value.Value;

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
                    ContainerValue value = arguments.get(0);
                    System.out.println(value.getHeldValue());
                    return new NumberValue(0);
                });
                program.addBuiltinFunction("=", 2, (arguments) -> {
                    ContainerValue container = arguments.get(0);
                    Value<?> value = arguments.get(1).getHeldValue();
                    container.setHeldValue(value);
                    return new NumberValue(0);
                });
                program.addBuiltinFunction("+", 2, (arguments) -> {
                    Value<?> value1 = arguments.get(0).getHeldValue();
                    Value<?> value2 = arguments.get(1).getHeldValue();
                    if (!(value1 instanceof NumberValue) || !(value2 instanceof NumberValue)) {
                        return new NumberValue(0);
                    }
                    return new NumberValue(((NumberValue)value1).getValue() + ((NumberValue)value2).getValue());
                });
                program.execute();
            } catch (IllegalStateException e) {
                System.out.println(e.getMessage());
            }
        }
        // TODO argument functions consuming line_end
        // TODO returning literal values
    }
}
