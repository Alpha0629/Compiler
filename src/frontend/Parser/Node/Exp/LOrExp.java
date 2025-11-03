package frontend.Parser.Node.Exp;

import java.util.ArrayList;
import frontend.Lexer.Token;

public class LOrExp {
    // 逻辑或表达式 LOrExp → LAndExp | LOrExp '||' LAndExp
    // 消除左递归 LOrExp → LAndExp {'||' LAndExp} 
    private final ArrayList<LAndExp> lAndExps;
    private final ArrayList<Token> signs;

    public LOrExp(ArrayList<LAndExp> lAndExps, ArrayList<Token> signs) {
        this.lAndExps = lAndExps;
        this.signs = signs;
    }

    public ArrayList<LAndExp> getLAndExps() {
        return lAndExps;
    }

    public ArrayList<Token> getSigns() {
        return signs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(lAndExps.get(0).toString());
        sb.append("<LOrExp>");
        sb.append("\n");
        for (int i = 0; i < signs.size(); i++) {
            sb.append(signs.get(i).toString());
            sb.append(lAndExps.get(i + 1).toString());
            sb.append("<LOrExp>");
            sb.append("\n");
        }
        return sb.toString();
    }

}
