package llvm.values.constants;

import llvm.types.CharType;
import llvm.values.User;
import llvm.values.Value;

public class ConstChar extends User {
    private final char ch;

    public ConstChar(CharType charType, char ch) {
        super(charType);
        this.ch = ch;
    }

    @Override
    public String toString() {
        return String.valueOf(ch);
    }
}
