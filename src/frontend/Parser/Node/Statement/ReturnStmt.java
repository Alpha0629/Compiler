package frontend.Parser.Node.Statement;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import frontend.Parser.Node.Exp.Exp;

public class ReturnStmt implements Stmt {
    // 'return' [Exp] ';' // i
    private final Token Return;
    private final Exp exp;
    private TokenType funcReturnType;

    public ReturnStmt(Token Return, Exp exp) {
        this.Return = Return;
        this.exp = exp;
        this.funcReturnType = null;
    }

    public void setFuncReturnType(TokenType funcReturnType) {
        this.funcReturnType = funcReturnType;
    }

    public TokenType getFuncReturnType() {
        return funcReturnType;
    }

    public Exp getExp() {
        return exp;
    }

    public Token getToken() {
        return Return;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(TokenType.RETURNTK.toString() + " " + "return" + "\n");
        if (exp != null) {
            sb.append(exp.toString());
        }
        sb.append(TokenType.SEMICN.toString() + " " + ";" + "\n");
        sb.append("<Stmt>");
        sb.append('\n');
        return sb.toString();
    }
}
