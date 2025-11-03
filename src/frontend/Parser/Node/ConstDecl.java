package frontend.Parser.Node;

import java.util.ArrayList;
import frontend.Lexer.TokenType;

public class ConstDecl {
    // 常量声明 ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';' // i
    // 不必定义终结符
    private final BType bType;
    private final ArrayList<ConstDef> constDefs;

    public ConstDecl(BType bType, ArrayList<ConstDef> constDefs) {
        this.bType = bType;
        this.constDefs = constDefs;
    }

    public BType getBType() {
        return bType;
    }

    public ArrayList<ConstDef> getConstDefs() {
        return constDefs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(TokenType.CONSTTK.toString() + " " + "const" + "\n");
        sb.append(bType.toString());
        sb.append(constDefs.get(0).toString());
        for (int i = 1; i < constDefs.size(); i++) {
            sb.append(TokenType.COMMA + " " + "," + "\n");
            sb.append(constDefs.get(i).toString());
        }
        sb.append(TokenType.SEMICN.toString() + " " + ";" + "\n");
        sb.append("<ConstDecl>");
        sb.append('\n');
        return sb.toString();
    }
}
