package optimization;

import backend.MipsModule;
import llvm.IrMaker;
import llvm.IrModule;
import llvm.IrUtils;
import llvm.types.LabelType;
import llvm.types.ValueType;
import llvm.types.VoidType;
import llvm.values.BasicBlock;
import llvm.values.Function;
import llvm.values.Value;
import llvm.values.instructions.Branch;
import llvm.values.instructions.Copy;
import llvm.values.instructions.Instruction;
import llvm.values.instructions.Phi;
import llvm.values.instructions.Ret;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

public class RemovePhi {
    private final IrModule irModule;
    // private final MipsModule mipsModule;
    private final HashMap<BasicBlock, ArrayList<Copy>> precursor2Copys;
    private static int midCounter = 0;
    private static int midBlockCounter = 0;

    public RemovePhi(IrModule irModule, MipsModule mipsModule) {
        this.irModule = irModule;
        this.precursor2Copys = new HashMap<>();
    }

    public void pass() {
        ArrayList<Function> functions = irModule.getFunctions();
        for (Function function : functions) {
            ArrayList<BasicBlock> blocksSnapshot = new ArrayList<>(function.getBlocks());
            for (BasicBlock basicBlock : blocksSnapshot) {
                this.precursor2Copys.clear();
                transferPhi(basicBlock);
                insertCopy(basicBlock);
            }
        }
    }

    public void insertCopy(BasicBlock curBlock) {
        for (HashMap.Entry<BasicBlock, ArrayList<Copy>> entry : this.precursor2Copys.entrySet()) {
            BasicBlock precursor = entry.getKey();  // 当前前驱块
            ArrayList<Copy> copys = entry.getValue();   // 这个前驱块所需要插入的copy
            // 得到解开冲突后的顺序序列
            LinkedList<Copy> resultCopys = dealParallel(copys);
            if (resultCopys.isEmpty()) continue;

            int successorCount = precursor.getSuccessors().size();
            if (successorCount == 1) {
                // 只有一条路，直接插在末尾
                insertIntoBlockEnd(precursor, resultCopys);
            } else if (successorCount > 1) {
                // 临界边，必须拆分
                splitEdgeAndInsert(precursor, curBlock, resultCopys);
            } else {
                // successorCount == 0
                throw new RuntimeException("Error: Precursor has no successors but is linked to " + curBlock.getName());
            }
        }
    }

    public void insertIntoBlockEnd(BasicBlock precursor, LinkedList<Copy> resultCopys) {
        if (resultCopys == null || resultCopys.isEmpty()) {
            return;
        }
        for (Copy cp : resultCopys) {
            cp.resetParent(precursor); // 必须让指令知道自己现在属于前驱块
        }
        ArrayList<Instruction> instructions = precursor.getInstructions();
        Instruction lastInstr = precursor.getLastInstruction();

        if (lastInstr instanceof Branch || lastInstr instanceof Ret) {
            // 1. 找到终结符的位置
            int index = instructions.size() - 1;
            // 2. 在这个位置插入所有的 Copy
            instructions.addAll(index, resultCopys);
        } else {
            // 兜底逻辑：直接加到末尾
            instructions.addAll(resultCopys);
        }
    }

    public void splitEdgeAndInsert(BasicBlock precursor, BasicBlock curBlock, LinkedList<Copy> resultCopys) {
        // 1. 创建中间块，并关联到当前函数
        Function func = curBlock.getParentFunction();
        BasicBlock middleBlock = new BasicBlock("splitEdge" + (midBlockCounter++), new LabelType(), func);
        // 2. 将 Copy 指令序列加入中间块
        middleBlock.addInstructionsToTail(new ArrayList<>(resultCopys));
        for (Copy cp : resultCopys) {
            cp.resetParent(middleBlock);    // 从指向原来的块变成指向现在的新块
        }

        // 3. 中间块最后必须有一个无条件跳转指向目标块（curBlock）
        Branch branch = new Branch(new VoidType(), middleBlock, curBlock);
        middleBlock.addInstructionToTail(branch);

        // 4. 修改前驱块（precursor）的跳转指令
        // 找到跳转到 curBlock 的那条指令，并把目标换成 middleBlock
        Instruction oldJump = precursor.getLastInstruction();

        ArrayList<Value> operands = new ArrayList<>();
        for (Value operand : oldJump.getOperands()) {
            if (operand == curBlock) {
                operands.add(middleBlock);
                curBlock.removeUser(oldJump);
                middleBlock.addUser(oldJump);
            } else {
                operands.add(operand);
            }
        }
        oldJump.resetOperands(operands);

        // 5. 维护 CFG 结构
        // precursor 原本指向 curBlock，现在指向 middleBlock
        precursor.getSuccessors().remove(curBlock);
        precursor.addSuccessor(middleBlock);

        // middleBlock 指向 curBlock
        middleBlock.addPrecursor(precursor);
        middleBlock.addSuccessor(curBlock);

        // curBlock 的前驱原本有 precursor，现在换成 middleBlock
        curBlock.getPrecursors().remove(precursor);
        curBlock.addPrecursor(middleBlock);

        // 6. 将新块物理插入到函数的基本块列表中（建议插在 precursor 后面）
        int index;
        for (index = 0; index < func.getBlocks().size(); index++) {
            if (func.getBlocks().get(index) == precursor) {
                break;
            }
        }
        func.getBlocks().add(index + 1, middleBlock);
    }

    public LinkedList<Copy> dealParallel(ArrayList<Copy> copys) {
        // 1. 使用 LinkedList 方便在头部插入备份指令
        LinkedList<Copy> resultList = new LinkedList<>();
        // 2. 遍历输入的每一条 Copy
        for (int i = 0; i < copys.size(); i++) {
            Copy firstCopy = copys.get(i);
            Value phiNode = firstCopy.getPhiNode(); // 目的地 (a)
            // 3. 检查后续指令中是否有人需要读取当前的 phiNode (a)
            for (int j = i + 1; j < copys.size(); j++) {
                Copy secondCopy = copys.get(j);
                // 发现 Read-After-Write 冲突：我要改写 a，但后面有人要读旧的 a
                if (phiNode == secondCopy.getValue()) {
                    System.out.println(1111);
                    // 构造备份指令：mid = a
                    ValueType valueType = phiNode.getValueType();
                    Value tempValue = new Value("mid" + (midCounter++), valueType, firstCopy.getParent());
                    Copy temp = IrUtils.makeCopy(tempValue, phiNode, (BasicBlock) firstCopy.getParent());
                    // 核心：备份必须发生在所有改写之前，直接插到最前面
                    resultList.addFirst(temp);
                    // 4. 更新后续所有引用了旧 a 的地方，让他们改读 mid
                    for (int k = j; k < copys.size(); k++) {
                        Copy thirdCopy = copys.get(k);
                        if (thirdCopy.getValue() == phiNode) {
                            thirdCopy.resetValue(tempValue);
                        }
                    }
                    // 找到一个冲突并处理后，对于这条 firstCopy 来说 target 已经安全备份了
                    break;
                }
            }
            // 5. 将当前的 Copy 指令按序加入结果集
            resultList.addLast(firstCopy);
        }
        return resultList;
    }

    public void transferPhi(BasicBlock curBlock) {
        ArrayList<Instruction> patternInstrs = curBlock.getInstructions();
        Iterator<Instruction> instIter = patternInstrs.iterator();

        while (instIter.hasNext()) {
            Instruction instruction = instIter.next();
            if (instruction instanceof Phi phi) {
                // 获取清洗后的、安全的映射关系
                HashMap<BasicBlock, Value> cleanedMap = getCleanedSourceMap(curBlock, phi);
                for (HashMap.Entry<BasicBlock, Value> entry : cleanedMap.entrySet()) {
                    if (entry.getValue() == IrMaker.CInf) continue; // 占位符直接跳过即可
                    Copy copy = IrUtils.makeCopy(phi, entry.getValue(), entry.getKey());
                    this.precursor2Copys.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(copy);
                }
                // 使用迭代器安全移除
                // phi.deleteAllUsageRelationship();
                instIter.remove();
            }
        }
    }

    private HashMap<BasicBlock, Value> getCleanedSourceMap(BasicBlock curBlock, Phi phi) {
        HashMap<BasicBlock, Value> sourceMap = new HashMap<>(phi.getSourceBlock2sourceValue());
        HashSet<BasicBlock> precursors = curBlock.getPrecursors();
        // 如果 key（来源块）不再是当前块的前驱，则剔除
        sourceMap.keySet().removeIf(sourceBlock -> !precursors.contains(sourceBlock));
        return sourceMap;
    }
}
