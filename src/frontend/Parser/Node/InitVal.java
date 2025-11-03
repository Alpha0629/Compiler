package frontend.Parser.Node;

import java.util.ArrayList;

import frontend.Lexer.TokenType;
import frontend.Parser.Node.Exp.Exp;

public class InitVal {
    // 变量初值 InitVal → Exp | '{' [ Exp { ',' Exp } ] '}' 
    private final Exp exp;
    private final ArrayList<Exp> exps;

    public InitVal(Exp exp) {
        this.exp = exp;
        this.exps = null;
    }

    public InitVal(ArrayList<Exp> exps) {
        this.exp = null;
        this.exps = exps;
    }

    public Exp getExp() {
        return exp;
    }

    public ArrayList<Exp> getExps() {
        return exps;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (exp != null) {  // 是左边那种情况
            sb.append(exp.toString());
        } else {    // 是右边那种情况
            sb.append(TokenType.LBRACE.toString() + " " + "{" + "\n");
            if (exps.size() == 0) {
                // sb.append("{}");
            } else {
                sb.append(exps.get(0).toString());
                for (int i = 1; i < exps.size(); i++) {
                    sb.append(TokenType.COMMA.toString() + " " + "," + "\n");
                    sb.append(exps.get(i).toString());
                }
            }
            sb.append(TokenType.RBRACE.toString() + " " + "}" + "\n");
        }
        sb.append("<InitVal>");
        sb.append('\n');
        return sb.toString();
    }
}
