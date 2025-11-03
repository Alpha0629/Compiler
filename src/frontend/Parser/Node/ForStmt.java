package frontend.Parser.Node;

import java.util.ArrayList;

import frontend.Lexer.TokenType;
import frontend.Parser.Node.Exp.Exp;

public class ForStmt {
    // 语句 ForStmt → LVal '=' Exp { ',' LVal '=' Exp } 
    private final ArrayList<LVal> lVals;
    private final ArrayList<Exp> exps;

    public ForStmt(ArrayList<LVal> lVals, ArrayList<Exp> exps) {
        this.lVals = lVals;
        this.exps = exps;
    }

    public ArrayList<LVal> getLVals() {
        return lVals;
    }

    public ArrayList<Exp> getExps() {
        return exps;
    }

    @Override
    public String toString() {
        assert(lVals.size() == exps.size());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lVals.size(); i++) {
            if (i >= 1) {
                sb.append(TokenType.COMMA + " " + "," + "\n");
            }
            sb.append(lVals.get(i).toString());
            sb.append(TokenType.ASSIGN + " " + "=" + "\n");
            sb.append(exps.get(i).toString());
        }
        sb.append("<ForStmt>");
        sb.append('\n');
        return sb.toString();
    }
}
