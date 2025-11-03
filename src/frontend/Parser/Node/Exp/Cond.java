package frontend.Parser.Node.Exp;

public class Cond {
    // 条件表达式 Cond → LOrExp 
    private final LOrExp lOrExp;

    public Cond(LOrExp lOrExp) {
        this.lOrExp = lOrExp;
    }

    public LOrExp getLOrExp() {
        return lOrExp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(lOrExp.toString());
        sb.append("<Cond>");
        sb.append('\n');
        return sb.toString();
    }
}
