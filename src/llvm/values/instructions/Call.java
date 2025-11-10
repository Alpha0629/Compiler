package llvm.values.instructions;

import llvm.types.ValueType;
import llvm.types.VoidType;
import llvm.values.Function;
import llvm.values.Value;

import java.util.ArrayList;

public class Call extends Instruction {
    // <result> = call [ret attrs] <ty> <name>(<...args>)
    // %v13 = call i32 @add(i32  3, i32  18)
    //        call void @func(i32  12)
    public Call(String name, ValueType valueType, Value parent, Function function, ArrayList<Value> args) {
        super(name, valueType, parent, concatenate(function, args));
    }

    public Call(ValueType valueType, Value parent, Function function, ArrayList<Value> args) {
        // void返回类型函数，valueType保证是voidType
        super(valueType, parent, concatenate(function, args));
    }

    public static ArrayList<Value> concatenate(Value function, ArrayList<Value> args) {
        ArrayList<Value> initialList = new ArrayList<>();
        initialList.add(function);
        initialList.addAll(args);
        return initialList;
    }

    public Function getFunction() {
        return (Function) super.getOperands().get(0);
    }

    public ArrayList<Value> getArguments() {
        ArrayList<Value> args = new ArrayList<>(super.getOperands());
        args.remove(0); // 去掉function
        return args;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!(super.getValueType() instanceof VoidType)) {
            sb.append(super.getName());
            sb.append(" = ");
        }
        sb.append("call");
        sb.append(" ");
        sb.append(super.getValueType().toString());
        sb.append(" ");
        sb.append(getFunction().getName());
        sb.append("(");
        for (int i = 0; i < getArguments().size(); i++) {
            if (i >= 1) sb.append(", ");
            sb.append(getArguments().get(i).getValueType().toString());
            sb.append(" ");
            sb.append(getArguments().get(i).getName());
        }
        sb.append(")");
        return sb.toString();
    }
}
