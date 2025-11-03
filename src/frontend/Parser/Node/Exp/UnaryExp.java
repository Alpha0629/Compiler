package frontend.Parser.Node.Exp;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import frontend.Parser.Node.FuncRParams;
import frontend.Parser.Node.UnaryOp;

public class UnaryExp {
    // 一元表达式 UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp // j
    private final PrimaryExp primaryExp;
    private final Token ident;
    private final FuncRParams funcRParams;  // 可能为空
    private final UnaryOp unaryOp;
    private final UnaryExp unaryExp;

    public UnaryExp(PrimaryExp primaryExp) {
        this.primaryExp = primaryExp;
        this.ident = null;
        this.funcRParams = null;
        this.unaryOp = null;
        this.unaryExp = null;
    }
    
    public UnaryExp(Token ident, FuncRParams funcRParams) {
        this.primaryExp = null;
        this.ident = ident;
        this.funcRParams = funcRParams;
        this.unaryOp = null;
        this.unaryExp = null;
    }
    
    public UnaryExp(UnaryOp unaryOp, UnaryExp unaryExp) {
        this.primaryExp = null;
        this.ident = null;
        this.funcRParams = null;
        this.unaryOp = unaryOp;
        this.unaryExp = unaryExp;
    }

    public PrimaryExp getPrimaryExp() {
        return primaryExp;
    }

    public boolean isArray() {
        if (primaryExp != null) {
            return this.primaryExp.isArray();
        } else if (ident != null) {
            // 说明是函数，函数目前只有int类型返回值
            // 一行中只能存一个错误，不会既不匹配，又有未声明的函数
            return false;
        } else {
            assert unaryExp != null;
            return this.unaryExp.isArray();
        }
    }

    public Token getIdent() {
        return ident;
    }

    public FuncRParams getFuncRParams() {
        return funcRParams;
    }

    public UnaryOp getUnaryOp() {
        return unaryOp;
    }

    public UnaryExp getUnaryExp() {
        return unaryExp;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (primaryExp != null) {
            sb.append(primaryExp.toString());
        } else if (ident != null) {
            sb.append(ident.toString());
            sb.append(TokenType.LPARENT.toString() + " " + "(" + "\n");
            if (funcRParams != null) {
                sb.append(funcRParams.toString());
            }
            sb.append(TokenType.RPARENT.toString() + " " + ")" + "\n");
        } else if (unaryOp != null) {
            sb.append(unaryOp.toString());
            sb.append(unaryExp.toString());
        }
        sb.append("<UnaryExp>");
        sb.append('\n');
        return sb.toString();
    }
}
