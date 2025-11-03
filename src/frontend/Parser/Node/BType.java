package frontend.Parser.Node;

import frontend.Lexer.Token;

public class BType {
    private final Token bType;

    public BType(Token bType) {
        this.bType = bType;
    }

    public Token getToken() {
        return bType;
    }

    @Override
    public String toString() {
        // return this.bType.toString() + "<BType>" + '\n';
        return this.bType.toString();
    }
}
