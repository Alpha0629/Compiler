package frontend.Parser.Node.Statement;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;

public class BreakStmt implements Stmt {
    // 'break' ';' //i
    // 无需定义成员变量
    private Token Break;

    public BreakStmt(Token Break) {
        this.Break = Break;
    }

    public Token getToken() {
        return Break;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(TokenType.BREAKTK.toString() + " " + "break" + "\n");
        sb.append(TokenType.SEMICN.toString() + " " + ";" + "\n");
        sb.append("<Stmt>");
        sb.append('\n');
        return sb.toString();
    }
}
