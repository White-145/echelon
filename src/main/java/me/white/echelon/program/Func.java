package me.white.echelon.program;

import me.white.echelon.program.value.InstructionValue;
import me.white.echelon.program.value.IdentifierValue;
import me.white.echelon.program.value.Container;
import me.white.echelon.program.value.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Func {
    private List<String> arguments;
    private List<Instruction> instructions;
    private Value<?> returnValue;
    private int instructionsCount;
    protected int argumentCount;

    public Func(List<String> arguments, List<Instruction> instructions, Value<?> returnValue) {
        this.arguments = arguments;
        this.instructions = instructions;
        this.returnValue = returnValue;
        argumentCount = arguments == null ? 0 : arguments.size();
        instructionsCount = instructions == null ? 0 : instructions.size();
    }

    public Value<?> execute(List<Container> arguments, Map<String, Func> functions) {
        Map<String, Value<?>> storage = new HashMap<>();
        for (int i = 0; i < argumentCount; ++i) {
            storage.put(this.arguments.get(i), arguments.get(i).getStoredValue());
        }
        for (Instruction instruction : instructions) {
            instruction.execute(storage, functions);
        }
        for (Container argument : arguments) {
            String name = argument.getName();
            if (name != null) {
                argument.setStoredValue(storage.get(name));
            }
        }
        if (returnValue instanceof InstructionValue instructionValue) {
            return instructionValue.getValue().execute(storage, functions);
        }
        if (returnValue instanceof IdentifierValue identifierValue) {
            return storage.get(identifierValue.getValue());
        }
        return returnValue;
    }

    public int getArgumentCount() {
        return argumentCount;
    }

    @Override
    public String toString() {
        return "{Function: " + argumentCount + "}";
    }
}
