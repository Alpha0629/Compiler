package frontend.Parser.Node.Statement;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;

public class ContinueStmt implements Stmt {
    // 'continue' ';' // i
    // 无需定义成员变量
    public Token Continue;

    public ContinueStmt(Token Continue) {
        this.Continue = Continue;
    }

    public Token getToken() {
        return Continue;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(TokenType.CONTINUETK.toString() + " " + "continue" + "\n");
        sb.append(TokenType.SEMICN.toString() + " " + ";" + "\n");
        sb.append("<Stmt>");
        sb.append('\n');
        return sb.toString();
    }
}
