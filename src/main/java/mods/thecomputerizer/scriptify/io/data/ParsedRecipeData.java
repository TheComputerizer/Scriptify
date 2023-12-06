package mods.thecomputerizer.scriptify.io.data;

import mods.thecomputerizer.scriptify.io.IOUtils;
import mods.thecomputerizer.scriptify.mixin.access.*;
import stanhebben.zenscript.expression.*;
import stanhebben.zenscript.expression.partial.IPartialExpression;
import stanhebben.zenscript.parser.expression.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ParsedRecipeData {

    private final RecipeBlueprint blueprint;
    private final List<Object> args;

    public ParsedRecipeData(RecipeBlueprint blueprint, List<ParsedExpression> args) throws IllegalArgumentException {
        this.blueprint = blueprint;
        this.args = new ArrayList<>();
        for(ParsedExpression expression : args)
            parseArg(this.args,expression);
        if(!blueprint.verifyArgs(this.args))
            throw new IllegalArgumentException("Parsed recipe data failed blueprint verification!");
    }

    private void parseArg(List<Object> args, ParsedExpression expression) {
        if(expression instanceof ParsedExpressionValue)
            parsePartial(args,((ParsedExpressionValueAccessor)expression).getValue());
        else if(expression instanceof ParsedExpressionArray)
            parseArray(args,(ParsedExpressionArray)expression);
        else if(expression instanceof ParsedExpressionMap)
            parseMap(args,(ParsedExpressionMap)expression);
        else if(expression instanceof ParsedExpressionCast)
            parseCast(args,(ParsedExpressionCast)expression);
        else if(expression instanceof ParsedExpressionBinary)
            parseOperation(args,(ParsedExpressionBinary)expression);
    }

    private void parseArray(List<Object> args, ParsedExpressionArray array) {
        List<Object> elements = new ArrayList<>();
        for(ParsedExpression expression : ((ParsedExpressionArrayAccessor)array).getContents())
            parseArg(elements,expression);
        args.add(elements);
    }

    private void parseOperation(List<Object> args, ParsedExpressionBinary cast) {
        ParsedExpressionBinaryAccessor access = (ParsedExpressionBinaryAccessor)cast;
    }

    private void parseCast(List<Object> args, ParsedExpressionCast cast) {
        ParsedExpressionCastAccessor access = (ParsedExpressionCastAccessor)cast;
    }

    private void parseMap(List<Object> args, ParsedExpressionMap map) {
        Map<Object,Object> objMap = new HashMap<>();
        List<Object> keys = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        ParsedExpressionMapAccessor access = (ParsedExpressionMapAccessor)map;
        for(ParsedExpression expression : access.getKeys())
            parseArg(keys,expression);
        for(ParsedExpression expression : access.getValues())
            parseArg(values,expression);
        for(int i=0; i<keys.size(); i++) objMap.put(keys.get(i),values.get(i));
        args.add(objMap);
    }

    private void parsePartial(List<Object> args, IPartialExpression partial) {
        if(partial instanceof ExpressionBool)
            args.add(((ExpressionBoolAccessor)partial).getValue());
        else if(partial instanceof ExpressionFloat)
            args.add(((ExpressionFloatAccessor)partial).getValue());
        else if(partial instanceof ExpressionInt)
            args.add(((ExpressionIntAccessor)partial).getValue());
        else if(partial instanceof ExpressionString)
            args.add(((ExpressionStringAccessor)partial).getValue());
        else if(partial instanceof ExpressionCallStatic)
            parseBEP(args,(ExpressionCallStatic)partial);
    }

    private void parseBEP(List<Object> args, ExpressionCallStatic expression) {
        String type = expression.getType().getName().toLowerCase();
        Function<Object,String> writer = IOUtils.getWriterFunc(type);
        BEP bep = IOUtils.parseBEP(writer.apply(((ExpressionCallStaticAccessor)expression).getArguments()));
        if(type.contains("item")) args.add(bep.asItem());
        else if(type.contains("liquid")) args.add(bep.asFluid());
        else if(type.contains("ore")) args.add(bep.asOreEntries());
        else args.add(bep.toString());
    }
}
