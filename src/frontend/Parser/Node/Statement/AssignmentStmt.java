package frontend.Parser.Node.Statement;

import frontend.Lexer.TokenType;
import frontend.Parser.Node.Assign;
import frontend.Parser.Node.Exp.Exp;
import frontend.Parser.Node.LVal;

public class AssignmentStmt implements Stmt {
    // LVal '=' Exp ';' // i
    private final LVal lVal;
    private final Assign assign;
    private final Exp exp;

    public AssignmentStmt (LVal lVal, Assign assign, Exp exp) {
        this.lVal = lVal;
        this.assign = assign;
        this.exp = exp;
    }

    public LVal getLVal() {
        return lVal;
    }

    public Exp getExp() {
        return exp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(lVal.toString());
        // sb.append(TokenType.ASSIGN.toString() + " " + "=" + "\n");
        sb.append(assign.toString());
        sb.append(exp.toString());
        sb.append(TokenType.SEMICN.toString() + " " + ";" + "\n");
        sb.append("<Stmt>");
        sb.append('\n');
        return sb.toString();
    }
}
