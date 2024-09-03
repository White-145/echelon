package me.white.echelon;

import me.white.echelon.environment.Context;
import me.white.echelon.environment.Storage;

import java.util.*;

public class Instruction {
    private final List<Action> actions;

    public Instruction(List<Action> actions) {
        Collections.reverse(actions);
        this.actions = actions;
    }

    public Value<?> execute(Context context) {
        List<Storage.ReferenceValue> values = new ArrayList<>();
        int nested = 0;
        Value<?> result = null;
        for (Action action : actions) {
            if (result != null) {
                values.add(new Storage.ReferenceValue(null, result));
                result = null;
            }
            Value<?> value = action.getValue();
            if (action.getType() == ActionType.INPUT) {
                values.add(value.resolve(context));
                continue;
            }
            if (value == null) {
                nested += 1;
                continue;
            }
            while (nested > 0) {
                Value<?> resolved = value.resolve(context).getValue();
                if (!(resolved instanceof Value.FunctionValue)) {
                    throw new IllegalStateException("calling non function");
                }
                Function function = ((Value.FunctionValue)resolved).getValue();
                if (function.getParameterCount() != 0) {
                    throw new IllegalStateException("mouh calling function with no parameters");
                }
                value = function.run(context);
                nested -= 1;
            }
            Value<?> resolved = value.resolve(context).getValue();
            if (!(resolved instanceof Value.FunctionValue)) {
                throw new IllegalStateException("calling non function");
            }
            Function function = ((Value.FunctionValue)resolved).getValue();
            int parameterCount = function.getParameterCount();
            List<Storage.ReferenceValue> subList = values.subList(0, parameterCount);
            List<Storage.ReferenceValue> parameters = new ArrayList<>(subList);
            subList.clear();
            Collections.reverse(parameters);
            result = function.run(context, parameters);
        }
        if (!values.isEmpty()) {
            throw new IllegalStateException("too much values inputed");
        }
        return result;
    }

    public static class Action {
        private final ActionType type;
        private final Value<?> value;

        public Action(ActionType type, Value<?> value) {
            this.type = type;
            this.value = value;
        }

        public Action(ActionType type) {
            this(type, null);
        }

        public ActionType getType() {
            return type;
        }

        public Value<?> getValue() {
            return value;
        }
    }

    public enum ActionType {
        INPUT,
        ACCESS
    }
}
