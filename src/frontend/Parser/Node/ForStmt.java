package frontend.Parser.Node;

import java.util.ArrayList;

import frontend.Lexer.TokenType;
import frontend.Parser.Node.Exp.Exp;

public class ForStmt {
    // 语句 ForStmt → LVal Assign Exp
    private final LVal lVal;
    private final Assign assign;
    private final Exp exp;

    public ForStmt(LVal lVal, Assign assign, Exp exp) {
        this.lVal = lVal;
        this.assign = assign;
        this.exp = exp;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(lVal.toString());
        sb.append(assign.toString());
        sb.append(exp.toString());
        sb.append("<ForStmt>");
        sb.append('\n');
        return sb.toString();
    }
}
