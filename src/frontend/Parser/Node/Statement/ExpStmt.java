package frontend.Parser.Node.Statement;

import frontend.Lexer.TokenType;
import frontend.Parser.Node.Exp.Exp;

public class ExpStmt implements Stmt {
    // [Exp] ';' // i
    private final Exp exp;

    public ExpStmt(Exp exp) {
        this.exp = exp;
    }

    public Exp getExp() {
        return exp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (exp != null) sb.append(exp.toString());
        sb.append(TokenType.SEMICN.toString() + " " + ";" + "\n");
        sb.append("<Stmt>");
        sb.append('\n');
        return sb.toString();
    }
}
