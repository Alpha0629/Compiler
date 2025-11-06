package llvm;

import frontend.Parser.Node.CompUnit;

/**
 * {@code @Description} Ir构建器
 */
public class IrBuilder {
    private final CompUnit AST; // 语法分析得到的抽象语法树
    private final IrSymbolTableStack symbolTableStack;  // 符号表栈，每一个符号表代表一个作用区域

    public IrBuilder(CompUnit AST) {
        this.AST = AST;
        this.symbolTableStack = new IrSymbolTableStack();
    }

    public void buildCompUnitIr() {

    }
}
