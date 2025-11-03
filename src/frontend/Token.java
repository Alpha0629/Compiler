package frontend;
public class Token {
    private final String key;
    private final TokenType value;
    private final int line;

    public Token(String key, TokenType value, int line) {
        this.key = key;
        this.value = value;
        this.line = line;
    }

    public String getKey() {
        return key;
    }

    public TokenType getValue() {
        return value;
    }

    public int getLine() {
        return line;
    }

    public String toString() {
        return value + " " + key;
    }
}