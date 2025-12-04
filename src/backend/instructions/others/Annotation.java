package backend.instructions.others;

import backend.instructions.MipsInstruction;

public class Annotation extends MipsInstruction {
    private final String annotation;

    public Annotation(String annotation) {
        this.annotation = annotation;
    }

    @Override
    public String toString() {
        return "# " + annotation;
    }
}
