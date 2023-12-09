package mods.thecomputerizer.scriptify.io.data;

import lombok.Getter;
import mods.thecomputerizer.scriptify.io.read.ExpressionReader;
import mods.thecomputerizer.scriptify.io.write.FileWriter;
import stanhebben.zenscript.compiler.IEnvironmentGlobal;
import stanhebben.zenscript.parser.expression.*;

import java.util.*;

@Getter
public class ParsedRecipeData {

    private final RecipeBlueprint blueprint;
    private final IEnvironmentGlobal environment;
    private final List<ExpressionReader> readers;

    public ParsedRecipeData(RecipeBlueprint blueprint, IEnvironmentGlobal environment,
                            List<ParsedExpression> expressions) throws IllegalArgumentException {
        this.blueprint = blueprint;
        this.environment = environment;
        this.readers = getReaders(expressions);
        if(!blueprint.verifyArgs(this.readers))
            throw new IllegalArgumentException("Parsed recipe data failed blueprint verification!");
    }

    private List<ExpressionReader> getReaders(List<ParsedExpression> expressions) {
        List<ExpressionReader> readers = new ArrayList<>();
        for(ParsedExpression expression : expressions)
            readers.add(new ExpressionReader(expression,this.environment));
        return readers;
    }

    public FileWriter makeWriter() {
        return this.blueprint.makeWriter(this.readers);
    }
}
