package frontend.Parser.Node.Statement;

import java.util.ArrayList;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import frontend.Parser.Node.BType;
import frontend.Parser.Node.Exp.Cond;
import frontend.Parser.Node.InitVal;

public class IfStmt implements Stmt {
    // 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // j
    // Stmt → if '(' Btype Ident '=' InitVal ')' Stmt [else Stmt]
    private final BType bType;
    private final Token ident;
    private final InitVal initVal;
    private final ArrayList<Stmt> stmts;

    public IfStmt(BType bType, Token ident, InitVal initVal, ArrayList<Stmt> stmts) {
        this.bType = bType;
        this.ident = ident;
        this.initVal = initVal;
        this.stmts = stmts;
    }

    public Cond getCond() {
        return null;
    }

    public ArrayList<Stmt> getStmts() {
        return stmts;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(TokenType.IFTK.toString() + " " + "if" + "\n");
        sb.append(TokenType.LPARENT.toString() + " " + "(" + "\n");
        sb.append(bType.toString());
        sb.append(ident.toString());
        sb.append(TokenType.EQL.toString() + " " + "=" + "\n");
        sb.append(initVal.toString());
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
