package optimization;

import backend.MipsModule;
import llvm.IrModule;
import llvm.values.BasicBlock;
import llvm.values.Function;
import llvm.values.instructions.Branch;
import llvm.values.instructions.Instruction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public class DominationAnalysis {
    private final IrModule irModule;
    // private final MipsModule mipsModule;

    public DominationAnalysis(IrModule irModule, MipsModule mipsModule) {
        this.irModule = irModule;
        // this.mipsModule = mipsModule;
    }

    public void pass() {
        // 先构建CFG图
        buildCFG();
        buildDominanceTree();

    }

    public void buildCFG() {
        // CFG用来表示block之间的前驱和后继关系，通过跳转指令构建
        ArrayList<Function> functions = irModule.getFunctions();
        for (Function function : functions) {
            if (function.isDeclare()) continue;
            BasicBlock firstBlock = function.getFirstBlock();
            DFS(firstBlock);
            deleteUnreachableBasicBlock(function);
        }
    }

    public void DFS(BasicBlock curBlock) {
        if (curBlock.isVisited()) return;
        curBlock.setVisited(true);
        // 经过死代码删除后，ret和branch语句只能出现在blcok的最后一句
        Instruction instr = curBlock.getLastInstruction();
        if (instr instanceof Branch branch) {
            if (branch.isConditionalJump()) {
                // 无条件跳转，说明只有一个目标block
                // 把当前块和trueBlock建立联系
                BasicBlock trueBlock = branch.getTrueBlock();
                curBlock.addSuccessor(trueBlock);
                trueBlock.addPrecursor(curBlock);
                DFS(trueBlock);
                // 把当前块和falseBlock建立联系
                BasicBlock falseBlock = branch.getFalseBlock();
                curBlock.addSuccessor(falseBlock);
                falseBlock.addPrecursor(curBlock);
                DFS(falseBlock);
            } else {
                BasicBlock destBlock = branch.getDest();
                curBlock.addSuccessor(destBlock);
                destBlock.addPrecursor(curBlock);
                DFS(destBlock);
            }
        }
    }

    public void deleteUnreachableBasicBlock(Function function) {
        BasicBlock firstBlock = function.getFirstBlock();
        ArrayList<BasicBlock> basicBlocks = function.getBlocks();
        // 使用迭代器安全删除
        Iterator<BasicBlock> iterator = basicBlocks.iterator();
        while (iterator.hasNext()) {
            BasicBlock curBlock = iterator.next();
            // 跳过入口块
            if (curBlock.equals(firstBlock)) {
                continue;
            }
            // 如果前驱为空，说明不可达
            if (curBlock.getPrecursors().isEmpty()) {
                // 清理指令间的引用关系（解偶 Use-Def 链）
                for (Instruction instr : curBlock.getInstructions()) {
                    instr.deleteAllUsageRelationship();
                }
                curBlock.getInstructions().clear();
                // 安全删除当前元素
                iterator.remove();
            }
        }
    }

    public void buildDominanceTree() {
        ArrayList<Function> functions = irModule.getFunctions();
        for (Function function : functions) {
            if (function.isDeclare()) continue;
            System.out.println("\nCurrent function is: " + function.getName());
            calSteadyDominators(function);
            findDirectDominators(function);
            calDominanceBoundary(function);
        }
    }

    public void calSteadyDominators(Function function) {
        ArrayList<BasicBlock> basicBlocks = function.getBlocks();
        // 初始化
        BasicBlock firstBlock = basicBlocks.get(0);
        firstBlock.addDominator(firstBlock);
        for (int i = 1; i < basicBlocks.size(); i++) {
            BasicBlock curBlock = basicBlocks.get(i);
            // 其他基本块，初始的支配者是这个函数的所有基本块
            for (BasicBlock dominator : basicBlocks) {
                curBlock.addDominator(dominator);
            }
        }

        boolean steady = false;
        while (!steady) {
            steady = true;
            for (int i = 1; i < basicBlocks.size(); i++) {
                BasicBlock curBlock = basicBlocks.get(i);
                HashSet<BasicBlock> newDoms = calIntersection(curBlock); // 计算交集
                newDoms.add(curBlock);   // 把自己也加进去
                HashSet<BasicBlock> oriDoms = curBlock.getDominators();  // 原本的
                if (oriDoms.size() != newDoms.size()) {
                    steady = false;
                } else {
                    for (BasicBlock dom : oriDoms) {
                        // 本质是不断缩小的过程
                        if (!newDoms.contains(dom)) {
                            steady = false;
                            break;
                        }
                    }
                }
                curBlock.setNewDominators(newDoms);
            }
        }

        // --- 新增：最后进行统一排序存储 ---
        // 假设 BasicBlock 已经根据录入顺序在 basicBlocks 列表中排好了
        for (BasicBlock bb : basicBlocks) {
            HashSet<BasicBlock> unsorted = bb.getDominators();

            // 将 HashSet 转换为按 basicBlocks 原始索引排序的 LinkedHashSet
            LinkedHashSet<BasicBlock> sortedDoms = basicBlocks.stream()
                    .filter(unsorted::contains)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            // 重新存入（此时顺序固定为 Entry -> ... -> 自生）
            bb.setNewDominators(sortedDoms);
        }
    }

    public HashSet<BasicBlock> calIntersection(BasicBlock curBlock) {
        HashSet<BasicBlock> precursors = curBlock.getPrecursors();
        HashSet<BasicBlock> intersection = new HashSet<>();
        boolean isFirst = true; // 专门的标记位
        for (BasicBlock precursor : precursors) {
            if (isFirst) {
                // 真正意义上的第一次：拷贝第一个集合
                intersection.addAll(precursor.getDominators());
                isFirst = false;
            } else {
                // 之后的每一次：严格执行取交集
                intersection.retainAll(precursor.getDominators());
            }
        }
        return intersection;
    }

    public void findDirectDominators(Function function) {
        ArrayList<BasicBlock> basicBlocks = function.getBlocks();
        for (BasicBlock curBlock : basicBlocks) {
            HashSet<BasicBlock> doms = curBlock.getDominators();
            for (BasicBlock dom1 : doms) {
                if (dom1 == curBlock) continue;
                boolean dom1IsDirectDominator = true;
                for (BasicBlock dom2 : doms) {
                    if (dom2 == curBlock || dom2 == dom1) continue;
                    // dom1和dom2都是curBlock的支配者
                    // 如果dom1也支配了dom2，则dom1不是直接支配者
                    if (dom2.getDominators().contains(dom1)) {
                        dom1IsDirectDominator = false;
                        break;
                    }
                }
                if (dom1IsDirectDominator) {
                    curBlock.setDirectDominator(dom1);
                    dom1.addDirectDominatedPerson(curBlock);
                    break;
                }
            }
        }
    }

    public void calDominanceBoundary(Function function) {
        ArrayList<BasicBlock> basicBlocks = function.getBlocks();
        for (BasicBlock curBlock : basicBlocks) {
            for (BasicBlock successor : curBlock.getSuccessors()) {
                BasicBlock mid = curBlock;
                while (mid == successor || !successor.getDominators().contains(mid)) {
                    // if (mid == successor) System.out.println(successor.getName());
                    mid.addDonminanceBoundary(successor);
                    mid = mid.getDirectDominator();
                }
            }
        }
    }
}
