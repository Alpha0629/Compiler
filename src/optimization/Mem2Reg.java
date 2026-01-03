package optimization;

import backend.MipsModule;
import llvm.IrMaker;
import llvm.IrModule;
import llvm.IrUtils;
import llvm.types.IntType;
import llvm.types.PointerType;
import llvm.values.BasicBlock;
import llvm.values.Function;
import llvm.values.User;
import llvm.values.Value;
import llvm.values.instructions.Alloca;
import llvm.values.instructions.Instruction;
import llvm.values.instructions.Load;
import llvm.values.instructions.Phi;
import llvm.values.instructions.Store;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

public class Mem2Reg {
    private final IrModule irModule;
    // private final MipsModule mipsModule;
    private final ArrayList<Alloca> promotableAllocas;
    private final LinkedHashSet<BasicBlock> blocksOfStore;
    private final ArrayList<Instruction> writeInstructions;
    private final ArrayList<Instruction> readInstructions;
    private final Stack<Value> stack;

    public Mem2Reg(IrModule irModule, MipsModule mipsModule) {
        this.irModule = irModule;
        // this.mipsModule = mipsModule;
        promotableAllocas = new ArrayList<>();
        blocksOfStore = new LinkedHashSet<>();
        writeInstructions = new ArrayList<>();
        readInstructions = new ArrayList<>();
        stack = new Stack<>();
    }

    public void pass() {
        ArrayList<Function> functions = irModule.getFunctions();
        for (Function function : functions) {
            System.out.println("\nCurFunction: " + function.getName());
            if (function.isDeclare()) continue;
            findPromotableAllocas(function);
            delteUselessStoreLoad(function);
            addPhiNode(function);
        }
    }

    public void addPhiNode(Function function) {
        for (Alloca alloca : promotableAllocas) {
            this.blocksOfStore.clear();
            this.writeInstructions.clear();
            this.readInstructions.clear();
            System.out.println("\n=======================");
            ArrayList<User> users = alloca.getUsers();
            // 遍历所有使用当前alloca的指令
            for (User user : users) {
                // 使用者所在的基本块
                BasicBlock parentBlock = (BasicBlock) user.getParent();
                if (user instanceof Load load) {
                    this.readInstructions.add(load);
                } else if (user instanceof Store store) {
                    this.blocksOfStore.add(parentBlock);
                    this.writeInstructions.add(store);
                }
            }
            if (this.blocksOfStore.isEmpty()) deleteUselessAlloca(alloca);

            // 之前准备完成，现在加入phi节点
            // 需要插入phi的block
            LinkedHashSet<BasicBlock> phiBlocks = new LinkedHashSet<>();
            for (BasicBlock block : blocksOfStore) {
                for (BasicBlock boundaryBlock : block.getDominanceBoundaries()) {
                    phiBlocks.add(boundaryBlock);
                }
            }
            boolean steady = false;
            while (!steady) {
                steady = true;
                LinkedHashSet<BasicBlock> newPhiBlocks = new LinkedHashSet<>(phiBlocks);
                for (BasicBlock temp : phiBlocks) {
                    for (BasicBlock boundaryBlock : temp.getDominanceBoundaries()) {
                        if (!newPhiBlocks.contains(boundaryBlock)) {
                            newPhiBlocks.add(boundaryBlock);
                            steady = false;
                        }
                    }
                }
                phiBlocks = newPhiBlocks;
            }
            // 需要插入phi的block找到了，现在插入phi
            for (BasicBlock phiBlock : phiBlocks) {
                System.out.println("Make Phi in: " + phiBlock.getName());
                Phi phi = IrUtils.makePhi(alloca, phiBlock);
                phiBlock.insertPhiToHead(phi);
                this.writeInstructions.add(phi);    // Phi 也产生一个值（它是多个前驱路径汇合后的结果）
                this.readInstructions.add(phi);     // Phi 指令需要从前驱路径“索要”值，所以它也是“使用者”
                System.out.println(phi.getName());
                // System.out.println(phiBlock);
            }
            this.stack.clear();
            // rename(function.getFirstBlock(), alloca);
            renameIterative(alloca, function.getFirstBlock());
        }

    }

    public void renameIterative(Alloca alloca, BasicBlock entryBlock) {
        // 任务栈：Entry<基本块, 是否为回溯标记>
        // SimpleEntry<K, V> 是 Java 自带的 Pair 实现
        Stack<SimpleEntry<BasicBlock, Boolean>> workStack = new Stack<>();

        // 映射表：存储每个块当时压入 writeStack 的次数，用于回溯
        HashMap<BasicBlock, Integer> blockPushCounts = new HashMap<>();

        // 初始任务：进入入口块
        workStack.push(new SimpleEntry<>(entryBlock, false));

        while (!workStack.isEmpty()) {
            SimpleEntry<BasicBlock, Boolean> currentEntry = workStack.pop();
            BasicBlock currentBlock = currentEntry.getKey();
            boolean isExit = currentEntry.getValue();

            if (!isExit) {
                // === 【进入块阶段】 ===
                int pushCount = 0;

                // 1. 处理指令 (Alloca/Store/Load 提升)
                ArrayList<Instruction> patterList = currentBlock.getInstructions();
                ArrayList<Instruction> instList = new ArrayList<>(patterList);

                for (Instruction instr : instList) {
                    if (instr == alloca) {
                        patterList.remove(instr);
                        continue;
                    }
                    if (writeInstructions.contains(instr)) {
                        if (instr instanceof Phi phi) {
                            this.stack.push(phi);
                            pushCount++;
                        } else if (instr instanceof Store store) {
                            this.stack.push(store.getStoredValue());
                            pushCount++;
                            store.deleteAllUsageRelationship();
                            patterList.remove(store);
                        }
                    } else if (readInstructions.contains(instr) && instr instanceof Load load) {
                        Value stackTopValue = IrMaker.CInf;
                        if (!stack.isEmpty()) stackTopValue = stack.peek();
                        for (User user : load.getUsers()) {
                            ArrayList<Value> operands = user.getOperands();
                            for (int i = 0; i < operands.size(); i++) {
                                Value operand = user.getOperands().get(i);
                                if (operand == load) {
                                    if (user instanceof Phi phi) {
                                        phi.updateSourceBlock2sourceValue(operand, stackTopValue);
                                    }
                                    operands.set(i, stackTopValue);
                                    System.out.println(operands.get(i).getName());
                                }
                            }
                        }
                        // 删除 load
                        load.deleteAllUsageRelationship();
                        patterList.remove(load);
                    }
                }

                // 2. 填充后继块的 Phi
                for (BasicBlock succ : currentBlock.getSuccessors()) {
                    for (Instruction instr : succ.getInstructions()) {
                        if (instr instanceof Phi phi && readInstructions.contains(phi)) {
                            Value stackTopValue = IrMaker.CInf;
                            if (!stack.isEmpty()) stackTopValue = stack.peek();
                            phi.buildOperand(stackTopValue, currentBlock);
                        }
                        if (!(instr instanceof Phi)) break;
                    }
                }

                // 3. 准备回溯：先存下压栈次数，并在栈中压入“退出”标记
                blockPushCounts.put(currentBlock, pushCount);
                workStack.push(new SimpleEntry<>(currentBlock, true));

                // 4. 准备递归子树
                HashSet<BasicBlock> children = currentBlock.getDirectDominatedpersons();
                for (BasicBlock child : children) {
                    workStack.push(new SimpleEntry<>(child, false));
                }

            } else {
                // === 【回溯阶段】 ===
                // 当从栈中弹出 isExit 为 true 的项时，说明该块及其子树已全部处理完
                int count = blockPushCounts.getOrDefault(currentBlock, 0);
                for (int i = 0; i < count; i++) {
                    this.stack.pop();
                }
                // 处理完后移除记录，节省空间
                blockPushCounts.remove(currentBlock);
            }
        }
    }

    public void findPromotableAllocas(Function function) {
        promotableAllocas.clear();
        BasicBlock firstBlock = function.getFirstBlock();
        for (Instruction instruction : firstBlock.getInstructions()) {
            if (instruction instanceof Alloca alloca) {
                if (((PointerType) alloca.getValueType()).getPointedType() instanceof IntType) {
                    // 必须是指向int类型的alloca才行，指向数组的不行
                    promotableAllocas.add(alloca);
                }
            }
        }
    }

    public void delteUselessStoreLoad(Function function) {
        for (BasicBlock curBlock : function.getBlocks()) {
            HashMap<Alloca, Store> alloca2Store = new HashMap<>();
            Iterator<Instruction> it = curBlock.getInstructions().iterator();
            while (it.hasNext()) {
                Instruction instruction = it.next();
                if (instruction instanceof Store store && store.getPointer() instanceof Alloca alloca) {
                    alloca2Store.put(alloca, store);
                } else if (instruction instanceof Load load && load.getPointer() instanceof Alloca alloca) {
                    // %a = alloca i32                   ; 变量 a 的地址
                    // store i32 10, i32* %a             ; (1) 把 10 存入 a
                    // %val1 = load i32, i32* %a         ; (2) 从 a 读取值到 %val1
                    // %res = add i32 %val1, 5           ; (3) 计算 %val1 + 5
                    // store i32 20, i32* %a             ; (4) 把 20 存入 a
                    // %val2 = load i32, i32* %a         ; (5) 从 a 读取值到 %val2
                    // ret i32 %val2                     ; (6) 返回 %val2
                    if (alloca2Store.containsKey(alloca)) {
                        Store store = alloca2Store.getOrDefault(alloca, null);
                        Value newValue = store.getStoredValue();
                        // 1. 先拷贝一份 user 列表，防止 removeUser 时导致遍历崩溃
                        ArrayList<User> usersCopy = new ArrayList<>(load.getUsers());

                        for (User user : usersCopy) {
                            ArrayList<Value> operands = user.getOperands();
                            for (int i = 0; i < operands.size(); i++) {
                                if (operands.get(i) == load) {
                                    // 2. 维护 Use-Def 链
                                    load.removeUser(user);      // 从旧值的用户列表中移除
                                    newValue.addUser(user);     // 加入新值的用户列表

                                    // 3. 真正替换操作数
                                    operands.set(i, newValue);  // 将操作数指向新值
                                }
                            }
                        }
                        instruction.deleteAllUsageRelationship();
                        // 删除这个load指令
                        System.out.println("删掉了没用的: " + instruction);
                        it.remove();
                    }
                }
            }

            // 然后开始删除没有用的store指令
            // 获取 List 引用
            List<Instruction> instrList = curBlock.getInstructions();
            // 将迭代器初始化在列表的最末尾
            ListIterator<Instruction> iter = instrList.listIterator(instrList.size());
            alloca2Store.clear();
            // 开始逆序遍历
            while (iter.hasPrevious()) {
                Instruction instruction = iter.previous();
                if (instruction instanceof Store store && store.getPointer() instanceof Alloca alloca) {
                    if (!alloca2Store.containsKey(alloca)) alloca2Store.put(alloca, store);
                    else {
                        // 如果已经有了，说明这条指令需要被删除
                        instruction.deleteAllUsageRelationship();
                        // System.out.println("逆序删掉了没有用的: " + instruction);
                        iter.remove();
                    }
                }
            }
        }

    }

    public void deleteUselessAlloca(Alloca alloca) {
        // 1. 这里的 users 也建议用副本，防止遍历 Load 时产生意外修改
        ArrayList<User> usersCopy = new ArrayList<>(alloca.getUsers());

        for (User user : usersCopy) {
            if (user instanceof Load load) {
                // 核心修正点：创建 Load 使用者的副本，防止 ConcurrentModificationException
                ArrayList<User> usersOfLoadCopy = new ArrayList<>(load.getUsers());
                // 所有使用这条load指令的对象
                for (User userOfLoad : usersOfLoadCopy) {
                    ArrayList<Value> operands = userOfLoad.getOperands();
                    // 拿到其中一个对象，遍历这个对象的operands（必然存在某个operand使用了load）
                    for (int i = 0; i < operands.size(); i++) {
                        if (operands.get(i) == load) {
                            // 2. 维护 Use-Def 链
                            load.removeUser(userOfLoad);
                            IrMaker.C0.addUser(userOfLoad);
                            // 3. 真正替换操作数
                            operands.set(i, IrMaker.C0);
                        }
                    }
                }
                // 替换完所有使用后，通常建议把这个 load 指令也从 IR 中删掉
                load.deleteAllUsageRelationship();
                System.out.println("删掉了没用的: " + load);
                ((BasicBlock) load.getParent()).getInstructions().remove(load);
            }
        }
        alloca.deleteAllUsageRelationship();
        ((BasicBlock) alloca.getParent()).getInstructions().remove(alloca);
    }
}
