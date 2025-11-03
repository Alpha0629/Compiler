package frontend.Parser.Node;

import frontend.Lexer.TokenType;

public class MainFuncDef {
    // 主函数定义 MainFuncDef → 'int' 'main' '(' ')' Block // j
    private final Block block;

    public MainFuncDef(Block block) {
        this.block = block;
    }

    public Block getBlock() {
        return block;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(TokenType.INTTK.toString() + " " + "int" + "\n");
        sb.append(TokenType.MAINTK.toString() + " " + "main" + "\n");
        sb.append(TokenType.LPARENT.toString() + " " + "(" + "\n");
        sb.append(TokenType.RPARENT.toString() + " " + ")" + "\n");
        sb.append(block.toString());
        sb.append("<MainFuncDef>");
        sb.append('\n');
        return sb.toString();
    }
}
