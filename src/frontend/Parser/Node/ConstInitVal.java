package frontend.Parser.Node;

import java.util.ArrayList;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import frontend.Parser.Node.Exp.ConstExp;

public class ConstInitVal {
    // // 常量初值 ConstInitVal → ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}' | StringConst
    private final ConstExp constExp;
    private final ArrayList<ConstExp> constExps;
    private final Token stringConst;

    public ConstInitVal(ConstExp constExp, ArrayList<ConstExp> constExps, Token stringConst) {
        this.constExp = constExp;
        this.constExps = constExps;
        this.stringConst = stringConst;
    }

    public ConstInitVal(ConstExp constExp) {
        this.constExp = constExp;
        this.constExps = null;
        this.stringConst = null;
    }

    public ConstInitVal(ArrayList<ConstExp> constExps) {
        this.constExp = null;
        this.constExps = constExps;
        this.stringConst = null;
    }

    public ConstInitVal(Token stringConst) {
        this.constExp = null;
        this.constExps = null;
        this.stringConst = stringConst;
    }

    public ConstExp getConstExp() {
        return constExp;
    }

    public ArrayList<ConstExp> getConstExps() {
        return constExps;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (stringConst != null) {  // 是第三种
            sb.append(stringConst.toString());
        } else {    // 是第一或者第二
            if (constExp != null) { // 是第一种
                sb.append(constExp.toString());
            } else {    // 是第二种
                sb.append(TokenType.LBRACE.toString() + " " + "{" + "\n");
                if (constExps.size() == 0) {
                    // sb.append("{}");
                } else {
                    sb.append(constExps.get(0));
                    for (int i = 1; i < constExps.size(); i++) {
                        sb.append(TokenType.COMMA.toString() + " " + "," + "\n");
                        sb.append(constExps.get(i).toString());
                    }
                }
                sb.append(TokenType.RBRACE.toString() + " " + "}" + "\n");
            }
        }
        sb.append("<ConstInitVal>");
        sb.append('\n');
        return sb.toString();
    }
}
