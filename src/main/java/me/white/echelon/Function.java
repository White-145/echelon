package me.white.echelon;

import me.white.echelon.environment.Context;
import me.white.echelon.environment.Evaluation;
import me.white.echelon.environment.Storage;

import java.util.List;

public abstract class Function {
    public abstract int getParameterCount();

    public abstract Value<?> run(Context context, List<Storage.ReferenceValue> parameters);

    public abstract Value<?> runWithContext(Context context, List<Storage.ReferenceValue> parameters);

    public Value<?> run(Context context) {
        return run(context, List.of());
    }

    public static class Defined extends Function {
        private final String name;
        private final List<String> parameters;
        private final List<Instruction> instructions;
        private final Evaluation evaluation;

        public Defined(String name, List<String> parameters, List<Instruction> instructions, Evaluation evaluation) {
            this.name = name;
            this.parameters = parameters;
            this.instructions = instructions;
            this.evaluation = evaluation;
        }

        public Defined(List<String> parameters, List<Instruction> instructions, Evaluation evaluation) {
            this(null, parameters, instructions, evaluation);
        }

        public String getName() {
            return name;
        }

        @Override
        public int getParameterCount() {
            return parameters.size();
        }

        @Override
        public Value<?> run(Context parentContext, List<Storage.ReferenceValue> parameters) {
            Storage storage = new Storage();
            Context context = new Context(parentContext.getEnvironment(), storage, parentContext);
            return runWithContext(context, parameters);
        }

        @Override
        public Value<?> runWithContext(Context context, List<Storage.ReferenceValue> parameters) {
            Storage storage = context.getStorage();
            for (int i = 0; i < this.parameters.size(); ++i) {
                storage.put(this.parameters.get(i), parameters.get(i));
            }
            for (Instruction instruction : instructions) {
                instruction.execute(context);
            }
            if (evaluation == null) {
                return null;
            }
            return evaluation.evaluate(context);
        }
    }

    public static class Builtin extends Function {
        private final int parameterCount;
        private final BuiltinFunction function;

        public Builtin(int parametersCount, BuiltinFunction function) {
            this.parameterCount = parametersCount;
            this.function = function;
        }

        @Override
        public int getParameterCount() {
            return parameterCount;
        }

        @Override
        public Value<?> run(Context context, List<Storage.ReferenceValue> parameters) {
            return function.run(context, parameters);
        }

        @Override
        public Value<?> runWithContext(Context context, List<Storage.ReferenceValue> parameters) {
            return run(context, parameters);
        }
    }

    @FunctionalInterface
    public interface BuiltinFunction {
        Value<?> run(Context context, List<Storage.ReferenceValue> parameters);
    }
}
