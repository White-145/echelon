package me.white.echelon.lexer;

public class Token {
    public static final TokenType FUNCTION = TokenType.FUNCTION;
    public static final TokenType ACCESS = TokenType.ACCESS;
    public static final TokenType SEPARATOR = TokenType.SEPARATOR;
    public static final TokenType RETURN = TokenType.RETURN;
    public static final TokenType NUMBER = TokenType.NUMBER;
    public static final TokenType STRING = TokenType.STRING;
    public static final TokenType IDENTIFIER = TokenType.IDENTIFIER;
    public static final TokenType EOL = TokenType.EOL;
    public static final TokenType EOF = TokenType.EOF;
    private final TokenType type;
    private final int position;
    private final int line;
    private final int column;
    private final String value;

    public Token(TokenType type, int position, int line, int column, String value) {
        this.type = type;
        this.position = position;
        this.line = line;
        this.column = column;
        this.value = value;
    }

    public Token(TokenType type, int position, int line, int column) {
        this(type, position, line, column, null);
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public boolean isOf(TokenType ...types) {
        for (TokenType type : types) {
            if (this.type == type) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        if (value == null) {
            return "<" + line + ":" + column + " " + type.toString() + ">";
        }
        return "<" + line + ":" + column + " " + type.toString() + ":'" + value + "'>";
    }

    public enum TokenType {
        FUNCTION("FNC"),
        ACCESS("ACS"),
        SEPARATOR("SEP"),
        RETURN("RET"),
        NUMBER("NUM"),
        STRING("STR"),
        IDENTIFIER("IDT"),
        EOL("EOL"),
        EOF("EOF");

        private final String shortening;

        TokenType(String shortening) {
            this.shortening = shortening;
        }

        @Override
        public String toString() {
            return shortening;
        }
    }
}
