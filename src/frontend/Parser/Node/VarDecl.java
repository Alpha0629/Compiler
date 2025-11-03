package frontend.Parser.Node;

import java.util.ArrayList;

import frontend.Lexer.TokenType;
import frontend.Lexer.Token;

public class VarDecl {
    // 变量声明 VarDecl → [ 'static' ] BType VarDef { ',' VarDef } ';' // i
    private final Token staticToken;
    private final BType bType;
    private final ArrayList<VarDef> varDefs;

    public VarDecl(Token staticToken, BType bType, ArrayList<VarDef> varDefs) {
        this.staticToken = staticToken;
        this.bType = bType;
        this.varDefs = varDefs;
    }

    public BType getBType() {
        return bType;
    }

    public ArrayList<VarDef> getVarDefs() {
        return varDefs;
    }

    public boolean isStatic() {
        return this.staticToken != null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.staticToken != null) sb.append(staticToken.toString());
        sb.append(bType.toString());
        sb.append(varDefs.get(0).toString());
        for (int i = 1; i < varDefs.size(); i++) {
            sb.append(TokenType.COMMA.toString() + " " + "," + "\n");
            sb.append(varDefs.get(i).toString());
        }
        sb.append(TokenType.SEMICN.toString() + " " + ";" + "\n");
        sb.append("<VarDecl>");
        sb.append('\n');
        return sb.toString();
    }
}
