package frontend.Parser.Node.Statement;

import java.util.ArrayList;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import frontend.Parser.Node.Exp.Exp;

public class PrintfStmt implements Stmt {
    // 'printf''('StringConst {','Exp}')'';' // i j
    private final Token Printf;
    private final Token stringConst;
    private final ArrayList<Exp> exps;

    public PrintfStmt(Token Printf, Token stringConst, ArrayList<Exp> exps) {
        this.Printf = Printf;
        this.stringConst = stringConst;
        this.exps = exps;
    }

    public Token getToken() {
        return Printf;
    }

    public Token getStringConst() {
        return stringConst;
    }

    public ArrayList<Exp> getExps() {
        return exps;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(TokenType.PRINTFTK.toString() + " " + "printf" + "\n");
        sb.append(TokenType.LPARENT.toString() + " " + "(" + "\n");
        sb.append(stringConst.toString());
        for (int i = 0; i < exps.size(); i++) {
            sb.append(TokenType.COMMA.toString() + " " + "," + "\n");
            sb.append(exps.get(i).toString());
        }
        sb.append(TokenType.RPARENT.toString() + " " + ")" + "\n");
        sb.append(TokenType.SEMICN.toString() + " " + ";" + "\n");
        sb.append("<Stmt>");
        sb.append('\n');
        return sb.toString();
    }
}
