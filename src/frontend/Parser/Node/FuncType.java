package frontend.Parser.Node;

import frontend.Lexer.Token;

public class FuncType {
    // 函数类型 FuncType → 'void' | 'int' 
    private final Token voidToken;
    private final Token intToken;

    public FuncType(Token voidToken, Token intToken) {
        this.voidToken = voidToken;
        this.intToken = intToken;
    }

    public boolean isVoid() {
        // voidToken非空就说明是void类型
        return voidToken != null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (voidToken != null) {
            sb.append(voidToken.toString());
        } else {
            assert(intToken != null);
            sb.append(intToken.toString());
        }
        sb.append("<FuncType>");
        sb.append('\n');
        return sb.toString();
    }
}
