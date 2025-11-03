package frontend.Parser.Node.Statement;

import frontend.Lexer.TokenType;
import frontend.Parser.Node.Exp.Cond;
import frontend.Parser.Node.ForStmt;

public class ForLoopStmt implements Stmt {
    // 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt 
    private final ForStmt headForStmt;
    private final Cond cond;
    private final ForStmt rearForStmt;
    private final Stmt stmt;

    public ForLoopStmt(ForStmt headForStmt, Cond cond, ForStmt rearForStmt, Stmt stmt) {
        this.headForStmt = headForStmt;
        this.cond = cond;
        this.rearForStmt = rearForStmt;
        this.stmt = stmt;
    }

    public ForStmt getHeadForStmt() {
        return headForStmt;
    }

    public Cond getCond() {
        return cond;
    }

    public ForStmt getRearForStmt() {
        return rearForStmt;
    }

    public Stmt getStmt() {
        return stmt;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(TokenType.FORTK.toString() + " " + "for" + "\n");
        sb.append(TokenType.LPARENT.toString() + " " + "(" + "\n");
        if (headForStmt != null) sb.append(headForStmt.toString());
        sb.append(TokenType.SEMICN.toString() + " " + ";" + "\n");
        if (cond != null) sb.append(cond.toString());
        sb.append(TokenType.SEMICN.toString() + " " + ";" + "\n");
        if (rearForStmt != null) sb.append(rearForStmt.toString());
        sb.append(TokenType.RPARENT.toString() + " " + ")" + "\n");
        sb.append(stmt.toString());
        sb.append("<Stmt>");
        sb.append('\n');
        return sb.toString();
    }
}
