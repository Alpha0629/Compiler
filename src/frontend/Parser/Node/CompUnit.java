package frontend.Parser.Node;
import java.util.ArrayList;

public class CompUnit {
    private final ArrayList<Decl> decls;
    private final ArrayList<FuncDef> funcDefs;
    private final MainFuncDef mainFuncDef;

    public CompUnit(ArrayList<Decl> decls, ArrayList<FuncDef> funcDefs, MainFuncDef mainFuncDef) {
        this.decls = decls;
        this.funcDefs = funcDefs;
        this.mainFuncDef = mainFuncDef;
    }

    public ArrayList<Decl> getDecls() {
        return decls;
    }

    public ArrayList<FuncDef> getFuncDefs() {
        return funcDefs;
    }

    public MainFuncDef getMainFuncDef() {
        return mainFuncDef;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!decls.isEmpty()) {
            for (int i = 0; i < decls.size(); i++) {
                sb.append(decls.get(i).toString());
            }
        }

        if (!funcDefs.isEmpty()) {
            for (int i = 0; i < funcDefs.size(); i++) {
                sb.append(funcDefs.get(i).toString());
            }
        }

        sb.append(mainFuncDef.toString());
        sb.append("<CompUnit>");
        // sb.append('\n');
        return sb.toString();
    }

}
