package llvm.values;

import llvm.types.IntType;
import llvm.types.PointerType;
import llvm.types.ValueType;
import llvm.types.VoidType;
import llvm.values.constants.ConstString;

import java.util.ArrayList;
import java.util.Collections;

public class Function extends Value {
    // declare i32 @getint()          ; 读取一个整数
    // declare void @putint(i32)      ; 输出一个整数
    // declare void @putch(i32)       ; 输出一个字符
    // declare void @putstr(i8*)      ; 输出字符串
    public static Function getint = new Function("getint", new IntType(32), new ArrayList<>(), true);
    public static Function putint = new Function("putint", new VoidType(), new ArrayList<>(Collections.singleton(new IntType(32))), true);
    public static Function putch = new Function("putch", new VoidType(), new ArrayList<>(Collections.singleton(new IntType(32))), true);
    public static Function putstr = new Function("putstr", new VoidType(), new ArrayList<>(Collections.singleton(new PointerType(new IntType(8)))), true);

    private final Boolean isDeclare;
    private final ArrayList<ValueType> argTypes;
    private final ArrayList<Value> args;
    private final ArrayList<BasicBlock> blocks;

    public Function(String name, ValueType returnType, ArrayList<ValueType> argTypes, boolean isDeclare) {
        super("@" + name, returnType);
        this.isDeclare = isDeclare;
        this.argTypes = argTypes;
        this.args = makeArgs(argTypes, isDeclare);
        this.blocks = new ArrayList<>();
    }

    public void addBlock(BasicBlock block) {
        blocks.add(block);
    }

    public ArrayList<Value> makeArgs(ArrayList<ValueType> argTypes, boolean isDeclare) {
        ArrayList<Value> args = new ArrayList<>();
        // 如果只是声明形式，那么就不需要创建具体的args，直接返回新的ArrayList即可
        // 如果是定义形式，那么需要创建参数列表
        if (isDeclare) return args;
        for (int i = 0; i < argTypes.size(); i++) {
            ValueType argType = argTypes.get(i);
            Value arg = new Value("%arg" + i, argType, this);
            args.add(arg);
        }
        return args;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isDeclare) {
            // 声明函数
            // declare void @putint(i32)      ; 输出一个整数
            sb.append("declare");
            sb.append(" ");
            sb.append(super.getValueType());
            sb.append(" ");
            sb.append(super.getName());
            sb.append("(");
            for (int i = 0; i < argTypes.size(); i++) {
                if (i >= 1) sb.append(", ");
                ValueType argType = argTypes.get(i);
                sb.append(argType.toString());
            }
            sb.append(")");
        } else {
            // 定义函数
            // define dso_local i32 @add(i32 %a0, i32 %a1, i32 %a2, i32 %a3, i32 %a4)
            // define dso_local i32 @main() {
            // b9:
            //     %v10 = getelementptr inbounds [6 x i8], [6 x i8]* @str.0, i32 0, i32 0
            //     call void @putstr(i8*  %v10)
            //     ret i32 0

            // b10:
            //     br label %b11
            // }
            sb.append("define dso_local");
            sb.append(" ");
            sb.append(super.getValueType());
            sb.append(" ");
            sb.append(super.getName());
            sb.append("(");
            for (int i = 0; i < args.size(); i++) {
                if (i >= 1) sb.append(", ");
                Value arg = args.get(i);
                sb.append(arg.getValueType().toString());
                sb.append(" ");
                sb.append(arg.getName());
            }
            sb.append(")");
            sb.append("{ ");
            sb.append('\n');
            for (int i = 0; i < blocks.size(); i++) {
                if (i >= 1) sb.append('\n');
                sb.append(blocks.get(i).toString());
            }
            sb.append('\n');
            sb.append("}");
        }
        return sb.toString();
    }
}
