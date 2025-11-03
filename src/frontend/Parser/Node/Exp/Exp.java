package frontend.Parser.Node.Exp;

public class Exp {
    // 表达式 Exp → AddExp 
    private final AddExp addExp;

    public Exp(AddExp addExp) {
        this.addExp = addExp;
    }

    public AddExp getAddExp() {
        return addExp;
    }

    public boolean isArray() {
        return this.addExp.isArray();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(addExp.toString());
        sb.append("<Exp>");
        sb.append('\n');
        return sb.toString();
    }
} 
