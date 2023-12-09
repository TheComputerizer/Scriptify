package mods.thecomputerizer.scriptify.io.read;

import lombok.Getter;
import mods.thecomputerizer.scriptify.ScriptifyRef;
import mods.thecomputerizer.scriptify.mixin.access.*;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;
import stanhebben.zenscript.compiler.IEnvironmentGlobal;
import stanhebben.zenscript.expression.*;
import stanhebben.zenscript.expression.partial.IPartialExpression;
import stanhebben.zenscript.parser.expression.*;
import stanhebben.zenscript.type.ZenType;
import stanhebben.zenscript.type.ZenTypeArrayBasic;
import stanhebben.zenscript.type.ZenTypeAssociative;

import java.util.ArrayList;
import java.util.List;

public class ExpressionReader implements FileReader<String> {

    @Getter private final IEnvironmentGlobal environment;
    @Getter private final Expression expression;
    private ZenType cachedType;

    public ExpressionReader(ParsedExpression parsed, IEnvironmentGlobal environment) {
        this.environment = environment;
        this.expression = getExpression(parsed);
    }

    public ExpressionReader(Expression expression, IEnvironmentGlobal environment) {
        this.environment = environment;
        this.expression = expression;
    }

    @Override
    public void copy(List<String> lines) {}

    private Expression getExpression(ParsedExpression expression) {
        return testPartial(getPartial(expression));
    }

    private IPartialExpression getPartial(ParsedExpression expression) {
        if(expression instanceof ParsedExpressionArray) return getArrayExpression((ParsedExpressionArray)expression);
        if(expression instanceof ParsedExpressionBinary) return getBinaryExpression((ParsedExpressionBinary)expression);
        if(expression instanceof ParsedExpressionCall) return getCallExpression((ParsedExpressionCall)expression);
        if(expression instanceof ParsedExpressionCast) return getCastExpression((ParsedExpressionCast)expression);
        if(expression instanceof ParsedExpressionMap) return getMapExpression((ParsedExpressionMap)expression);
        if(expression instanceof ParsedExpressionMember) return getMemberExpression((ParsedExpressionMember)expression);
        if(expression instanceof ParsedExpressionValue) return ((ParsedExpressionValueAccessor)expression).getValue();
        return makeNullExpression();
    }

    private Expression getArrayExpression(ParsedExpressionArray array) {
        ParsedExpressionArrayAccessor access = (ParsedExpressionArrayAccessor)array;
        ZenTypeArrayBasic arrayType = ZenType.ANYARRAY;
        Expression[] contents = getExpressionArray(access.getContents());
        if(contents.length>0) arrayType = new ZenTypeArrayBasic(contents[0].getType());
        return new ExpressionArray(array.getPosition(),arrayType,getExpressionArray(access.getContents()));
    }

    private Expression getBinaryExpression(ParsedExpressionBinary binary) {
        ParsedExpressionBinaryAccessor access = (ParsedExpressionBinaryAccessor)binary;
        Expression left = getExpression(access.getLeft());
        Expression right = getExpression(access.getRight());
        ZenType type = left.getType();
        return type.binary(binary.getPosition(),this.environment,left,right,access.getOperator());
    }

    private Expression getCallExpression(ParsedExpressionCall call) {
        ParsedExpressionCallAccessor access = (ParsedExpressionCallAccessor)call;
        Expression receiver = getExpression(access.getReceiver());
        ZenType type = receiver.getType();
        return type.call(call.getPosition(),this.environment,receiver,getExpressionArray(access.getArguments()));
    }

    private Expression getCastExpression(ParsedExpressionCast cast) {
        ParsedExpressionCastAccessor access = (ParsedExpressionCastAccessor)cast;
        return getExpression(access.getValue()).cast(cast.getPosition(),this.environment,access.getType());
    }

    private Expression[] getExpressionArray(List<ParsedExpression> expressions) {
        return expressions.stream().map(this::getExpression).toArray(Expression[]::new);
    }

    private Expression getMapExpression(ParsedExpressionMap map) {
        ParsedExpressionMapAccessor access = (ParsedExpressionMapAccessor)map;
        Expression[] keys = getExpressionArray(access.getKeys());
        Expression[] values = getExpressionArray(access.getValues());
        ZenTypeAssociative mapType = ZenType.ANYMAP;
        if(keys.length>0 && values.length>0) mapType = new ZenTypeAssociative(values[0].getType(),keys[0].getType());
        return new ExpressionMap(map.getPosition(),keys,values,mapType);
    }

    private Expression getMemberExpression(ParsedExpressionMember member) {
        ParsedExpressionMemberAccessor access = (ParsedExpressionMemberAccessor)member;
        return getExpression(access.getValue());
    }

    public ZenType getType() {
        return this.expression.getType();
    }

    private Expression makeNullExpression() {
        return new ExpressionString(null,"null");
    }

    private Expression testPartial(IPartialExpression partial) {
        if(partial instanceof Expression) return (Expression)partial;
        else ScriptifyRef.LOGGER.error("HUH? {}",partial.getType().getName());
        return makeNullExpression();
    }

    @Override
    public String toString() {
        List<String> lines = new ArrayList<>();
        copy(lines);
        return TextUtil.listToString(lines,"");
    }
}
