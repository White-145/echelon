package me.white.echelon;

import me.white.echelon.environment.Context;
import me.white.echelon.environment.Storage;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class Instruction {
    private final List<Action> actions;

    public Instruction(List<Action> actions) {
        this.actions = actions;
    }

    public Value<?> execute(Context context) {
        Stack<StackFrame> frames = new Stack<>();
        frames.add(new StackFrame());
        for (Action action : actions) {
            if (action.type == ActionType.ACCESS) {
                Storage.ReferenceValue functionReference = new Storage.ReferenceValue(null, action.value);
                if (action.value == null) {
                    functionReference = frames.peek().pop();
                } else if (action.value instanceof Value.IdentifierValue) {
                    functionReference = ((Value.IdentifierValue)action.value).resolve(context);
                }
                if (!(functionReference.getValue() instanceof Value.FunctionValue)) {
                    throw new IllegalStateException("calling not a function");
                }
                Function function = ((Value.FunctionValue)functionReference.getValue()).getValue();
                if (function.getParameterCount() == 0) {
                    throw new IllegalStateException("overfeeding function");
                }
                frames.add(new StackFrame(function));
            } else if (action.type == ActionType.PROCEDURE) {
                Storage.ReferenceValue functionReference = new Storage.ReferenceValue(null, action.value);
                if (action.value == null) {
                    functionReference = frames.peek().pop();
                } else if (action.value instanceof Value.IdentifierValue) {
                    functionReference = ((Value.IdentifierValue)action.value).resolve(context);
                }
                if (!(functionReference.getValue() instanceof Value.FunctionValue)) {
                    throw new IllegalStateException("calling not a function");
                }
                Function function = ((Value.FunctionValue)functionReference.getValue()).getValue();
                if (function.getParameterCount() != 0) {
                    throw new IllegalStateException("underfeeding function");
                }
                Value<?> value = function.run(context);
                if (value != null) {
                    frames.peek().add(new Storage.ReferenceValue(null, value));
                }
            } else if (action.value != null) {
                if (action.value instanceof Value.IdentifierValue) {
                    frames.peek().add(((Value.IdentifierValue)action.value).resolve(context));
                } else {
                    frames.peek().add(new Storage.ReferenceValue(null, action.value));
                }
            }

            while (frames.size() > 1 && frames.peek().size() == frames.peek().getFunction().getParameterCount()) {
                StackFrame frame = frames.pop();
                Value<?> value = frame.getFunction().run(context, frame.getValues());
                if (value != null) {
                    frames.peek().add(new Storage.ReferenceValue(null, value));
                }
            }
        }

        if (frames.size() > 1) {
            throw new IllegalStateException("too litle values");
        }

        if (frames.peek().size() != 1) {
            return null;
        }

        return frames.peek().pop().getValue();
    }

    private static class StackFrame {
        private final Function function;
        private final LinkedList<Storage.ReferenceValue> values = new LinkedList<>();

        public StackFrame(Function function) {
            this.function = function;
        }

        public StackFrame() {
            this(null);
        }

        public Function getFunction() {
            return function;
        }

        public List<Storage.ReferenceValue> getValues() {
            return values;
        }

        public void add(Storage.ReferenceValue value) {
            values.add(value);
        }

        public Storage.ReferenceValue pop() {
            return values.pop();
        }

        public int size() {
            return values.size();
        }
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
        ACCESS,
        PROCEDURE
    }
}
