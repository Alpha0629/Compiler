package frontend.Parser.Node;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;


public class FuncDef {
    // 函数定义 FuncDef → FuncType Ident '(' [FuncFParams] ')' Block // j
    private final FuncType funcType;
    private final Token ident;
    private final FuncFParams funcFParams;
    private final Block block;

    public FuncDef(FuncType funcType, Token ident, FuncFParams funcFParams, Block block) {
        this.funcType = funcType;
        this.ident = ident;
        this.funcFParams = funcFParams;
        this.block = block;
    }

    public FuncType getFuncType() {
        return funcType;
    }

    public boolean isVoid() {
        return funcType.isVoid();
    }

    public Token getIdent() {
        return ident;
    }

    public FuncFParams getFuncFParams() {
        return funcFParams;
    }

    public Block getBlock() {
        return block;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(funcType.toString());
        sb.append(ident.toString());
        sb.append(TokenType.LPARENT.toString() + " " + "(" + "\n");
        if (funcFParams != null) {
            sb.append(funcFParams);
        }
        sb.append(TokenType.RPARENT.toString() + " " + ")" + "\n");
        sb.append(block.toString());
        sb.append("<FuncDef>");
        sb.append('\n');
        return sb.toString();
    }
}
