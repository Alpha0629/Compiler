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
        this.parameters = new ArrayList<>(parameters);
    }

    public ValueType getReturnType() {
        return returnType;
    }

    public ArrayList<ValueType> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return "func";
    }
}
