package frontend.Parser.Node;

import frontend.Lexer.Token;

public class StringConst {
    // 终结符类
    private final Token stringConst;

    public StringConst(Token stringConst) {
        this.stringConst = stringConst;
    }

    @Override
    public String toString() {
        return stringConst.toString();
    }
}
