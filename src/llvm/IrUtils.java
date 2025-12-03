package llvm;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import llvm.types.ArrayType;
import llvm.types.FuncType;
import llvm.types.IntType;
import llvm.types.LabelType;
import llvm.types.PointerType;
import llvm.types.ValueType;
import llvm.types.VoidType;
import llvm.values.BasicBlock;
import llvm.values.Function;
import llvm.values.GlobalString;
import llvm.values.GlobalVar;
import llvm.values.Value;
import llvm.values.constants.ConstArray;
import llvm.values.constants.ConstString;
import llvm.values.constants.Constant;
import llvm.values.instructions.Add;
import llvm.values.instructions.Alloca;
import llvm.values.instructions.And;
import llvm.values.instructions.Branch;
import llvm.values.instructions.Call;
import llvm.values.instructions.Gep;
import llvm.values.instructions.Icmp;
import llvm.values.instructions.Load;
import llvm.values.instructions.Mul;
import llvm.values.instructions.Or;
import llvm.values.instructions.Ret;
import llvm.values.instructions.Sdiv;
import llvm.values.instructions.Srem;
import llvm.values.instructions.Store;
import llvm.values.instructions.Sub;
import llvm.values.instructions.Trunc;
import llvm.values.instructions.Zext;

import java.util.ArrayList;
import java.util.HashMap;

public class IrUtils {
    private final Module module;
    private final IrSymbolTableStack irSymbolTableStack;
    private final HashMap<String, GlobalString> globalStringMap;

    public static int nameCount = 0;
    public static int stringCount = 0;

    public IrUtils(Module module, IrSymbolTableStack irSymbolTableStack) {
        this.module = module;
        this.irSymbolTableStack = irSymbolTableStack;
        this.globalStringMap = new HashMap<>();
    }

    // 处理 %v3 = getelementptr inbounds i32, i32* %v2, i32 1
    // %v3的类型是i32*
    public Gep makeGep(Value pointer, Value offset) {
        PointerType resultType = (PointerType) pointer.getValueType();
        Gep gep = new Gep(String.valueOf(nameCount++), resultType, IrMaker.currentBlock, pointer, offset);
        IrMaker.currentBlock.addInstructionToTail(gep);
        return gep;
    }

    // 处理 %v26 = getelementptr inbounds [15 x i8], [15 x i8]* @str.1, i32 0, i32 0
    // %v26的类型是i8*
    public Gep makeGep(Value pointer, Value left, Value right) {
        // 先获取指针的类型即[15 x i8]*，然后获取被指向的类型即[15 x i8]，强转确保是数组类型
        ArrayType arrayType = (ArrayType) (((PointerType) pointer.getValueType()).getPointedType());
        // 这里new PointerType() 传入的参数是i8
        PointerType resultType = new PointerType(arrayType.getElementType());
        Gep gep = new Gep(String.valueOf(nameCount++), resultType, IrMaker.currentBlock, pointer, left, right);
        IrMaker.currentBlock.addInstructionToTail(gep);
        return gep;
    }

    public GlobalString makeGlobalString(String content) {
        if (globalStringMap.containsKey(content)) {
            return globalStringMap.get(content);
        }
        int length = content.replace("\\0A", "1").length() + 1;
        ArrayType arrayType = new ArrayType(new IntType(8), length);
        PointerType pointerType = new PointerType(arrayType);
        ConstString constString = new ConstString(arrayType, content);
        GlobalString globalString = new GlobalString(String.valueOf(stringCount++), pointerType, constString);
        globalStringMap.put(content, globalString);
        module.addConstString(globalString);
        return globalString;
    }

    public GlobalVar makeGlobalVar(String name, Constant constant, boolean isConst, boolean isStatic) {
        ValueType valueType = constant.getValueType();
        PointerType pointerType = new PointerType(valueType);
        String realName;
        if (isStatic) {
            String funcName = IrMaker.inheritedName;
            realName = "@s_" + funcName + "." + name;
            if (IrMaker.blockDeepth > 1) realName += "." + (IrMaker.blockDeepth - 1);
        } else {
            realName = "@g_" + name;
        }
        GlobalVar globalVar = new GlobalVar(realName, pointerType, isConst, isStatic, constant);
        module.addGlobalVar(globalVar);
        return globalVar;
    }

    public Function makeFunction(String name, FuncType funcType, boolean isDeclare) {
        Function function = new Function(name, funcType, isDeclare);
        module.addFunction(function);
        IrMaker.currentFunction = function;
        return function;
    }

    public BasicBlock makeBasicBlock(boolean updateCurrentBlock) {
        BasicBlock basicBlock = new BasicBlock(String.valueOf(nameCount++), new LabelType(), IrMaker.currentFunction);
        IrMaker.currentFunction.addBlock(basicBlock);
        if (updateCurrentBlock) IrMaker.currentBlock = basicBlock;
        return basicBlock;
    }

    public Alloca makeAlloca(ValueType pointedType) {
        // 如果initArray非空，则代表是Const int arr[]类型
        PointerType pointer = new PointerType(pointedType);
        BasicBlock basicBlock = IrMaker.currentFunction.getFirstBlock();
        Alloca alloca = new Alloca(String.valueOf(nameCount++), pointer, basicBlock);
        basicBlock.addInstructionToHead(alloca);
        return alloca;
    }

    public Alloca makeAlloca(ValueType pointedType, ConstArray initArray) {
        PointerType pointer = new PointerType(pointedType);
        BasicBlock basicBlock = IrMaker.currentFunction.getFirstBlock();
        Alloca alloca = new Alloca(String.valueOf(nameCount++), pointer, basicBlock, initArray);
        basicBlock.addInstructionToHead(alloca);
        return alloca;
    }

    public Branch makeBranch(Value cond, BasicBlock trueBlock, BasicBlock falseBlock) {
        Branch branch = new Branch(new VoidType(), IrMaker.currentBlock, cond, trueBlock, falseBlock);
        IrMaker.currentBlock.addInstructionToTail(branch);
        return branch;
    }

    public Branch makeBranch(BasicBlock dest) {
        Branch branch = new Branch(new VoidType(), IrMaker.currentBlock, dest);
        IrMaker.currentBlock.addInstructionToTail(branch);
        return branch;
    }

    public Call makeCall(Function function, ArrayList<Value> args) {
        Call call;
        ValueType returnType = function.getReturnType();
        if (returnType instanceof VoidType) call = new Call(new VoidType(), IrMaker.currentBlock, function, args);
        else call = new Call(String.valueOf(nameCount++), returnType, IrMaker.currentBlock, function, args);
        IrMaker.currentBlock.addInstructionToTail(call);
        return call;
    }

    public Ret makeRet() {
        Ret ret = new Ret(new VoidType(), IrMaker.currentBlock);
        IrMaker.currentBlock.addInstructionToTail(ret);
        return ret;
    }

    public Ret makeRet(Value returnValue) {
        ValueType returnValueType = returnValue.getValueType();
        Ret ret = new Ret(returnValueType, IrMaker.currentBlock, returnValue);
        IrMaker.currentBlock.addInstructionToTail(ret);
        return ret;
    }

    public Load makeLoad(Value pointer) {
        ValueType pointedType = ((PointerType) pointer.getValueType()).getPointedType();
        Load load = new Load(String.valueOf(nameCount++), pointedType, IrMaker.currentBlock, pointer);
        IrMaker.currentBlock.addInstructionToTail(load);
        return load;
    }

    public Store makeStore(Value storedValue, Value pointer) {
        VoidType voidType = new VoidType();
        Store store = new Store(voidType, IrMaker.currentBlock, storedValue, pointer);
        IrMaker.currentBlock.addInstructionToTail(store);
        return store;
    }

    public Trunc makeTrunc(Value operand, ValueType goalType) {
        Trunc trunc = new Trunc(String.valueOf(nameCount++), goalType, IrMaker.currentBlock, operand);
        IrMaker.currentBlock.addInstructionToTail(trunc);
        return trunc;
    }

    public Zext makeZext(Value operand, ValueType goalType) {
        Zext zext = new Zext(String.valueOf(nameCount++), goalType, IrMaker.currentBlock, operand);
        IrMaker.currentBlock.addInstructionToTail(zext);
        return zext;
    }

    public Icmp makeIcmp(TokenType cond, Value leftOp, Value rightOp) {
        IntType intType = new IntType(1);
        Icmp icmp = new Icmp(String.valueOf(nameCount++), intType, IrMaker.currentBlock, cond, leftOp, rightOp);
        IrMaker.currentBlock.addInstructionToTail(icmp);
        return icmp;
    }

    public Add makeAdd(Value leftOp, Value rightOp) {
        IntType intType = new IntType(32);
        Add add = new Add(String.valueOf(nameCount++), intType, IrMaker.currentBlock, leftOp, rightOp);
        IrMaker.currentBlock.addInstructionToTail(add);
        return add;
    }

    public Sub makeSub(Value leftOp, Value rightOp) {
        IntType intType = new IntType(32);
        Sub sub = new Sub(String.valueOf(nameCount++), intType, IrMaker.currentBlock, leftOp, rightOp);
        IrMaker.currentBlock.addInstructionToTail(sub);
        return sub;
    }

    public Mul makeMul(Value leftOp, Value rightOp) {
        IntType intType = new IntType(32);
        Mul mul = new Mul(String.valueOf(nameCount++), intType, IrMaker.currentBlock, leftOp, rightOp);
        IrMaker.currentBlock.addInstructionToTail(mul);
        return mul;
    }

    public Srem makeSrem(Value leftOp, Value rightOp) {
        IntType intType = new IntType(32);
        Srem srem = new Srem(String.valueOf(nameCount++), intType, IrMaker.currentBlock, leftOp, rightOp);
        IrMaker.currentBlock.addInstructionToTail(srem);
        return srem;
    }

    public Sdiv makeSdiv(Value leftOp, Value rightOp) {
        IntType intType = new IntType(32);
        Sdiv sdiv = new Sdiv(String.valueOf(nameCount++), intType, IrMaker.currentBlock, leftOp, rightOp);
        IrMaker.currentBlock.addInstructionToTail(sdiv);
        return sdiv;
    }

    public And makeAnd(Value leftOp, Value rightOp) {
        IntType intType = new IntType(32);
        And and = new And(String.valueOf(nameCount++), intType, IrMaker.currentBlock, leftOp, rightOp);
        IrMaker.currentBlock.addInstructionToTail(and);
        return and;
    }

    public Or makeOr(Value leftOp, Value rightOp) {
        IntType intType = new IntType(32);
        Or or = new Or(String.valueOf(nameCount++), intType, IrMaker.currentBlock, leftOp, rightOp);
        IrMaker.currentBlock.addInstructionToTail(or);
        return or;
    }

    public ArrayList<String> splitConstString(String origin) {
        ArrayList<String> strings = new ArrayList<>();
        String originString = origin.substring(1, origin.length() - 1);     // 去掉最前面和最后面的双引号
        originString = originString.replace("\\n", "\\0A"); // 替换掉所有的换行符
        // 下面要以%d为界把字符串分开
        int last = 0;
        for (int i = 0; i < originString.length(); i++) {
            if (originString.startsWith("%d", i)) {
                if (last != i) strings.add(originString.substring(last, i));    // 如果是%d%d, 不进行判断会导致添加空串
                strings.add("%d");
                last = i + 2;
            }
        }
        if (last != originString.length()) strings.add(originString.substring(last));   // 如果结尾是%d, 同样会导致空串
        return strings;
    }
}
