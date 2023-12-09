package mods.thecomputerizer.scriptify.io.read;

import lombok.Getter;
import mods.thecomputerizer.scriptify.mixin.access.StatementExpressionAccessor;
import stanhebben.zenscript.compiler.IEnvironmentGlobal;
import stanhebben.zenscript.statements.Statement;
import stanhebben.zenscript.statements.StatementExpression;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Getter
public class StatementReader implements FileReader<String> {

    /**
     * Character width of a page before a line break. Soft cap.
     */
    private static final int CHAR_WIDTH = 80;

    private final IEnvironmentGlobal environment;
    private final Statement statement;
    private final List<StatementReader> subReaders;
    private final @Nullable ExpressionReader expressionReader;
    public StatementReader(IEnvironmentGlobal environment, Statement statement) {
        this.environment = environment;
        this.statement = statement;
        this.subReaders = new ArrayList<>();
        for(Statement sub : statement.getSubStatements()) this.subReaders.add(new StatementReader(environment,sub));
        this.expressionReader = this.statement instanceof StatementExpression ?
                new ExpressionReader(((StatementExpressionAccessor)this.statement).getExpression(),environment) : null;
    }

    public void copy(List<String> lines) {}
}
