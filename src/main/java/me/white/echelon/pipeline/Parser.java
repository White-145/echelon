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

    public static HashMap<String, Func> parse(Lexer lexer) {
        Parser parser = new Parser(lexer);
        return parser.parseInner(lexer);
    }

    private HashMap<String, Func> parseInner(Lexer lexer) {
        // !\*(&)
        // !:**(&)
        // !x\*(&)
        // !x:**(&)
        HashMap<String, Func> functions = new HashMap<>();
        while (lexer.hasNext()) {
            Token token = lexer.next();
            if (token.isOf(Token.FUNCTION_DECLARATION)) {
                Token token1 = lexer.next();
                if (token1.isOf(Token.LINE_END)) {
                    functions.put("", parseFunction(null));
                } else if (token1.isOf(Token.ACCESS)) {
                    List<String> arguments = parseFunctionArguments();
                    functions.put("", parseFunction(arguments));
                } else if (token1.isOf(Token.IDENTIFIER)) {
                    String name = token1.getValue();
                    Token token2 = lexer.next();
                    if (token2.isOf(Token.LINE_END)) {
                        functions.put(name, parseFunction(null));
                    } else if (token2.isOf(Token.ACCESS)) {
                        List<String> arguments = parseFunctionArguments();
                        functions.put(name, parseFunction(arguments));
                    } else {
                        illegalToken(token2, Token.LINE_END, Token.ACCESS);
                    }
                } else {
                    illegalToken(token1, Token.LINE_END, Token.ACCESS, Token.IDENTIFIER);
                }
            } else if (!token.isOf(Token.LINE_END)) {
                illegalToken(token, Token.FUNCTION_DECLARATION, Token.LINE_END);
            }
        }
        return functions;
    }

    private Func parseFunction(List<String> arguments) {
        // ~v\
        // ~x:*
        // (x:*)~v\
        // (x:*)~x:*
        List<Instruction> instructions = new ArrayList<>();
        Value<?> returnValue = null;
        while (true) {
            Token token = lexer.next();
            if (token.isOf(Token.FUNCTION_RETURN)) {
                Token token1 = lexer.next();
                if (token1.isOf(Token.IDENTIFIER)) {
                    Token token2 = lexer.next();
                    if (token2.isOf(Token.ACCESS)) {
                        returnValue = new InstructionValue(parseInstruction(token1, true));
                    } else if (token2.isOf(Token.LINE_END) || token2.isOf(Token.FILE_END)) {
                        returnValue = new ContainerValue(token1.getValue());
                    } else {
                        illegalToken(token2, Token.ACCESS, Token.LINE_END, Token.FILE_END);
                    }
                } else if (!token1.isOf(Token.LINE_END) && !token1.isOf(Token.FILE_END)) {
                    illegalToken(token1, Token.IDENTIFIER, Token.LINE_END, Token.FILE_END);
                }
                break;
            } else if (token.isOf(Token.IDENTIFIER)) {
                Token token1 = lexer.next();
                if (token1.isOf(Token.ACCESS)) {
                    Instruction instruction = parseInstruction(token, false);
                    instructions.add(instruction);
                } else {
                    illegalToken(token1, Token.ACCESS);
                }
            } else if (!token.isOf(Token.LINE_END)) {
                illegalToken(token, Token.FUNCTION_RETURN, Token.IDENTIFIER);
            }
        }

        return new Func(arguments, instructions, returnValue);
    }

    private Instruction parseInstruction(Token token, boolean allowEOF) {
        // x^\
        // x^(v:|v,|v:,)\
        List<Value<?>> values = new ArrayList<>();
        List<Boolean> accesses = new ArrayList<>();
        List<Boolean> procedures = new ArrayList<>();
        values.add(new ContainerValue(token.getValue()));
        accesses.add(true);
        Token token1 = lexer.see();
        if (token1.isOf(Token.LINE_END) || (allowEOF && token1.isOf(Token.FILE_END))) {
            lexer.next();
            procedures.add(true);
        } else if (token1.isOf(Token.ARGUMENT_SEPARATION)) {
            if (allowEOF) {
                illegalToken(token1, Token.LINE_END, Token.FILE_END, Token.NUMBER, Token.STRING, Token.IDENTIFIER);
            } else {
                illegalToken(token1, Token.LINE_END, Token.NUMBER, Token.STRING, Token.IDENTIFIER);
            }
        } else {
            procedures.add(false);
            while (true) {
                Value<?> value = parseValue(lexer.next());
                values.add(value);
                Token token2 = lexer.next();
                if (token2.isOf(Token.LINE_END) || (allowEOF && token2.isOf(Token.FILE_END))) {
                    accesses.add(false);
                    procedures.add(false);
                    break;
                } else if (token2.isOf(Token.ACCESS) && value instanceof ContainerValue) {
                    accesses.add(true);
                    Token token3 = lexer.see();
                    if (token3.isOf(Token.ARGUMENT_SEPARATION)) {
                        lexer.next();
                        procedures.add(true);
                    } else if (token3.isOf(Token.LINE_END) || (allowEOF && token3.isOf(Token.Type.FILE_END))) {
                        lexer.next();
                        procedures.add(true);
                        break;
                    } else {
                        procedures.add(false);
                    }
                } else if (token2.isOf(Token.ARGUMENT_SEPARATION)) {
                    accesses.add(false);
                    procedures.add(false);
                } else {
                    if (value instanceof ContainerValue) {
                        illegalToken(token2, Token.LINE_END, Token.ACCESS, Token.ARGUMENT_SEPARATION);
                    } else {
                        illegalToken(token2, Token.LINE_END, Token.ARGUMENT_SEPARATION);
                    }
                }
            }
        }
        return new Instruction(values, accesses, procedures);
    }

    private List<String> parseFunctionArguments() {
        // x\
        // x(,x)\
        List<String> arguments = new ArrayList<>();
        while (true) {
            Token token = lexer.next();
            if (token.isOf(Token.IDENTIFIER)) {
                arguments.add(token.getValue());
                Token token1 = lexer.next();
                if (token1.isOf(Token.LINE_END)) {
                    break;
                } else if (!token1.isOf(Token.ARGUMENT_SEPARATION)) {
                    illegalToken(token1, Token.LINE_END, Token.ARGUMENT_SEPARATION);
                }
            } else {
                illegalToken(token, Token.IDENTIFIER);
            }
        }

        return arguments;
    }

    private Value<?> parseValue(Token token) {
        // !\*
        // !:**
        // 0
        // "
        // x
        if (token.isOf(Token.FUNCTION_DECLARATION)) {
            Token token1 = lexer.next();
            if (token1.isOf(Token.LINE_END)) {
                return new FunctionValue(parseFunction(null));
            } else if (token1.isOf(Token.ACCESS)) {
                List<String> arguments = parseFunctionArguments();
                return new FunctionValue(parseFunction(arguments));
            } else {
                illegalToken(token1, Token.LINE_END, Token.ACCESS);
            }
        } else if (token.isOf(Token.NUMBER)) {
            return new NumberValue(Integer.parseInt(token.getValue()));
        } else if (token.isOf(Token.STRING)) {
            return new StringValue(token.getValue());
        } else if (token.isOf(Token.IDENTIFIER)) {
            return new ContainerValue(token.getValue());
        } else {
            illegalToken(token, Token.FUNCTION_DECLARATION, Token.NUMBER, Token.STRING, Token.IDENTIFIER);
        }
        return null;
    }
}
