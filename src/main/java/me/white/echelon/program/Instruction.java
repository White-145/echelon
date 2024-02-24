package me.white.echelon.program;

import me.white.echelon.program.value.ContainerValue;
import me.white.echelon.program.value.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Instruction {
    private List<Value<?>> values;
    private List<Boolean> accesses;
    private List<Boolean> procedures;

    public Instruction(List<Value<?>> values, List<Boolean> accesses, List<Boolean> procedures) {
        this.values = values;
        this.accesses = accesses;
        this.procedures = procedures;
    }

    public Value<?> execute(Map<String, ContainerValue> storage, Map<String, Func> functions) {
        List<Func> awaitingFunctions = new ArrayList<>();
        List<List<ContainerValue>> awaitingArguments = new ArrayList<>();
        List<ContainerValue> currentAwaitingArguments = new ArrayList<>();
        awaitingArguments.add(currentAwaitingArguments);
        int argumentCount = 0;
        Value<?> returnValue = null;
        int i = 0;
        while (i < values.size()) {
            Value<?> value = values.get(i);
            if (accesses.get(i)) {
                assert value instanceof ContainerValue;
                String name = ((ContainerValue)value).getValue();
                if (!functions.containsKey(name)) {
                    throw new IllegalStateException("Attempt to call non-existant function: '" + name + "'.");
                }
                Func function = functions.get(name);
                if (function.getArgumentCount() == 0) {
                    if (!procedures.get(i)) {
                        throw new IllegalStateException("Attempt to pass arguments to a procedure.");
                    }
                    Value<?> functionValue = function.execute(List.of(), functions);
                    if (functionValue instanceof ContainerValue containerValue) {
                        currentAwaitingArguments.add(containerValue);
                    } else {
                        ContainerValue argumentValue = new ContainerValue("");
                        argumentValue.setHeldValue(functionValue);
                        currentAwaitingArguments.add(argumentValue);
                    }
                } else {
                    argumentCount = function.getArgumentCount();
                    awaitingFunctions.add(function);
                    currentAwaitingArguments = new ArrayList<>();
                    awaitingArguments.add(currentAwaitingArguments);
                }
            } else {
                assert !awaitingFunctions.isEmpty();
                if (value instanceof ContainerValue containerValue) {
                    if (storage.containsKey(containerValue.getValue())) {
                        currentAwaitingArguments.add(storage.get(containerValue.getValue()));
                    } else {
                        currentAwaitingArguments.add(new ContainerValue(containerValue.getValue()));
                    }
                } else {
                    ContainerValue argumentValue = new ContainerValue("");
                    argumentValue.setHeldValue(value);
                    currentAwaitingArguments.add(argumentValue);
                }
            }
            if (argumentCount == currentAwaitingArguments.size()) {
                assert !awaitingFunctions.isEmpty();
                Func function = awaitingFunctions.get(awaitingFunctions.size() - 1);
                assert function.getArgumentCount() == argumentCount;
                Value<?> functionValue = function.execute(currentAwaitingArguments, functions);
                awaitingFunctions.remove(awaitingFunctions.size() - 1);
                for (ContainerValue argument : currentAwaitingArguments) {
                    if (!argument.getValue().equals("")) {
                        storage.put(argument.getValue(), argument);
                    }
                }
                awaitingArguments.remove(awaitingArguments.size() - 1);
                currentAwaitingArguments = awaitingArguments.get(awaitingArguments.size() - 1);
                if (awaitingFunctions.isEmpty()) {
                    returnValue = functionValue;
                    if (i != values.size() - 1) {
                        throw new IllegalStateException("Excessive arguments in instruction.");
                    }
                } else {
                    argumentCount = awaitingFunctions.get(awaitingFunctions.size() - 1).getArgumentCount();
                    if (functionValue instanceof ContainerValue containerValue) {
                        awaitingArguments.add(currentAwaitingArguments);
                    } else {
                        ContainerValue argumentValue = new ContainerValue("");
                        argumentValue.setHeldValue(functionValue);
                        currentAwaitingArguments.add(argumentValue);
                    }
                }
            }
            i += 1;
        }
        while (!awaitingFunctions.isEmpty()) {
            if (currentAwaitingArguments.size() < argumentCount) {
                throw new IllegalStateException("Deficient arguments in instruction.");
            }
            Func function = awaitingFunctions.get(awaitingFunctions.size() - 1);
            assert function.getArgumentCount() == argumentCount;
            Value<?> functionValue = function.execute(currentAwaitingArguments, functions);
            awaitingFunctions.remove(awaitingFunctions.size() - 1);
            for (ContainerValue argument : currentAwaitingArguments) {
                if (!argument.getValue().equals("")) {
                    storage.put(argument.getValue(), argument);
                }
            }
            awaitingArguments.remove(awaitingArguments.size() - 1);
            currentAwaitingArguments = awaitingArguments.get(awaitingArguments.size() - 1);
            if (awaitingFunctions.isEmpty()) {
                returnValue = functionValue;
            } else {
                argumentCount = awaitingFunctions.get(awaitingFunctions.size() - 1).getArgumentCount();
                ContainerValue argumentValue = new ContainerValue("");
                argumentValue.setHeldValue(functionValue);
                currentAwaitingArguments.add(argumentValue);
            }
        }
        assert returnValue != null;
        return returnValue;
    }
}
