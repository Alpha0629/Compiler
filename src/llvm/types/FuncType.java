package llvm.types;

import java.util.ArrayList;

/**
 * {@code @Description} 函数变量
 */
public class FuncType extends ValueType {
    private final ValueType returnType;
    private final ArrayList<ValueType> parameters;

    public FuncType(ValueType returnType, ArrayList<ValueType> parameters) {
        this.returnType = returnType;
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "func";
    }
}
