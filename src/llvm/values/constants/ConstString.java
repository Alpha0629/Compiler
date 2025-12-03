package llvm.values.constants;

import llvm.types.ArrayType;
import llvm.types.ValueType;

public class ConstString extends Constant {
    private final String content;

    public ConstString(ArrayType arrayType, String content) {
        // 是数组类型，数组内部应该是char类型
        super(arrayType);
        // this.content = replaceLineBreaks(content);
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return content;
    }
}
