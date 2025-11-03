package frontend.Parser.Node.Exp;

import frontend.Lexer.TokenType;
import frontend.Parser.Node.LVal;
import frontend.Parser.Node.Number;

public class PrimaryExp {
    // 基本表达式 PrimaryExp → '(' Exp ')' | LVal | Number // j
    private final Exp exp;
    private final LVal lVal;
    private final Number number;

    public PrimaryExp(Exp exp) {
        this.exp = exp;
        this.lVal = null;
        this.number = null;
    }

    public PrimaryExp(LVal lVal) {
        this.exp = null;
        this.lVal = lVal;
        this.number = null;
    }

    public PrimaryExp(Number number) {
        this.exp = null;
        this.lVal = null;
        this.number = number;
    }

    public boolean isArray() {
        if (exp != null) {
            return exp.isArray();
        } else if (lVal != null) {
            return lVal.isArray();
        } else {
            return false;
        }
    }

    public Exp getExp() {
        return exp;
    }

    public LVal getLVal() {
        return lVal;
    }

    public Number getNumber() {
        return number;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (exp != null) {
            sb.append(TokenType.LPARENT.toString() + " " + "(" + "\n");
            sb.append(exp.toString());
            sb.append(TokenType.RPARENT.toString() + " " + ")" + "\n");
        } else if (lVal != null) {
            sb.append(lVal.toString());
        } else {
            sb.append(number.toString());
        }
        sb.append("<PrimaryExp>");
        sb.append('\n');
        return sb.toString();
    }
}
