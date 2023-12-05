package mods.thecomputerizer.scriptify.io.read;

import mods.thecomputerizer.scriptify.Scriptify;
import mods.thecomputerizer.scriptify.mixin.access.StatementExpressionAccessor;
import stanhebben.zenscript.statements.Statement;
import stanhebben.zenscript.statements.StatementExpression;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StatementReader implements IClampedStringReader<String> {

    //Character width of a page before a line break. Soft cap.
    private static final int CHAR_WIDTH = 80;

    private final Statement statement;
    private final Set<String> imports;
    public StatementReader(Statement statement) {
        this.statement = statement;
        this.imports = new HashSet<>();
    }

    @Override
    public String parse(String unparsed) {
        return null;
    }

    public void copy(List<String> lines) {
        Scriptify.logDebug("Statement position is {}",this.statement.getPosition());
        Scriptify.logDebug("Statement class is {}",this.statement.getClass().getName());
        List<String> unformatted = new ArrayList<>();
        if(this.statement instanceof StatementExpression) {
            StatementExpressionAccessor access = (StatementExpressionAccessor)this.statement;
            this.imports.addAll(ParsedExpressionReader.copy(unformatted,access.getExpression(),false));
        }
        StringBuilder builder = new StringBuilder();
        for(String line : unformatted) {
            if(builder.length()>=CHAR_WIDTH && line.length()>2) {
                lines.add(builder.toString());
                builder = new StringBuilder();
            }
            builder.append(line);
        }
        lines.add(builder.append(";").toString());
        lines.add("");
    }
}
