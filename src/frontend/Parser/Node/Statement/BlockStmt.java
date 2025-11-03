package frontend.Parser.Node.Statement;

import frontend.Parser.Node.Block;

public class BlockStmt implements Stmt{
    // Block
    private final Block block;

    public BlockStmt(Block block) {
        this.block = block;
    }

    public Block getBlock() {
        return block;
    }

    @Override
    public String toString() {
        return block.toString() + "<Stmt>" + '\n';
    }
}
