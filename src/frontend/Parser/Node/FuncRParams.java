package frontend.Parser.Node;

import java.util.ArrayList;

import frontend.Lexer.TokenType;
import frontend.Parser.Node.Exp.Exp;

public class FuncRParams {
    // 函数实参表 FuncRParams → Exp { ',' Exp } 
    private final ArrayList<Exp> exps;

    public FuncRParams(ArrayList<Exp> exps) {
        this.exps = exps;
    }

    public ArrayList<Exp> getExps() {
        return exps;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(exps.get(0).toString());
        for (int i = 1; i < exps.size(); i++) {
            sb.append(TokenType.COMMA.toString() + " " + "," + "\n");
            sb.append(exps.get(i).toString());
        }
        sb.append("<FuncRParams>");
        sb.append('\n');
        return sb.toString();
    }
}
