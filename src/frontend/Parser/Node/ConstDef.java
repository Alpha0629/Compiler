package frontend.Parser.Node;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import frontend.Parser.Node.Exp.ConstExp;

public class ConstDef {
    // 常量定义 ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal // k
    private final Token ident;
    private final ConstExp constExp;
    private final ConstInitVal constInitVal;

    public ConstDef(Token ident, ConstExp constExp, ConstInitVal constInitVal) {
        this.ident = ident;
        this.constExp = constExp;
        this.constInitVal = constInitVal;
    }

    public Token getIdent() {
        return ident;
    }

    public ConstExp getConstExp() {
        return constExp;
    }

    public ConstInitVal getConstInitVal() {
        return constInitVal;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ident.toString());
        if (constExp != null) {
            sb.append(TokenType.LBRACK.toString() + " " + "[" + "\n");
            sb.append(constExp.toString());
            sb.append(TokenType.RBRACK.toString() + " " + "]" + "\n");
        }
        sb.append(TokenType.ASSIGN.toString() + " " + "=" + "\n");
        sb.append(constInitVal.toString());
        sb.append("<ConstDef>");
        sb.append("\n");
        return sb.toString();
    }
}
