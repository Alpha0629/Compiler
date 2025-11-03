package frontend.Parser.Node;

import frontend.Lexer.Token;

public class Assign {
    private Token token;

    public Assign(Token token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return token.toString() + "<Assign>" + "\n";
    }
}
