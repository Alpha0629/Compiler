package frontend.Parser.Node.Statement;

import frontend.Lexer.TokenType;
import frontend.Parser.Node.Assign;
import frontend.Parser.Node.Exp.Cond;
import frontend.Parser.Node.Exp.Exp;
import frontend.Parser.Node.LVal;

public class AssignThreeStmt implements Stmt {
    // Stmt → LVal Assign Cond '?' Exp ':' Exp ';'//三目运算符
    private final LVal lVal;
    private final Assign assign;
    private final Cond cond;
    private final Exp exp1;
    private final Exp exp2;

    public AssignThreeStmt(LVal lVal, Assign assign, Cond cond, Exp exp1, Exp exp2) {
        this.lVal = lVal;
        this.assign = assign;
        this.cond = cond;
        this.exp1 = exp1;
        this.exp2 = exp2;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(lVal);
        sb.append(assign);
        sb.append(cond);
        sb.append(TokenType.QUERY.toString() + " " + ";" + "\n");
        sb.append(exp1);
        sb.append(TokenType.COLON.toString() + " " + ";" + "\n");
        sb.append(exp2);
        sb.append(TokenType.SEMICN.toString() + " " + ";" + "\n");
        sb.append("<Stmt>");
        sb.append("\n");
        return sb.toString();
    }

}
