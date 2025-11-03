package frontend.Parser.Node.Statement;

import java.util.ArrayList;

import frontend.Lexer.TokenType;
import frontend.Parser.Node.Exp.Cond;

public class IfStmt implements Stmt {
    // 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // j
    private final Cond cond;
    private final ArrayList<Stmt> stmts;

    public IfStmt(Cond cond, ArrayList<Stmt> stmts) {
        this.cond = cond;
        this.stmts = stmts;
    }

    public Cond getCond() {
        return cond;
    }

    public ArrayList<Stmt> getStmts() {
        return stmts;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(TokenType.IFTK.toString() + " " + "if" + "\n");
        sb.append(TokenType.LPARENT.toString() + " " + "(" + "\n");
        sb.append(cond.toString());
        sb.append(TokenType.RPARENT.toString() + " " + ")" + "\n");
        sb.append(stmts.get(0));
        for (int i = 1; i < stmts.size(); i++) {
            sb.append(TokenType.ELSETK.toString() + " " + "else" + "\n");
            sb.append(stmts.get(i));
        }
        sb.append("<Stmt>");
        sb.append('\n');
        return sb.toString();
    }
}
