package llvm.values.constants;

import llvm.types.ArrayType;
import llvm.types.ValueType;

public class ConstString extends Constant {
    private final String content;

    public ConstString(ArrayType arrayType, String content) {
        // 是数组类型，数组内部应该是char类型
        super(arrayType);
        this.content = replaceLineBreaks(content);
    }

    public String replaceLineBreaks(String content) {
        // 把所有换行符都替换成\0A
        return content.replace("\\0A", "\\n");
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return content;
    }
}
