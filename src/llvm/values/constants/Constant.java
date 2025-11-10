package llvm.values.constants;

import llvm.types.ValueType;
import llvm.values.User;
import llvm.values.Value;

import java.util.ArrayList;

public class Constant extends User {
    public Constant(ValueType valueType) {
        super(valueType);
    }
}
