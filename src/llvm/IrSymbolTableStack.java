package llvm;

import frontend.Visitor.SymbolTable;

import java.util.Stack;

/**
 * {@code @Description} Ir栈式符号表
 */
public class IrSymbolTableStack {
    private final Stack<SymbolTable> irSymbolTableStack;

    public IrSymbolTableStack() {
        this.irSymbolTableStack = new Stack<>();
    }

    public void push(SymbolTable symbolTable) {
        irSymbolTableStack.push(symbolTable);
    }

    public SymbolTable pop() {
        return irSymbolTableStack.pop();
    }

    public SymbolTable top() {
        return irSymbolTableStack.peek();
    }
}
