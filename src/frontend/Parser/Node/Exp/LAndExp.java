package frontend.Parser.Node.Exp;

import java.util.ArrayList;
import frontend.Lexer.Token;

public class LAndExp {
    // 逻辑与表达式 LAndExp → EqExp | LAndExp '&&' EqExp
    // 消除左递归 LAndExp → EqExp {'&&' EqExp}
    private final ArrayList<EqExp> eqExps;
    private final ArrayList<Token> signs;

    public LAndExp(ArrayList<EqExp> eqExps, ArrayList<Token> signs) {
        this.eqExps = eqExps;
        this.signs = signs;
    }

    public ArrayList<EqExp> getEqExps() {
        return eqExps;
    }

    public ArrayList<Token> getSigns() {
        return signs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(eqExps.get(0).toString());
        sb.append("<LAndExp>");
        sb.append("\n");
        for (int i = 0; i < signs.size(); i++) {
            sb.append(signs.get(i).toString());
            sb.append(eqExps.get(i + 1).toString());
            sb.append("<LAndExp>");
            sb.append("\n");
        }
        return sb.toString();
    }
}
