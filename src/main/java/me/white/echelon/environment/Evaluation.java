package me.white.echelon.environment;

import me.white.echelon.Instruction;
import me.white.echelon.Value;

public abstract class Evaluation {
    public abstract Value<?> evaluate(Context context);

    public static class LiteralEvaluation extends Evaluation {
        private final Value<?> value;

        public LiteralEvaluation(Value<?> value) {
            this.value = value;
        }

        @Override
        public Value<?> evaluate(Context context) {
            return value.resolve(context).getValue();
        }
    }

    public static class InstructionEvaluation extends Evaluation {
        private final Instruction instruction;

        public InstructionEvaluation(Instruction instruction) {
            this.instruction = instruction;
        }

        @Override
        public Value<?> evaluate(Context context) {
            return instruction.execute(context);
        }
    }
}
