package frontend.Parser.Node.Exp;

public class ConstExp {
    // 常量表达式 ConstExp → AddExp 注：使用的 Ident 必须是常量 
    private final AddExp addExp;

    public ConstExp(AddExp addExp) {
        this.addExp = addExp;
    }

    public AddExp getAddExp() {
        return addExp;
    }

    @Override
    public String toString() {
        return this.addExp.toString() + "<ConstExp>" + '\n';
    }
}
