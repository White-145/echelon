package me.white.echelon.parser;

import me.white.echelon.Function;
import me.white.echelon.Instruction;
import me.white.echelon.Value;
import me.white.echelon.environment.*;
import me.white.echelon.lexer.Lexer;
import me.white.echelon.lexer.Token;

import java.util.*;

public class Parser {
    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    private Token expect(Token.TokenType ...types) {
        Token token = lexer.read();
        if (token.isOf(types)) {
            return token;
        }
        throw new IllegalStateException("bad token >:( wanted " + Arrays.toString(types) + " and you did " + token);
    }

    private boolean isValue(Token token) {
        return token.isOf(Token.NUMBER, Token.STRING, Token.FUNCTION, Token.IDENTIFIER);
    }

    private String parseIdentifier() {
        return expect(Token.IDENTIFIER).getValue();
    }

    private Value<?> parseValue() {
        Token token = expect(Token.NUMBER, Token.STRING, Token.FUNCTION, Token.IDENTIFIER);
        switch (token.getType()) {
            case NUMBER -> {
                return new Value.NumberValue(Double.parseDouble(token.getValue()));
            }
            case STRING -> {
                return new Value.StringValue(token.getValue());
            }
            case FUNCTION -> {
                return parseLambda();
            }
            case IDENTIFIER -> {
                return new Value.IdentifierValue(token.getValue());
            }
        }
        return null; // unreachable
    }

    private Instruction parseInstruction(Value<?> value) {
        if (value == null) {
            value = parseValue();
        }
        expect(Token.ACCESS);
        List<Instruction.Action> actions = new ArrayList<>();
        while (lexer.peek().isOf(Token.ACCESS)) {
            lexer.skip();
            actions.add(new Instruction.Action(Instruction.ActionType.ACCESS, value));
            value = null;
        }
        actions.add(new Instruction.Action(Instruction.ActionType.ACCESS, value));
        if (lexer.peek().isOf(Token.EOL, Token.EOF)) {
            return new Instruction(actions);
        }
        if (!isValue(lexer.peek())) {
            expect(Token.NUMBER, Token.STRING, Token.FUNCTION, Token.IDENTIFIER, Token.EOL);
        }
        value = parseValue();

        while (true) {
            Token token = lexer.peek();
            if (token.isOf(Token.ACCESS)) {
                lexer.skip();
                actions.add(new Instruction.Action(Instruction.ActionType.ACCESS, value));
                value = isValue(lexer.peek()) ? parseValue() : null;
            } else if (token.isOf(Token.SEPARATOR)) {
                lexer.skip();
                if (value != null) {
                    actions.add(new Instruction.Action(Instruction.ActionType.INPUT, value));
                }
                value = parseValue();
            } else if (token.isOf(Token.EOL, Token.EOF)) {
                break;
            } else {
                expect(Token.ACCESS, Token.SEPARATOR, Token.EOL, Token.EOF);
            }
        }

        if (value != null) {
            actions.add(new Instruction.Action(Instruction.ActionType.INPUT, value));
        }

        return new Instruction(actions);
    }

    private List<String> parseParameters() {
        List<String> parameters = new ArrayList<>();
        expect(Token.ACCESS);
        parameters.add(parseIdentifier());
        while (lexer.peek().isOf(Token.SEPARATOR)) {
            lexer.skip();
            parameters.add(parseIdentifier());
        }

        return parameters;
    }

    private List<Instruction> parseBody() {
        List<Instruction> instructions = new ArrayList<>();
        while (true) {
            Token token = lexer.peek();
            if (token.isOf(Token.EOL)) {
                lexer.skip();
                continue;
            }
            if (token.isOf(Token.EOF, Token.RETURN)) {
                break;
            }
            instructions.add(parseInstruction(null));
            expect(Token.EOL);
        }

        return instructions;
    }

    private Evaluation parseEvaluation() {
        Value<?> value = parseValue();
        if (lexer.peek().isOf(Token.ACCESS)) {
            return new Evaluation.InstructionEvaluation(parseInstruction(value));
        }
        return new Evaluation.LiteralEvaluation(value);
    }

    private Function.Defined parseFunction() {
        expect(Token.FUNCTION);
        String name = null;
        if (lexer.peek().isOf(Token.IDENTIFIER)) {
            name = parseIdentifier();
        }
        List<String> parameters;
        if (lexer.peek().isOf(Token.ACCESS)) {
            parameters = parseParameters();
            expect(Token.EOL);
        } else {
            parameters = new ArrayList<>();
            expect(Token.ACCESS, Token.EOL);
        }
        List<Instruction> instructions = parseBody();
        expect(Token.RETURN);
        Evaluation evaluation = null;
        if (isValue(lexer.peek())) {
            evaluation = parseEvaluation();
        }

        return new Function.Defined(name, parameters, instructions, evaluation);
    }

    private Value.FunctionValue parseLambda() {
        List<String> parameters;
        if (lexer.peek().isOf(Token.ACCESS)) {
            parameters = parseParameters();
            expect(Token.EOL);
        } else {
            parameters = new ArrayList<>();
            expect(Token.ACCESS, Token.EOL);
        }
        List<Instruction> instructions = parseBody();
        expect(Token.RETURN);
        Evaluation evaluation = null;
        if (isValue(lexer.peek())) {
            evaluation = parseEvaluation();
        }

        return new Value.FunctionValue(new Function.Defined(parameters, instructions, evaluation));
    }

    public Environment parse() {
        Function mainFunction = null;
        Map<String, Function> functions = new HashMap<>();
        while (!lexer.peek().isOf(Token.EOF)) {
            if (lexer.peek().isOf(Token.EOL)) {
                lexer.skip();
                continue;
            }
            Function.Defined function = parseFunction();
            if (function.getName() == null) {
                if (mainFunction != null) {
                    throw new IllegalStateException("main function twice bozo");
                }
                mainFunction = function;
            } else {
                if (functions.containsKey(function.getName())) {
                    throw new IllegalStateException("function twice");
                }
                functions.put(function.getName(), function);
            }
        }

        return new Environment(mainFunction, functions);
    }
}
