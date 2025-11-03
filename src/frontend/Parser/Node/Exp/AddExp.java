package frontend.Parser.Node.Exp;

import java.util.ArrayList;
import frontend.Lexer.Token;

public class AddExp {
    // 加减表达式 AddExp → MulExp | AddExp ('+' | '−') MulExp
    // 消除左递归 AddExp → MulExp {('+' | '−') MulExp}
    private final ArrayList<MulExp> mulExps;
    private final ArrayList<Token> signs;
    // mulExps中有n个元素，则signs中有n-1个元素

    public AddExp(ArrayList<MulExp> mulExps, ArrayList<Token> signs) {
        this.mulExps = mulExps;
        this.signs = signs;
    }

    public ArrayList<MulExp> getMulExps() {
        return mulExps;
    }

    public ArrayList<Token> getSigns() {
        return signs;
    }

    public boolean isArray() {
        return this.mulExps.get(0).isArray();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(mulExps.get(0).toString());
        sb.append("<AddExp>");
        sb.append("\n");
        for (int i = 0; i < signs.size(); i++) {
            sb.append(signs.get(i).toString());
            sb.append(mulExps.get(i + 1).toString());
            sb.append("<AddExp>");
            sb.append("\n");
        }
        return sb.toString();
    }
}
