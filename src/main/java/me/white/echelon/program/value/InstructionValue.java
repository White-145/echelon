package me.white.echelon.program.value;

import me.white.echelon.program.Instruction;

public class InstructionValue implements Value<Instruction> {
    private Instruction value;

    public InstructionValue(Instruction value) {
        this.value = value;
    }

    @Override
    public Instruction getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "{Instruction '" + value + "'}";
    }
}
