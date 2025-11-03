package frontend.Parser.Node.Exp;

import java.util.ArrayList;
import frontend.Lexer.Token;

public class RelExp {
    // 关系表达式 RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp 
    // 消除左递归 RelExp → AddExp {('<' | '>' | '<=' | '>=') AddExp}
    private final ArrayList<AddExp> addExps;
    private final ArrayList<Token> signs;

    public RelExp(ArrayList<AddExp> addExps, ArrayList<Token> signs) {
        this.addExps = addExps;
        this.signs = signs;
    }

    public ArrayList<AddExp> getAddExps() {
        return addExps;
    }

    public ArrayList<Token> getSigns() {
        return signs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(addExps.get(0).toString());
        sb.append("<RelExp>");
        sb.append("\n");
        for (int i = 0; i < signs.size(); i++) {
            sb.append(signs.get(i).toString());
            sb.append(addExps.get(i + 1).toString());
            sb.append("<RelExp>");
            sb.append("\n");
        }
        return sb.toString();
    }
}
