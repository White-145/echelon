package me.white.echelon.program;

import me.white.echelon.program.value.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Instruction {
    public enum Action {
        INPUT(true),
        ACCESS(true),
        PROCEDURE(false),
        STACK_ACCESS(false);

        final boolean hasValue;

        Action(boolean hasValue) {
            this.hasValue = hasValue;
        }
    }
    public static final Action INPUT = Action.INPUT;
    public static final Action ACCESS = Action.ACCESS;
    public static final Action PROCEDURE = Action.PROCEDURE;
    public static final Action STACK_ACCESS = Action.STACK_ACCESS;

    private List<Action> actions;
    private List<Value<?>> values;

    public Instruction(List<Value<?>> values, List<Action> actions) {
        this.values = values;
        this.actions = actions;
        int neededValues = 0;
        for (Action action : actions) {
            if (action.hasValue) {
                neededValues += 1;
            }
        }
        assert values.size() == neededValues;
    }

    public Value<?> execute(Map<String, Value<?>> storage, Map<String, Func> functions) {
        int readValues = 0;
        Stack<Func> accessStack = new Stack<>();
        Stack<List<Value<?>>> argumentStack = new Stack<>();
        argumentStack.add(new ArrayList<>());
        for (Action action : actions) {
            if (action == Action.INPUT || action == Action.PROCEDURE) {
                if (action == Action.INPUT) {
                    Value<?> value = values.get(readValues);
                    readValues += 1;
                    argumentStack.peek().add(value);
                } else {
                    if (accessStack.peek().getArgumentCount() != 0) {
                        throw new IllegalStateException("Attempt to call non-procedure function as procedure.");
                    }
                    assert argumentStack.peek().isEmpty();
                }
                while (!accessStack.isEmpty() && accessStack.peek().getArgumentCount() == argumentStack.peek().size()) {
                    Func function = accessStack.pop();
                    List<Container> arguments = new ArrayList<>();
                    for (Value<?> value : argumentStack.pop()) {
                        if (value instanceof IdentifierValue identifierValue) {
                            String identifier = identifierValue.getValue();
                            if (storage.containsKey(identifier)) {
                                arguments.add(new Container(identifier, storage.get(identifier)));
                            } else if (functions.containsKey(identifier)) {
                                arguments.add(new Container(null, new FunctionValue(functions.get(identifier))));
                            } else {
                                arguments.add(new Container(identifier, new NumberValue(0)));
                            }
                        } else {
                            arguments.add(new Container(null, value));
                        }
                    }
                    Value<?> result = function.execute(arguments, functions);
                    for (Container argument : arguments) {
                        String name = argument.getName();
                        if (name != null) {
                            storage.put(name, argument.getStoredValue());
                        }
                    }
                    argumentStack.peek().add(result);
                }
            } else if (action == Action.ACCESS) {
                Value<?> value = values.get(readValues);
                readValues += 1;
                Func func;
                if (value instanceof FunctionValue functionValue) {
                    func = functionValue.getValue();
                } else if (value instanceof IdentifierValue identifierValue) {
                    String identifier = identifierValue.getValue();
                    if (storage.containsKey(identifier)) {
                        Value<?> storedValue = storage.get(identifier);
                        if (storedValue instanceof FunctionValue functionValue) {
                            func = functionValue.getValue();
                        } else {
                            throw new IllegalStateException("Attempt to call non-function value '" + identifier + "'.");
                        }
                    } else if (functions.containsKey(identifier)) {
                        func = functions.get(identifier);
                    } else {
                        throw new IllegalStateException("Attempt to call non-existant value '" + identifier + "'.");
                    }
                } else {
                    throw new IllegalStateException("Attempt to call non-function value.");
                }
                argumentStack.add(new ArrayList<>());
                accessStack.add(func);
            } else if (action == Action.STACK_ACCESS) {
                List<Value<?>> arguments = argumentStack.peek();
                Value<?> value = arguments.remove(arguments.size() - 1);
                if (!(value instanceof FunctionValue)) {
                    throw new IllegalStateException("Attempt to call non-function value.");
                }
                Func func = ((FunctionValue)value).getValue();
                argumentStack.add(new ArrayList<>());
                accessStack.add(func);
            }
        }
        if (!accessStack.isEmpty()) {
            throw new IllegalStateException("Insufficient argument count in instruction.");
        }
        List<Value<?>> arguments = argumentStack.pop();
        if (arguments.size() != 1) {
            System.out.println(arguments);
            throw new IllegalStateException("Excess argument count in instruction.");
        }
        return arguments.get(0);
    }
}
