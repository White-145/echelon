package me.white.echelon.pipeline;

public class Token {
    enum Type {
        FUNCTION_DECLARATION,
        FUNCTION_RETURN,
        IDENTIFIER,
        ARGUMENT_SEPARATION,
        ACCESS,
        LINE_END,
        NUMBER,
        STRING,
        FILE_END;

        @Override
        public String toString() {
            return switch (this) {
                case FUNCTION_DECLARATION -> "<FNC>";
                case FUNCTION_RETURN -> "<RET>";
                case IDENTIFIER -> "<IDT>";
                case ARGUMENT_SEPARATION -> "<SEP>";
                case ACCESS -> "<ACS>";
                case LINE_END -> "<END>";
                case NUMBER -> "<NUM>";
                case STRING -> "<STR>";
                case FILE_END -> "<EOF>";
            };
        }
    }
    public static Type FUNCTION_DECLARATION = Type.FUNCTION_DECLARATION;
    public static Type FUNCTION_RETURN = Type.FUNCTION_RETURN;
    public static Type IDENTIFIER = Type.IDENTIFIER;
    public static Type ARGUMENT_SEPARATION = Type.ARGUMENT_SEPARATION;
    public static Type ACCESS = Type.ACCESS;
    public static Type LINE_END = Type.LINE_END;
    public static Type NUMBER = Type.NUMBER;
    public static Type STRING = Type.STRING;
    public static Type FILE_END = Type.FILE_END;

    private Type type;
    private String value;
    private int position;

    public Token(Type type, String value, int position) {
        this.type = type;
        this.value = value;
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public String getValue() {
        return value;
    }

    public boolean isOf(Type type) {
        return this.type == type;
    }

    public boolean isOf(Type... types) {
        for (Type type : types) {
            if (this.type == type) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return switch (type) {
            case FUNCTION_DECLARATION -> "<FNC>";
            case FUNCTION_RETURN -> "<RET>";
            case IDENTIFIER -> "<IDT:" + value + ">";
            case ARGUMENT_SEPARATION -> "<SEP>";
            case ACCESS -> "<ACS>";
            case LINE_END -> "<END>";
            case NUMBER -> "<NUM:" + value + ">";
            case STRING -> "<STR:" + value + ">";
            case FILE_END -> "<EOF>";
        };
    }
}
