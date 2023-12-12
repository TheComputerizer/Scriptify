package mods.thecomputerizer.scriptify.io.data;

import crafttweaker.zenscript.GlobalRegistry;
import mods.thecomputerizer.scriptify.Scriptify;
import mods.thecomputerizer.scriptify.ScriptifyRef;
import mods.thecomputerizer.scriptify.io.IOUtils;
import mods.thecomputerizer.scriptify.io.read.ZenFileReader;
import mods.thecomputerizer.scriptify.mixin.access.*;
import mods.thecomputerizer.scriptify.util.Misc;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;
import org.apache.commons.lang3.StringUtils;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.parser.expression.ParsedExpression;
import stanhebben.zenscript.parser.expression.ParsedExpressionCall;
import stanhebben.zenscript.parser.expression.ParsedExpressionMember;
import stanhebben.zenscript.statements.StatementExpression;
import stanhebben.zenscript.symbols.IZenSymbol;
import stanhebben.zenscript.symbols.SymbolJavaStaticField;
import stanhebben.zenscript.symbols.SymbolPackage;
import stanhebben.zenscript.symbols.SymbolType;
import stanhebben.zenscript.type.ZenTypeNative;
import stanhebben.zenscript.type.natives.IJavaMethod;
import stanhebben.zenscript.type.natives.ZenNativeMember;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

@SuppressWarnings("SpellCheckingInspection")
public class ExpressionDataHandler {

    private static final List<Blueprint> BLUEPRINT_LIST = new ArrayList<>();

    public static void addBluePrint(Blueprint blueprint) {
        if(!matchesExisting(blueprint)) {
            BLUEPRINT_LIST.add(blueprint);
            ScriptifyRef.LOGGER.info("Successfully added expression blueprint {}",blueprint);
        }
    }

    public static Set<Blueprint> getPotentialBlueprints(String className, String methodName, boolean allowUnknownBlueprint) {
        Set<Blueprint> matches = new HashSet<>();
        for(Blueprint blueprint : BLUEPRINT_LIST)
            if(blueprint.matches(className,methodName))
                matches.add(blueprint);
        if(matches.isEmpty())
            Scriptify.logError(ExpressionDataHandler.class, "get", null, className, methodName);
        return matches;
    }

    public static boolean isGlobalClass(String className) {
        return className.matches("recipes") || className.matches("furnace");
    }

    public static @Nullable ExpressionData matchFilteredExpression(
            ZenFileReader reader, StatementExpression statement, Collection<String> classMatches,
            Collection<String> methodMatches, boolean allowUnknownBlueprint) throws IllegalArgumentException {
        ParsedExpression expression = ((StatementExpressionAccessor)statement).getExpression();
        if(expression instanceof ParsedExpressionCall) {
            ParsedExpressionCallAccessor access = (ParsedExpressionCallAccessor)expression;
            Set<Blueprint> matches = matchMember((ParsedExpressionMember)access.getReceiver(),classMatches,methodMatches,allowUnknownBlueprint);
            if(!matches.isEmpty()) {
                Scriptify.logInfo(ExpressionDataHandler.class,"match",matches.size());
                return new ExpressionData(matches,reader.getEnvironment(),access.getArguments());
            } else if(!classMatches.isEmpty() || !methodMatches.isEmpty()) {
                if(classMatches.isEmpty())
                    Scriptify.logError(ExpressionDataHandler.class,"method",null,TextUtil.compileCollection(methodMatches));
                else Scriptify.logError(ExpressionDataHandler.class,"class",null,TextUtil.compileCollection(classMatches));
            }
        } else Scriptify.logError(ExpressionDataHandler.class,"match",null,expression.getClass().getName());
        return null;
    }

    public static Set<Blueprint> matchMember(ParsedExpressionMember member, Collection<String> classMatches,
                                             Collection<String> methodMatches, boolean allowUnknownBlueprint) {
        ParsedExpressionMemberAccessor access = (ParsedExpressionMemberAccessor)member;
        String methodName = access.getMember();
        if(!methodMatches.isEmpty() && !methodMatches.contains(methodName)) return new HashSet<>();
        String className = ((ParsedExpressionVariableAccessor)access.getValue()).getName();
        if(!classMatches.isEmpty() && !classMatches.contains(className)) return new HashSet<>();
        return getPotentialBlueprints(className,methodName,allowUnknownBlueprint);
    }

    public static boolean matchesExisting(Blueprint newBlueprint) {
        for(Blueprint blueprint : BLUEPRINT_LIST)
            if(blueprint==newBlueprint) return true;
        return false;
    }

    /**
     * Goes through the CT registry and catalogues all the static methods the classes
     */
    public static void registerBlueprints() {
        BLUEPRINT_LIST.clear();
        registerZenClassAliases("",GlobalRegistry.getRoot().getPackages());
        registerBlueprints("",GlobalRegistry.getGlobals());
        registerBlueprints("",GlobalRegistry.getRoot().getPackages());
    }

    private static void registerBlueprints(String previous, Map<String,IZenSymbol> symbolMap) {
        for(Map.Entry<String,IZenSymbol> symbolEntry : symbolMap.entrySet()) {
            String id = (StringUtils.isNotBlank(previous) ? previous+"." : "")+symbolEntry.getKey();
            IZenSymbol symbol = symbolEntry.getValue();
            if(symbol instanceof SymbolPackage) registerBlueprints(id,((SymbolPackage)symbol).getPackages());
            else {
                if(symbol instanceof SymbolType) {
                    SymbolType type = (SymbolType)symbol;
                    if(type.getType() instanceof ZenTypeNative) registerBlueprints((ZenTypeNative)type.getType());
                } else if(symbol instanceof SymbolJavaStaticField)
                    registerBlueprints(id,(SymbolJavaStaticFieldAccessor)symbol);
                else ScriptifyRef.LOGGER.debug("Skipping unknown symbol type with ID {}",id);
            }
        }
    }

    private static void registerBlueprints(ZenTypeNative type) {
        for(Map.Entry<String, ZenNativeMember> staticEntry : type.getStaticMembers().entrySet()) {
            String className = IOUtils.getBaseTypeName(type);
            String methodName = staticEntry.getKey();
            for(IJavaMethod method : staticEntry.getValue().getMethods()) {
                String[] parameterTypes = new String[method.getParameterTypes().length];
                Misc.supplyArray(parameterTypes,i -> IOUtils.getBaseTypeName(method.getParameterTypes()[i]));
                addBluePrint(new Blueprint(className,methodName,method.getReturnType().getName(),parameterTypes));
            }
        }
    }

    private static void registerBlueprints(String className, SymbolJavaStaticFieldAccessor access) {
        Object obj = Misc.getFieldInstance(access.getField());
        if(Objects.nonNull(obj)) {
            for(Method method : obj.getClass().getDeclaredMethods()) {
                String returnType = method.getReturnType().getSimpleName();
                String[] parameterTypes = new String[method.getParameters().length];
                Misc.supplyArray(parameterTypes,i -> {
                    Parameter parameter = method.getParameters()[i];
                    return (Objects.nonNull(parameter.getAnnotation(Optional.class)) ? "@" : "")+parameter.getType().getSimpleName();
                });
                addBluePrint(new Blueprint(className,method.getName(),returnType,parameterTypes));
            }
        }
    }

    private static void registerZenClassAliases(String previous, Map<String,IZenSymbol> symbolMap) {
        List<SymbolType> classSymbols = new ArrayList<>();
        for(Map.Entry<String,IZenSymbol> symbolEntry : symbolMap.entrySet()) {
            String id = (StringUtils.isNotBlank(previous) ? previous+"." : "")+symbolEntry.getKey();
            IZenSymbol symbol = symbolEntry.getValue();
            if(symbol instanceof SymbolPackage) registerZenClassAliases(id,((SymbolPackage)symbol).getPackages());
            else if(symbol instanceof SymbolType && ((SymbolType)symbol).getType() instanceof ZenTypeNative)
                classSymbols.add((SymbolType)symbol);
        }
        for(SymbolType type : classSymbols)
            IOUtils.addClassAliases(type.getType().toJavaClass());
    }
}
