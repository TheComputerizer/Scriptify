package mods.thecomputerizer.scriptify.io.read;

import crafttweaker.zenscript.GlobalRegistry;
import mcp.MethodsReturnNonnullByDefault;
import stanhebben.zenscript.symbols.IZenSymbol;
import stanhebben.zenscript.symbols.SymbolPackage;
import stanhebben.zenscript.symbols.SymbolType;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("SameParameterValue")
@MethodsReturnNonnullByDefault
public abstract class FileReader {

    public abstract void copy(List<String> lines);

    /**
     * Casts the symbol to a package or returns null if it is invalid
     */
    protected @Nullable SymbolPackage getAsPackage(@Nullable IZenSymbol symbol) {
        return symbol instanceof SymbolPackage ? (SymbolPackage)symbol : null;
    }

    /**
     * Casts the symbol to a type or returns null if it is invalid
     */
    protected @Nullable SymbolType getAsType(@Nullable IZenSymbol symbol) {
        return symbol instanceof SymbolType ? (SymbolType)symbol : null;
    }

    /**
     * Gets a symbol from fully qualified split package name
     */
    protected @Nullable IZenSymbol getSymbol(String symbolID, String ... packageIDs) {
        SymbolPackage parent = root();
        for(String packageID : packageIDs) {
            parent = getAsPackage(parent.get(packageID));
            if(Objects.isNull(parent)) return null;
        }
        return parent.get(symbolID);
    }

    /**
     * Gets a symbol from fully qualified class name as it was registered via @ZenClass
     */
    protected @Nullable SymbolType getSymbolFromClassName(String name) {
        String[] packageIDs = name.split("\\.");
        if(packageIDs.length==1) return getSymbolType(packageIDs[0]);
        String simpleName = packageIDs[packageIDs.length-1];
        return getSymbolType(simpleName,Arrays.copyOfRange(packageIDs,0,packageIDs.length-1));
    }

    /**
     * Gets a symbol from fully qualified split package name and tries to cast it to SymbolType
     */
    protected @Nullable SymbolType getSymbolType(String symbolID, String ... packageIDs) {
        return getAsType(getSymbol(symbolID,packageIDs));
    }

    /**
     * Gets the global root package
     */
    protected SymbolPackage root() {
        return GlobalRegistry.getRoot();
    }
}
