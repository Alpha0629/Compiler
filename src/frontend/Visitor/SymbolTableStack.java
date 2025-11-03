package frontend.Visitor;

import java.util.Stack;

public class SymbolTableStack {
    private final Stack<SymbolTable> stack;
    private int scopeLayer;
    // 此为作用域层次号，不同于实验课要输出的作用域序号，序号只是每次遇到新的大括号就相加，无层次关系
    // 用栈来维护作用域层次号

    public SymbolTableStack() {
        stack = new Stack<>();
        this.scopeLayer = 0;
    }

    public void push(SymbolTable symbolTable) {
        stack.push(symbolTable);
        scopeLayer++;
    }

    public void pop() {
        stack.pop();
        scopeLayer--;
    }

    public SymbolTable top() {
        return stack.peek();
    }

    public SymbolTable getRoot() {
        return stack.get(0);
    }

    public int getScopeLayer() {
        return scopeLayer;
    }
}
