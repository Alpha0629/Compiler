package frontend.Parser.Node;

import frontend.Parser.Node.Statement.Stmt;

public class BlockItem {
    // 语句块项 BlockItem → Decl | Stmt 
    private final Decl decl;
    private final Stmt stmt;

    public BlockItem(Decl decl) {
        this.decl = decl;
        this.stmt = null;
    }

    public BlockItem(Stmt stmt) {
        this.decl = null;
        this.stmt = stmt;
    }

    public Decl getDecl() {
        return decl;
    }

    public Stmt getStmt() {
        return stmt;
    }

    @Override
    public String toString() {
        // <BlockItem>不需要输出
        if (decl != null) return decl.toString();
        else return stmt.toString();
    }
}
