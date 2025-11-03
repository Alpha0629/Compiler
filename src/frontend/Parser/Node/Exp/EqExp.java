package frontend.Parser.Node.Exp;

import java.util.ArrayList;
import frontend.Lexer.Token;

public class EqExp {
    // 相等性表达式 EqExp → RelExp | EqExp ('==' | '!=') RelExp 
    // 消除左递归 EqExp → RelExp {('==' | '!=') RelExp}
    private final ArrayList<RelExp> relExps;
    private final ArrayList<Token> signs;

    public EqExp(ArrayList<RelExp> relExps, ArrayList<Token> signs) {
        this.relExps = relExps;
        this.signs = signs;
    }

    public ArrayList<RelExp> getRelExps() {
        return relExps;
    }

    public ArrayList<Token> getSigns() {
        return signs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(relExps.get(0).toString());
        sb.append("<EqExp>");
        sb.append("\n");
        for (int i = 0; i < signs.size(); i++) {
            sb.append(signs.get(i).toString());
            sb.append(relExps.get(i + 1).toString());
            sb.append("<EqExp>");
            sb.append("\n");
        }
        return sb.toString();
    }
}
