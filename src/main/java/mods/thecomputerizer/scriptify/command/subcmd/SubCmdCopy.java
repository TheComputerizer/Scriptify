package mods.thecomputerizer.scriptify.command.subcmd;

import mods.thecomputerizer.scriptify.Scriptify;
import mods.thecomputerizer.scriptify.io.data.ExpressionData;
import mods.thecomputerizer.scriptify.io.read.ZenFileReader;
import mods.thecomputerizer.scriptify.io.write.FileWriter;
import mods.thecomputerizer.scriptify.io.write.ZenFileWriter;
import mods.thecomputerizer.scriptify.network.PacketSendContainerInfo;
import mods.thecomputerizer.scriptify.util.Misc;
import net.minecraft.command.CommandException;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;
import java.util.function.Function;

public class SubCmdCopy extends SubCmd {

    public SubCmdCopy() {
        super(Type.COMMAND_COPY,Type.PARAMETER_CLASS_NAMES,Type.PARAMETER_ENHANCEMENTS,Type.PARAMETER_METHOD_NAMES,
                Type.PARAMETER_PARAMETERS,Type.PARAMETER_SAVE_PARAMETERS,Type.PARAMETER_SORT_BY,
                Type.PARAMETER_ZEN_FILE_INPUTS,Type.PARAMETER_ZEN_FILE_OUTPUTS);
    }

    private Map<String,List<ExpressionData>> applySort(Map<ZenFileReader,List<ExpressionData>> dataMap, String sortBy) {
        Map<String,List<ExpressionData>> sorted = new HashMap<>();
        Iterable<List<ExpressionData>> dataItr = dataMap.values();
        if(sortBy.startsWith("class"))
            applySort(sorted,dataItr,data -> data.getBlueprint().getClassName());
        else if(sortBy.startsWith("method"))
            applySort(sorted,dataItr,data -> data.getBlueprint().getMethodName());
        else if(sortBy.startsWith("mod"))
            applySort(sorted,dataItr,data -> data.getBlueprint().getMod());
        else if(sortBy.startsWith("args"))
            applySort(sorted,dataItr,data -> data.getBlueprint().getFirstTypeSimpleName());
        else applySort(sorted,dataItr,data -> data.getBlueprint().getClassName());
        return sorted;
    }

    private void applySort(Map<String,List<ExpressionData>> outputMap, Iterable<List<ExpressionData>> parsedIter,
                           Function<ExpressionData,String> stringFunc) {
        for(List<ExpressionData> dataList : parsedIter) {
            for(ExpressionData data : dataList) {
                String str = stringFunc.apply(data);
                outputMap.putIfAbsent(str,new ArrayList<>());
                outputMap.get(str).add(data);
            }
        }
    }

    @Override
    public void execute() throws CommandException {
        defineParameterSets();
        Map<ZenFileReader,List<ExpressionData>> dataMap = parseInputFiles(getParameterAsFileList("zenFileInput"));
        if(dataMap.isEmpty()) throwGeneric(array("copy","data"));
        runEnhancements(dataMap,getParameterAsList("enhancements"));
        runOutput(dataMap,getParameterAsString("zenFileOutput"));
    }

    @Override
    protected void executeOnPacket(PacketSendContainerInfo packet) {}

    private String getSortQualifier(String sortBy) {
        return "alphabetical";
    }

    @Override
    protected boolean hasParameters() {
        return true;
    }

    @Override
    public int isRequired() {
        return 0;
    }

    private Map<ZenFileReader,List<ExpressionData>> parseInputFiles(List<String> inputFilePaths) throws CommandException {
        Map<ZenFileReader,List<ExpressionData>> map = new HashMap<>();
        List<String> classNames = getParameterAsList("classNames");
        List<String> methodNames = getParameterAsList("methodNames");
        if(inputFilePaths.isEmpty()) throwGeneric(array("copy","input"));
        for(String path : new HashSet<>(inputFilePaths)) {
            ZenFileReader reader = new ZenFileReader(path);
            reader.setDebug(true);
            if(Objects.nonNull(reader.getClassName())) {
                List<ExpressionData> data = new ArrayList<>();
                try {
                    data = reader.parseFilteredRecipeData(classNames,methodNames);
                } catch(Exception ex) {
                    Scriptify.logError(getClass(),"data",ex,path);
                }
                if(data.isEmpty()) Scriptify.logInfo(getClass(),"empty",path);
                else map.put(reader,data);
            } else Scriptify.logDebug(getClass(),"parse",path);
        }
        return map;
    }

    private void runEnhancements(Map<ZenFileReader,List<ExpressionData>> dataMap, List<String> enhancements) {
    }

    private void runOutput(Map<ZenFileReader,List<ExpressionData>> dataMap, String output) throws CommandException {
        if(StringUtils.isBlank(output)) throwGeneric(array("copy","output"));
        Map<String,List<ExpressionData>> sortedDataMap = applySort(dataMap,getParameterAsString("sortBy"));
        if(output.endsWith(".zs")) writeFile(sortedDataMap,output);
        else writeDirectory(sortedDataMap,output);
    }

    private void writeDirectory(Map<String,List<ExpressionData>> sortedDataMap, String dirPath) {
        for(Map.Entry<String,List<ExpressionData>> sortedEntry : sortedDataMap.entrySet()) {
            String sortKey = sortedEntry.getKey();
            ZenFileWriter writer = new ZenFileWriter();
            writer.getComments().set("Automagically Generated",String.format("Sort Element `$1%s`",sortKey));
            for(ExpressionData data : sortedEntry.getValue()) {
                writer.getWriters().add(data.makeWriter());
                data.finalizeWriter(writer);
            }
            writer.addPreProcessor("reloadable");
            writer.writeToFile(new File(dirPath,Misc.getLastSplit(sortedEntry.getKey(),".") +".zs").getPath(),true);
        }
        sendGeneric(this.sender,array("copy","write"),sortedDataMap.size(),dirPath);
    }

    private void writeFile(Map<String,List<ExpressionData>> sortedDataMap, String filePath) {
        ZenFileWriter zenWriter = new ZenFileWriter();
        for(Map.Entry<String,List<ExpressionData>> sortedEntry : sortedDataMap.entrySet()) {
            boolean first = true;
            for(ExpressionData data : sortedEntry.getValue()) {
                FileWriter writer = data.makeWriter();
                if(first) {
                    writer.getComments().set("Automagically Generated","Sort Element `"+sortedEntry.getKey()+"`");
                    first = false;
                }
                zenWriter.getWriters().add(writer);
                data.finalizeWriter(zenWriter);
            }
        }
        zenWriter.addPreProcessor("reloadable");
        zenWriter.writeToFile(filePath,true);
        sendGeneric(this.sender,array("copy","write"),sortedDataMap.size(),filePath);
    }
}
