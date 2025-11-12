package llvm;

import llvm.values.Value;

import java.util.Stack;

/**
 * {@code @Description} Ir栈式符号表
 */
public class IrSymbolTableStack {
    private final Stack<IrSymbolTable> irSymbolTableStack;

    public IrSymbolTableStack() {
        this.irSymbolTableStack = new Stack<>();
        IrSymbolTable currentSymbolTable = new IrSymbolTable();
        this.push(currentSymbolTable);
    }

    public void push(IrSymbolTable irSymbolTable) {
        irSymbolTableStack.push(irSymbolTable);
    }

    public IrSymbolTable pop() {
        return irSymbolTableStack.pop();
    }

    public IrSymbolTable top() {
        return irSymbolTableStack.peek();
    }

    public void putSymbolToGlobal(String name, Value value) {
        irSymbolTableStack.get(0).putSymbol(name, value);
    }

    public void putSymbolToCurScope(String name, Value value) {
        top().putSymbol(name, value);
    }

    public boolean inGlobalScope() {
        return irSymbolTableStack.size() == 1;
    }
}
