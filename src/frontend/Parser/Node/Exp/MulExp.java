package frontend.Parser.Node.Exp;

import java.util.ArrayList;
import frontend.Lexer.Token;

public class MulExp {
    // 乘除模表达式 MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp 
    // 消除左递归 MulExp → UnaryExp {('*' | '/' | '%') UnaryExp}
    private final ArrayList<UnaryExp> unaryExps;
    private final ArrayList<Token> signs;

    public MulExp(ArrayList<UnaryExp> unaryExps, ArrayList<Token> signs) {
        this.unaryExps = unaryExps;
        this.signs = signs;
    }

    public ArrayList<UnaryExp> getUnaryExps() {
        return unaryExps;
    }

    public ArrayList<Token> getSigns() {
        return signs;
    }

    public boolean isArray() {
        return this.unaryExps.get(0).isArray();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(unaryExps.get(0).toString());
        sb.append("<MulExp>");
        sb.append("\n");
        for (int i = 0; i < signs.size(); i++) {
            sb.append(signs.get(i).toString());
            sb.append(unaryExps.get(i + 1).toString());
            sb.append("<MulExp>");
            sb.append("\n");
        }
        return sb.toString();
    }
}
