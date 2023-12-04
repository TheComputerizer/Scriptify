package mods.thecomputerizer.scriptify.io.read;

import crafttweaker.zenscript.GlobalRegistry;
import mods.thecomputerizer.scriptify.Scriptify;
import mods.thecomputerizer.scriptify.ScriptifyRef;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import stanhebben.zenscript.ZenModule;
import stanhebben.zenscript.ZenParsedFile;
import stanhebben.zenscript.ZenTokener;
import stanhebben.zenscript.compiler.IEnvironmentGlobal;
import stanhebben.zenscript.definitions.ParsedFunction;
import stanhebben.zenscript.definitions.ParsedFunctionArgument;
import stanhebben.zenscript.statements.Statement;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class ZenFileReader implements IClampedStringReader<String> {

    private ZenParsedFile parsedFile;

    public ZenFileReader(String fileName) {
        Map<String,byte[]> classes = new HashMap<>();
        IEnvironmentGlobal env = GlobalRegistry.makeGlobalEnvironment(classes,ScriptifyRef.NAME);
        String className = ZenModule.extractClassName(fileName);
        File file = FileUtil.generateNestedFile(fileName,false);
        try(Reader reader = new InputStreamReader(new BufferedInputStream(Files.newInputStream(file.toPath())),StandardCharsets.UTF_8)) {
            ZenTokener parser = new ZenTokener(reader,env.getEnvironment(),fileName,false);
            Scriptify.logInfo("Parsing script file {}",fileName);
            this.parsedFile = new ZenParsedFile(fileName,className,parser,env);
        } catch(Exception e) {
            Scriptify.logError("Failed to parse file {}",fileName);
            this.parsedFile = null;
        }
    }

    public void testMove(String outputFile) {
        File file = FileUtil.generateNestedFile(outputFile,true);
        Scriptify.logInfo("Moving to file {}",outputFile);
        List<String> lines = new ArrayList<>();
        lines.add("#reloadable");
        lines.add("");
        lines.add("import mods.extendedcrafting.TableCrafting;");
        lines.add("");
        lines.add("//Automagically generated!");
        copy(lines);
        FileUtil.writeLinesToFile(file,lines,false);
    }

    @Override
    public void copy(List<String> lines) {
        Set<Map.Entry<String, ParsedFunction>> functionEntries = this.parsedFile.getFunctions().entrySet();
        Scriptify.logInfo("Writing {} functions",functionEntries.size());
        for(Map.Entry<String, ParsedFunction> functionEntry : functionEntries) {
            ParsedFunction function = functionEntry.getValue();
            Scriptify.logInfo("Adding function with name {}",function.getName());
            lines.add(function.getName());
            Scriptify.logInfo("Function has {} args",function.getArguments().size());
            for(ParsedFunctionArgument arg : function.getArguments()) {
                Scriptify.logInfo("Arg name is {}",arg.getName());
                Scriptify.logInfo("Arg type name is {}",arg.getType().getName());
                Scriptify.logInfo("Arg default expression is {}",arg.getDefaultExpression());
                lines.add(arg.getName());
                lines.add(arg.getType().toString());
                lines.add(arg.getDefaultExpression().toString());
            }
        }
        List<Statement> statements = this.parsedFile.getStatements();
        Scriptify.logInfo("Writing {} statements",statements.size());
        for(Statement statement : statements) new StatementReader(statement).copy(lines);
    }

    @Override
    public String parse(String unparsed) {
        return null;
    }
}
