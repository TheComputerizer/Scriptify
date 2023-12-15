package mods.thecomputerizer.scriptify.io.data;

import crafttweaker.mc1120.CraftTweaker;
import crafttweaker.zenscript.GlobalRegistry;
import mods.thecomputerizer.scriptify.Scriptify;
import mods.thecomputerizer.scriptify.ScriptifyRef;
import mods.thecomputerizer.scriptify.io.IOUtils;
import mods.thecomputerizer.scriptify.io.read.ZenFileReader;
import mods.thecomputerizer.scriptify.mixin.access.*;
import mods.thecomputerizer.scriptify.util.Misc;
import mods.thecomputerizer.scriptify.util.iterator.Wrapperable;
import mods.thecomputerizer.scriptify.util.iterator.WrapperableMappable;
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

    private static final WrapperableMappable<String,Blueprint> BLUEPRINTS_BY_MOD = new WrapperableMappable<>(new HashMap<>(),false);
    private static final Wrapperable<String> CACHED_BLUEPRINT_TYPES = Wrapperable.make(HashSet::new);

    public static void addBluePrint(String modid, Blueprint blueprint) {
        if(BLUEPRINTS_BY_MOD.addIfUnmatched(modid,Wrapperable.make(HashSet::new),blueprint))
            ScriptifyRef.LOGGER.info("Successfully added expression blueprint {} for mod {}",blueprint,modid);
    }

    public static Set<Blueprint> getPotentialBlueprints(String className, String methodName) {
        Set<Blueprint> matches = BLUEPRINTS_BY_MOD.insertMatchingValues(HashSet::new,
                blueprint -> blueprint.matches(className,methodName));
        if(matches.isEmpty())
            Scriptify.logError(ExpressionDataHandler.class, "get", null, className, methodName);
        return matches;
    }

    public static List<String> getRecipeTypes(String prefix, String arg) {
        Set<String> combined = CACHED_BLUEPRINT_TYPES.mapTo(HashSet::new,
                type -> type.startsWith(arg) ? prefix+"="+type : null,true);
        return new ArrayList<>(combined);
    }

    public static @Nullable ExpressionData matchFilteredExpression(
            ZenFileReader reader, StatementExpression statement, Collection<String> classMatches,
            Collection<String> methodMatches) throws IllegalArgumentException {
        ParsedExpression expression = ((StatementExpressionAccessor)statement).getExpression();
        if(expression instanceof ParsedExpressionCall) {
            ParsedExpressionCallAccessor access = (ParsedExpressionCallAccessor)expression;
            Set<Blueprint> matches = matchMember((ParsedExpressionMember)access.getReceiver(),classMatches,methodMatches);
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
                                             Collection<String> methodMatches) {
        ParsedExpressionMemberAccessor access = (ParsedExpressionMemberAccessor)member;
        String methodName = access.getMember();
        if(!methodMatches.isEmpty() && !methodMatches.contains(methodName)) return new HashSet<>();
        String className = ((ParsedExpressionVariableAccessor)access.getValue()).getName();
        if(!classMatches.isEmpty() && !classMatches.contains(className)) return new HashSet<>();
        return getPotentialBlueprints(className,methodName);
    }

    /**
     * Goes through the CT registry and catalogues all the static methods the classes
     */
    public static void registerBlueprints() {
        BLUEPRINTS_BY_MOD.clear();
        CACHED_BLUEPRINT_TYPES.clear();
        registerZenClassAliases("",GlobalRegistry.getRoot().getPackages());
        registerBlueprints("",GlobalRegistry.getGlobals());
        registerBlueprints("",GlobalRegistry.getRoot().getPackages());
        BLUEPRINTS_BY_MOD.forEachValue(wrappable -> wrappable.mapTo(CACHED_BLUEPRINT_TYPES,
                Blueprint::getTypeName, false,true));
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
                String returnName = method.getReturnType().getName();
                String[] parameterTypes = new String[method.getParameterTypes().length];
                Misc.supplyArray(parameterTypes,i -> IOUtils.getBaseTypeName(method.getParameterTypes()[i]));
                Blueprint blueprint = new Blueprint(false,className,methodName,returnName,parameterTypes);
                addBluePrint(blueprint.getMod(),blueprint);
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
                    String name = parameter.getType().getSimpleName();
                    return (Objects.nonNull(parameter.getAnnotation(Optional.class)) ? "@" : "")+name;
                });
                addBluePrint(CraftTweaker.MODID,new Blueprint(true,className,method.getName(),returnType,parameterTypes));
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
