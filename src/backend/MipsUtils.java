package backend;

import backend.MipsItem.MipsGV.MipsGlobalArray;
import backend.MipsItem.MipsGV.MipsGlobalInteger;
import backend.MipsItem.MipsGV.MipsGlobalString;
import backend.MipsItem.MipsGV.MipsZeroInitializer;
import llvm.values.GlobalString;
import llvm.values.GlobalVar;
import llvm.values.constants.ConstArray;
import llvm.values.constants.ConstInt;
import llvm.values.constants.Constant;

import java.util.ArrayList;

public class MipsUtils {
    private final MipsModule mipsModule;

    public MipsUtils(MipsModule mipsModule) {
        this.mipsModule = mipsModule;
    }

    public void makeMipsGlobalString(GlobalString globalString) {
        String name = globalString.getName();
        String content = globalString.getConstString().toString();
        MipsGlobalString mipsGlobalString = new MipsGlobalString(name, content);
        mipsModule.addMipsGlobalVariable(mipsGlobalString);
    }

    public void makeMipsZeroInitializer(GlobalVar globalVar) {
        String name = globalVar.getName();
        int length = ((ConstArray) globalVar.getConstInit()).getSize();
        MipsZeroInitializer zeroInitializer = new MipsZeroInitializer(name, length);
        mipsModule.addMipsGlobalVariable(zeroInitializer);
    }

    public void makeMipsGlobalArray(GlobalVar globalVar) {
        String name = globalVar.getName();
        ConstArray constArray = (ConstArray) globalVar.getConstInit();
        ArrayList<Integer> inits = new ArrayList<>();
        for (Constant constant : constArray.getConstants()) {
            assert constant instanceof ConstInt;
            inits.add(((ConstInt)constant).getVal());
        }
        MipsGlobalArray mipsGlobalArray = new MipsGlobalArray(name, inits);
        mipsModule.addMipsGlobalVariable(mipsGlobalArray);
    }

    public void makeMipsGlobalInteger(GlobalVar globalVar) {
        String name = globalVar.getName();
        int value = ((ConstInt) globalVar.getConstInit()).getVal();
        MipsGlobalInteger mipsGlobalInteger = new MipsGlobalInteger(name, value);
        mipsModule.addMipsGlobalVariable(mipsGlobalInteger);
    }
}
