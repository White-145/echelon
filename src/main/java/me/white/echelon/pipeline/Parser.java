package me.white.echelon.pipeline;

import me.white.echelon.program.Func;
import me.white.echelon.program.Instruction;
import me.white.echelon.program.value.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Parser {
    private Lexer lexer;
    private int position;

    private Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    private static void illegalToken(Token token) {
        throw new IllegalStateException("Illegal token at position " + token.getPosition() + ": '" + token + "'.");
    }

    private static void illegalToken(Token token, Token.Type ...expected) {
        StringBuilder builder = new StringBuilder(expected[0].toString());
        for (int i = 1; i < expected.length; ++i) {
            builder.append(", ");
            builder.append(expected[i].toString());
        }
        String expectedString = builder.toString();
        throw new IllegalStateException("Illegal token at position " + token.getPosition() + ": '" + token + "', expected: " + expectedString);
    }

    private static void illegalTokenEOL(Token token, boolean allowEOF, Token.Type ...expected) {
        Token.Type[] types;
        if (allowEOF) {
            types = new Token.Type[expected.length + 2];
            System.arraycopy(expected, 0, types, 2, expected.length);
            types[0] = Token.LINE_END;
            types[1] = Token.FILE_END;
        } else {
            types = new Token.Type[expected.length + 1];
            System.arraycopy(expected, 0, types, 1, expected.length);
            types[0] = Token.LINE_END;
        }
        illegalToken(token, types);
    }

    private static boolean isEOL(Token token, boolean allowEOF) {
        return token.isOf(Token.LINE_END) || (allowEOF && token.isOf(Token.FILE_END));
    }

    public static HashMap<String, Func> parse(Lexer lexer) {
        Parser parser = new Parser(lexer);
        return parser.parseInner(lexer);
    }

    private HashMap<String, Func> parseInner(Lexer lexer) {
        HashMap<String, Func> functions = new HashMap<>();
        while (lexer.hasNext()) {
            Token token = lexer.next();
            if (token.isOf(Token.FUNCTION_DECLARATION)) {
                Token token1 = lexer.next();
                if (token1.isOf(Token.LINE_END)) {
                    functions.put("", parseFunction(null, true));
                } else if (token1.isOf(Token.ACCESS)) {
                    List<String> arguments = parseFunctionArguments();
                    functions.put("", parseFunction(arguments, true));
                } else if (token1.isOf(Token.IDENTIFIER)) {
                    String name = token1.getValue();
                    Token token2 = lexer.next();
                    if (token2.isOf(Token.LINE_END)) {
                        functions.put(name, parseFunction(null, true));
                    } else if (token2.isOf(Token.ACCESS)) {
                        List<String> arguments = parseFunctionArguments();
                        functions.put(name, parseFunction(arguments, true));
                    } else {
                        illegalToken(token2, Token.LINE_END, Token.ACCESS);
                    }
                } else {
                    illegalToken(token1, Token.LINE_END, Token.IDENTIFIER, Token.ACCESS);
                }
            } else if (!token.isOf(Token.LINE_END)) {
                illegalToken(token, Token.LINE_END, Token.FUNCTION_DECLARATION);
            }
        }
        return functions;
    }

    private Func parseFunction(List<String> arguments, boolean allowEOF) {
        List<Instruction> instructions = new ArrayList<>();
        Value<?> returnValue = new NumberValue(0);
        while (true) {
            Token token = lexer.next();
            if (token.isOf(Token.FUNCTION_RETURN)) {
                Token token1 = lexer.see();
                if (token1.isOf(Token.FUNCTION_DECLARATION, Token.NUMBER, Token.STRING, Token.IDENTIFIER)) {
                    lexer.next();
                    Value<?> value = parseValue(token1);
                    returnValue = value;
                    if (value instanceof IdentifierValue || value instanceof FunctionValue) {
                        Token token2 = lexer.see();
                        if (value instanceof FunctionValue && token2.isOf(Token.LINE_END)) {
                            lexer.next();
                            token2 = lexer.see();
                        }
                        if (token2.isOf(Token.ACCESS)) {
                            lexer.next();
                            returnValue = new InstructionValue(parseInstruction(value, true));
                        } else if (!token2.isOf(Token.LINE_END, Token.FILE_END)) {
                            illegalToken(token2, Token.ACCESS, Token.LINE_END, Token.FILE_END);
                        }
                    }
                } else if (!isEOL(token1, allowEOF)) {
                    illegalTokenEOL(token1, allowEOF, Token.IDENTIFIER, Token.NUMBER, Token.STRING, Token.FUNCTION_DECLARATION);
                }
                break;
            } else if (token.isOf(Token.IDENTIFIER, Token.FUNCTION_DECLARATION)) {
                Value<?> value = parseValue(token);
                Token token1 = lexer.next();
                if (value instanceof FunctionValue) {
                    Token token2 = lexer.see();
                    if (token2.isOf(Token.ARGUMENT_SEPARATION, Token.ACCESS)) {
                        token1 = lexer.next();
                    }
                }
                if (!token1.isOf(Token.ACCESS)) {
                    illegalToken(token1, Token.ACCESS);
                }
                Instruction instruction = parseInstruction(value, false);
                instructions.add(instruction);
            } else if (!token.isOf(Token.LINE_END)) {
                illegalToken(token, Token.LINE_END, Token.IDENTIFIER, Token.FUNCTION_DECLARATION, Token.FUNCTION_RETURN);
            }
        }

        return new Func(arguments, instructions, returnValue);
    }

    private Instruction parseInstruction(Value<?> firstValue, boolean allowEOF) {
        List<Value<?>> values = new ArrayList<>();
        List<Instruction.Action> actions = new ArrayList<>();
        values.add(firstValue);
        actions.add(Instruction.ACCESS);
        Token token1 = lexer.see();
        if (token1.isOf(Token.ARGUMENT_SEPARATION)) {
            illegalTokenEOL(token1, allowEOF, Token.IDENTIFIER, Token.NUMBER, Token.STRING, Token.FUNCTION_DECLARATION);
        }
        if (isEOL(token1, allowEOF)) {
            lexer.next();
            actions.add(Instruction.PROCEDURE);
        } else while (true) {
            Token token2 = lexer.next();
            if (token2.isOf(Token.ACCESS)) {
                actions.add(Instruction.PROCEDURE);
                actions.add(Instruction.STACK_ACCESS);
                Token token3 = lexer.see();
                if (isEOL(token3, allowEOF)) {
                    lexer.next();
                    actions.add(Instruction.PROCEDURE);
                    break;
                }
            } else {
                Value<?> value = parseValue(token2);
                Token token3 = lexer.next();
                if (value instanceof FunctionValue) {
                    Token token4 = lexer.see();
                    if (token4.isOf(Token.ARGUMENT_SEPARATION, Token.ACCESS)) {
                        token3 = lexer.next();
                    }
                }
                if (isEOL(token3, allowEOF)) {
                    actions.add(Instruction.INPUT);
                    values.add(value);
                    break;
                }
                if (token3.isOf(Token.ACCESS) && (value instanceof IdentifierValue || value instanceof FunctionValue)) {
                    actions.add(Instruction.ACCESS);
                    values.add(value);
                    Token token4 = lexer.see();
                    if (isEOL(token4, allowEOF)) {
                        lexer.next();
                        actions.add(Instruction.PROCEDURE);
                        break;
                    }
                    if (token4.isOf(Token.ARGUMENT_SEPARATION)) {
                        lexer.next();
                        actions.add(Instruction.PROCEDURE);
                    } else if (!token4.isOf(Token.ACCESS, Token.NUMBER, Token.STRING, Token.IDENTIFIER, Token.FUNCTION_DECLARATION)) {
                        illegalTokenEOL(token4, allowEOF, Token.ARGUMENT_SEPARATION, Token.ACCESS);
                    }
                } else if (token3.isOf(Token.ARGUMENT_SEPARATION)) {
                    actions.add(Instruction.INPUT);
                    values.add(value);
                } else {
                    if (value instanceof IdentifierValue || value instanceof FunctionValue) {
                        illegalTokenEOL(token3, allowEOF, Token.ARGUMENT_SEPARATION, Token.ACCESS);
                    }
                    illegalTokenEOL(token3, allowEOF, Token.ARGUMENT_SEPARATION);
                }
            }
        }
        return new Instruction(values, actions);
    }

    private List<String> parseFunctionArguments() {
        List<String> arguments = new ArrayList<>();
        while (true) {
            Token token = lexer.next();
            if (!token.isOf(Token.IDENTIFIER)) {
                illegalToken(token, Token.IDENTIFIER);
            }
            arguments.add(token.getValue());
            Token token1 = lexer.next();
            if (token1.isOf(Token.LINE_END)) {
                break;
            }
            if (!token1.isOf(Token.ARGUMENT_SEPARATION)) {
                illegalToken(token1, Token.LINE_END, Token.ARGUMENT_SEPARATION);
            }
        }
        return arguments;
    }

    private Value<?> parseValue(Token token) {
        if (token.isOf(Token.FUNCTION_DECLARATION)) {
            Token token1 = lexer.next();
            if (token1.isOf(Token.LINE_END)) {
                return new FunctionValue(parseFunction(null, false));
            }
            if (token1.isOf(Token.ACCESS)) {
                List<String> arguments = parseFunctionArguments();
                return new FunctionValue(parseFunction(arguments, false));
            }
            illegalToken(token1, Token.LINE_END, Token.ACCESS);
        }
        if (token.isOf(Token.NUMBER)) {
            return new NumberValue(Integer.parseInt(token.getValue()));
        }
        if (token.isOf(Token.STRING)) {
            return new StringValue(token.getValue());
        }
        if (token.isOf(Token.IDENTIFIER)) {
            return new IdentifierValue(token.getValue());
        }
        illegalToken(token, Token.IDENTIFIER, Token.NUMBER, Token.STRING, Token.FUNCTION_DECLARATION);
        return null;
    }
}
