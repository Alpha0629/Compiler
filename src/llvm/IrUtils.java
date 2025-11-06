package llvm;

import llvm.values.Function;

/**
 * {@code @Description} Ir工具类, 用于生成一系列value
 */
public class IrUtils {
    public static Function genFunction() {
        return new Function();
    }
}
