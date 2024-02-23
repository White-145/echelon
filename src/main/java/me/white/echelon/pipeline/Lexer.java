package me.white.echelon.pipeline;

import java.util.function.Predicate;

public class Lexer {
    private String code;
    private int position;
    private Token next;

    public Lexer(String code) {
        this.code = code;
        position = 0;
        next();
    }

    private boolean canRead() {
        return position < code.length();
    }

    public boolean hasNext() {
        return next != null;
    }

    private char peek() {
        if (!canRead()) return '\0';
        return code.charAt(position);
    }

    private char read() {
        char ch = peek();
        position += 1;
        return ch;
    }

    private String readLength(int length) {
        String result = this.code.substring(position, position + length);
        position += length;
        return result;
    }

    private void skipWhitespace() {
        while (peek() == ' ' || peek() == '\t') {
            position += 1;
        }
    }

    private void skipUntil(char ch) {
        while (canRead() && peek() != ch) {
            position += 1;
        }
    }

    private void skipUntil(Predicate<Character> condition) {
        while (canRead() && !condition.test(peek())) {
            position += 1;
        }
    }

    private String readIdentifier() {
        position -= 1;
        int start = position;
        skipUntil(ch -> ch == ',' || ch == ':' || ch == '\n' || ch == '\\');
        int length = position - start;
        position = start;
        return readLength(length);
    }

    private String readLength() {
        int start = position;
        StringBuilder builder = new StringBuilder();
        boolean hasEnded = false;
        while (canRead()) {
            char ch = read();
            if (ch == '"') {
                hasEnded = true;
                break;
            }
            builder.append(ch);
        }
        if (!hasEnded) {
            throw new IllegalStateException("Unable to read string, reached end.");
        }
        return builder.toString();
    }

    private String readNumber() {
        position -= 1;
        int start = position;
        int result = 0;
        while (canRead() && '0' <= peek() && peek() <= '9') {
            position += 1;
        }
        int length = position - start;
        position = start;
        return readLength(length);
    }

    public Token see() {
        if (next == null) {
            return new Token(Token.FILE_END, null, position);
        }
        return next;
    }

    public Token next() {
        skipWhitespace();
        if (!canRead()) {
            if (next == null) {
                return new Token(Token.FILE_END, null, position);
            }
            Token token = next;
            next = null;
            return token;
        }
        int start = position;
        char ch = read();
        Token token = next;
        next = switch (ch) {
            case '!' -> new Token(Token.FUNCTION_DECLARATION, null, start);
            case ',' -> new Token(Token.ARGUMENT_SEPARATION, null, start);
            case ':' -> new Token(Token.ACCESS, null, start);
            case '~' -> new Token(Token.FUNCTION_RETURN, null, start);
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' ->
                    new Token(Token.NUMBER, readNumber(), start);
            case '"' -> new Token(Token.STRING, readLength(), start);
            case '\\', '\n' -> new Token(Token.LINE_END, null, start);
            default -> new Token(Token.IDENTIFIER, readIdentifier(), start);
        };
        return token;
    }
}
