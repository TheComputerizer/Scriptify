package mods.thecomputerizer.scriptify.io.data;

import lombok.Getter;
import mods.thecomputerizer.scriptify.io.read.ExpressionReader;
import mods.thecomputerizer.scriptify.io.write.FileWriter;
import mods.thecomputerizer.scriptify.io.write.ZenFileWriter;
import stanhebben.zenscript.compiler.IEnvironmentGlobal;
import stanhebben.zenscript.parser.expression.ParsedExpression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Getter
public class ExpressionData {

    private final Blueprint blueprint;
    private final IEnvironmentGlobal environment;
    private final List<ExpressionReader> readers;

    public ExpressionData(Collection<Blueprint> potentialBlueprints, IEnvironmentGlobal environment,
                          List<ParsedExpression> expressions) throws IllegalArgumentException {
        Blueprint matched = null;
        this.environment = environment;
        this.readers = getReaders(expressions);
        for(Blueprint potential : potentialBlueprints) {
            if(potential.verifyArgs(this.readers)) {
                matched = potential;
                break;
            }
        }
        if(Objects.isNull(matched)) throw new IllegalArgumentException("Parsed recipe data failed blueprint verification!");
        this.blueprint = matched;
    }

    private List<ExpressionReader> getReaders(List<ParsedExpression> expressions) {
        List<ExpressionReader> readers = new ArrayList<>();
        for(ParsedExpression expression : expressions)
            readers.add(new ExpressionReader(expression,this.environment));
        return readers;
    }

    public void finalizeWriter(ZenFileWriter writer) {
        if(this.blueprint.isReloadable()) writer.setPreProcessors("reloadable");
        String className = this.blueprint.getClassName();
        if(ExpressionDataHandler.isGlobalClass(className)) return;
        writer.setImports(className);
    }

    public FileWriter makeWriter() {
        return this.blueprint.makeWriter(this.readers);
    }
}
