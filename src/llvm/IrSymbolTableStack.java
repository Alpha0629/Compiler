package llvm;

import llvm.values.Function;
import llvm.values.Value;

import java.util.Stack;

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

    public IrSymbolTable root() {
        return irSymbolTableStack.get(0);
    }

    public void putSymbolToCurScope(String name, Value value) {
        top().putSymbol(name, value);
    }

    public boolean inGlobalScope() {
        return irSymbolTableStack.size() == 1;
    }

    public Value findSymbol(String name) {
        // 必定能找到
        for (int i = irSymbolTableStack.size() - 1; i >= 0; i--) {
            IrSymbolTable irSymbolTable = irSymbolTableStack.get(i);
            if (irSymbolTable.hasSymbol(name)) {
                return irSymbolTable.getSymbol(name);
            }
        }
        return null;
    }
}
