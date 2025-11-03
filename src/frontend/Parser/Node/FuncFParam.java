package frontend.Parser.Node;

import frontend.Lexer.Token;

public class FuncFParam {
    // 函数形参 FuncFParam → BType Ident ['[' ']'] // k
    private final BType bType;
    private final Token identToken;
    private final Token lbrackToken;
    private final Token rbrackToken;

    public FuncFParam(BType bType, Token identToken, Token lbrackToken, Token rbrackToken) {
        this.bType = bType;
        this.identToken = identToken;
        this.lbrackToken = lbrackToken;
        this.rbrackToken = rbrackToken;
    }

    public BType getBType() {
        return bType;
    }

    public Token getIdent() {
        return identToken;
    }

    public boolean isArray() {
        return lbrackToken != null && rbrackToken != null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(bType.toString());
        sb.append(identToken.toString());
        if (lbrackToken != null) {
            sb.append(lbrackToken.toString());
            sb.append(rbrackToken.toString());
        }
        sb.append("<FuncFParam>");
        sb.append('\n');
        return sb.toString();
    }
}
