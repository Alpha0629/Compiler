package frontend.Parser.Node;

public class Decl{
    // 声明 Decl → ConstDecl | VarDecl 
    private final ConstDecl constDecl;
    private final VarDecl varDecl;

    public Decl(ConstDecl constDecl) {
        this.constDecl = constDecl;
        this.varDecl = null;
    }

    public Decl(VarDecl varDecl) {
        this.constDecl = null;
        this.varDecl = varDecl;
    }

    public ConstDecl getConstDecl() {
        return constDecl;
    }

    public VarDecl getVarDecl() {
        return varDecl;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (constDecl != null) {
            sb.append(constDecl.toString());
        } else {
            sb.append(varDecl.toString());
        }
        // sb.append("<Decl>");
        // sb.append('\n');
        return sb.toString();
    }
}
