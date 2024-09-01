package me.white.echelon.lexer;

import java.util.LinkedList;

public class Lexer {
    private final String code;
    private int position = -1;
    private int line = 1;
    private int lastLine = 0;
    private LinkedList<Token> nextTokens = new LinkedList<>();

    public Lexer(String code) {
        this.code = code;
    }

    public Token peek(int offset) {
        if (nextTokens.size() <= offset) {
            while (nextTokens.size() <= offset) {
                nextTokens.add(readToken());
            }
            return nextTokens.getLast();
        }
        if (offset == 0) {
            return nextTokens.getFirst();
        }
        return nextTokens.get(offset);
    }

    public Token peek() {
        return peek(0);
    }

    public Token read() {
        if (nextTokens.isEmpty()) {
            return readToken();
        }
        return nextTokens.pollLast();
    }

    public void skip(int amount) {
        for (int i = 0; i < amount; ++i) {
            if (nextTokens.isEmpty()) {
                readToken();
            } else {
                nextTokens.pollFirst();
            }
        }
    }

    public void skip() {
        skip(1);
    }

    private String readString() {
        StringBuilder builder = new StringBuilder();
        position += 1;
        while (position < code.length()) {
            char ch = code.charAt(position);
            if (ch == '"' || ch == '\n') {
                break;
            }
            if (ch == '\\') {
                position += 1;
                if (position >= code.length()) {
                    break;
                }
                ch = code.charAt(position);
            }
            builder.append(ch);
            position += 1;
        }
        if (position >= code.length() || code.charAt(position) != '"') {
            throw new IllegalStateException("could not parse string lmao");
        }
        return builder.toString();
    }

    private String readNumber() {
        int start = position;
        boolean dot = false;
        while (position < code.length()) {
            char ch = code.charAt(position);
            if (ch == '.') {
                if (dot) {
                    break;
                }
                dot = true;
            } else if (ch < '0' || ch > '9') {
                break;
            }
            position += 1;
        }
        position -= 1;
        return code.substring(start, position + 1);
    }

    private String readIdentifier() {
        int start = position;
        while (position < code.length()) {
            char ch = code.charAt(position);
            if (ch == '\n' || ch == '\\' || ch == ':' || ch == ',') {
                break;
            }
            position += 1;
        }
        position -= 1;
        return code.substring(start, position + 1);
    }

    private Token readToken() {
        char ch;
        do {
            if (position + 1 >= code.length()) {
                return new Token(Token.EOF, code.length(), line, code.length() - lastLine);
            }
            position += 1;
            ch = code.charAt(position);
        } while (ch == ' ' || ch == '\t');
        int column = position - lastLine;
        switch (ch) {
            case '\n' -> {
                Token token = new Token(Token.EOL, position, line, column);
                line += 1;
                lastLine = position;
                return token;
            }
            case '!' -> {
                return new Token(Token.FUNCTION, position, line, column);
            }
            case ':' -> {
                return new Token(Token.ACCESS, position, line, column);
            }
            case ',' -> {
                return new Token(Token.SEPARATOR, position, line, column);
            }
            case '~' -> {
                return new Token(Token.RETURN, position, line, column);
            }
            case '\\' -> {
                return new Token(Token.EOL, position, line, column);
            }
            case '"' -> {
                return new Token(Token.STRING, position, line, column, readString());
            }
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                return new Token(Token.NUMBER, position, line, column, readNumber());
            }
            case '-', '.' -> {
                if (position + 1 < code.length()) {
                    char ch2 = code.charAt(position + 1);
                    if (ch2 >= '0' && ch2 <= '9') {
                        return new Token(Token.NUMBER, position, line, column, readNumber());
                    }
                }
            }
        }
        return new Token(Token.IDENTIFIER, position, line, column, readIdentifier());
    }
}