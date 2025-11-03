package frontend.Parser.Node;

import frontend.Lexer.Token;

public class UnaryOp {
    // 单目运算符 UnaryOp → '+' | '−' | '!' 注：'!'仅出现在条件表达式中
    private final Token unaryOp;

    public UnaryOp(Token unaryOp) {
        this.unaryOp = unaryOp;
    }
    
    
    @Override
    public String toString() {
        return unaryOp.toString() + "<UnaryOp>" + '\n';
    }

}
