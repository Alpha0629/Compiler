package frontend.Parser.Node;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import frontend.Parser.Node.Exp.ConstExp;

public class VarDef {
    // 变量定义 VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal // k
    private final Token ident;
    private final ConstExp constExp;
    private final InitVal initVal;

    public VarDef(Token ident) {
        // int i;
        this.ident = ident;
        this.constExp = null;
        this.initVal = null;
    }

    public VarDef(Token ident, InitVal initVal) {
        // int i = 2;
        this.ident = ident;
        this.constExp = null;
        this.initVal = initVal;
    }

    public VarDef(Token ident, ConstExp constExp) {
        // int i[2];
        this.ident = ident;
        this.constExp = constExp;
        this.initVal = null;
    }

    public VarDef(Token ident, ConstExp constExp, InitVal initVal) {
        // int i[2] = {};
        this.ident = ident;
        this.constExp = constExp;
        this.initVal = initVal;
    }

    public Token getIdent() {
        return ident;
    }

    public ConstExp getConstExp() {
        return constExp;
    }

    public InitVal getInitVal() {
        return initVal;
    }

    @Override
    public String toString() {
        // System.out.println("正在解析");
        StringBuilder sb = new StringBuilder();
        assert(ident != null);
        sb.append(ident.toString());
        if (constExp != null) {
            sb.append(TokenType.LBRACK.toString() + " " + "[" + "\n");
            sb.append(constExp.toString());
            sb.append(TokenType.RBRACK.toString() + " " + "]" + "\n");
        }
        if (initVal != null) {
            sb.append(TokenType.ASSIGN.toString() + " " + "=" + "\n");
            sb.append(initVal.toString());
        }
        sb.append("<VarDef>");
        sb.append('\n');
        return sb.toString();
    }
}
