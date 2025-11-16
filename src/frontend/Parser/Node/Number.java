package frontend.Parser.Node;

import frontend.Lexer.Token;

public class Number {
    // 数字 Number → IntConst 
    private final Token intConst;

    public Number(Token intConst) {
        this.intConst = intConst;
    }

    public Token getIntConst() {
        return intConst;
    }
    
    @Override
    public String toString() {
        return intConst.toString() + "<Number>" + '\n';
    }

}
