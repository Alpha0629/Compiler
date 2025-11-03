package frontend.Parser.Node;

import java.util.ArrayList;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;

public class Block {
    // // 语句块 Block → '{' { BlockItem } '}' 
    private final ArrayList<BlockItem> blockItems;
    private final Token lBrace;
    private final Token rBrace;

    public Block(ArrayList<BlockItem> blockItems, Token lBrace, Token rBrace) {
        this.blockItems = blockItems;
        this.lBrace = lBrace;
        this.rBrace = rBrace;
    }

    public ArrayList<BlockItem> getBlockItems() {
        return blockItems;
    }

    public int getRBraceLine() {
        return rBrace.getLine();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(TokenType.LBRACE.toString() + " " + "{" + "\n");
        for (int i = 0; i < blockItems.size(); i++) {
            sb.append(blockItems.get(i).toString());
        }
        sb.append(TokenType.RBRACE.toString() + " " + "}" + "\n");
        sb.append("<Block>");
        sb.append('\n');
        return sb.toString();
    }
}
