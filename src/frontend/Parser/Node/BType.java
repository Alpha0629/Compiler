package frontend.Parser.Node;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;

public class BType {
    private final Token bType;

    public BType(Token bType) {
        this.bType = bType;
    }

    public Token getToken() {
        return bType;
    }

    public boolean isInt() {
        return bType.getValue() == TokenType.INTTK;
    }

    public boolean isChar() {
        return false;
    }

    @Override
    public String toString() {
        // return this.bType.toString() + "<BType>" + '\n';
        return this.bType.toString();
    }
}
