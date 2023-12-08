package mods.thecomputerizer.scriptify.command;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import mods.thecomputerizer.scriptify.ScriptifyRef;
import mods.thecomputerizer.scriptify.command.parameters.types.*;
import mods.thecomputerizer.scriptify.command.subcmd.*;
import net.minecraft.command.CommandException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public interface ISubType<T> {

    ISubType<T> collect(String ... args) throws CommandException;
    String getLang(String ... args);
    String getName();
    List<String> getTabCompletions(String ... args);
    boolean isParameter();
    int isRequired();
    void send(ByteBuf buf);

    @SuppressWarnings("SpellCheckingInspection")
    enum Type {

        COMMAND_COPY("copy",SubCmdCopy::new),
        COMMAND_HELP("help",SubCmdHelp::new),
        COMMAND_RECIPE("recipe",SubCmdRecipe::new),
        COMMAND_RELOAD_CACHE("reloadcache",SubCmdReloadCache::new),
        COMMAND_RUN("run",SubCmdRun::new),
        COMMAND_TEST("test",SubCmdTest::new),
        PARAMETER_CLASS_NAMES("classNames","recipes",ParameterClassNames::new),
        PARAMETER_COMMANDS("commands","[]",ParameterCommands::new),
        PARAMETER_CONTAINER_TYPE("containerType","point",ParameterContainerType::new),
        PARAMETER_CONTAINER_SIZE("containerSize","9x3",ParameterContainerSize::new),
        PARAMETER_ENHANCEMENTS("enhancements","[]",ParameterEnhancements::new),
        PARAMETER_FILTERS("filters","[]",ParameterFilters::new),
        PARAMETER_MAX_LINE_WIDTH("maxLineWidth","80",ParameterLineWidth::new),
        PARAMETER_METHOD_NAMES("methodNames","[addShaped,addShapeless]",ParameterMethodNames::new),
        PARAMETER_NAME("name","name",ParameterName::new),
        PARAMETER_OREDICT("oreDict","[]",ParameterOreDict::new),
        PARAMETER_SORT_BY("sortBy","class", ParameterSortBy::new),
        PARAMETER_PARAMETERS("parameters","default",ParameterParameters::new),
        PARAMETER_QUERY("query","help",ParameterQuery::new),
        PARAMETER_SAVE_COMMAND("saveCommand","null", ParameterSaveCommand::new),
        PARAMETER_SAVE_PARAMETERS("saveParameters","null", ParameterSaveParameters::new),
        PARAMETER_TOTAL_SLOTS("totalSlots","27",ParameterTotalSlots::new),
        PARAMETER_TYPE("type","shaped",ParameterType::new),
        PARAMETER_ZEN_FILE_INPUTS("zenFileInput","scripts/"+ScriptifyRef.NAME+"/inputs/input.zs",ParameterZenFileInput::new),
        PARAMETER_ZEN_FILE_OUTPUTS("zenFileOutput","config/"+ ScriptifyRef.NAME+"/outputs/output.zs",ParameterZenFileOutput::new);

        private static final Map<String, Type> PARAMETERS_BY_NAME = new HashMap<>();
        private static final Map<String, Type> SUB_COMMANDS_BY_NAME = new HashMap<>();


        public static String[] getDefaultsParameters() {
            List<Type> parameters = new ArrayList<>(PARAMETERS_BY_NAME.values());
            String[] defaults = new String[parameters.size()];
            for(int i=0; i<defaults.length; i++)
                defaults[i] = parameters.get(i).withDefault();
            return defaults;
        }

        public static Type getParameter(String name) {
            return PARAMETERS_BY_NAME.getOrDefault(name,PARAMETER_NAME);
        }

        public static Type getSubCmd(String name) {
            return SUB_COMMANDS_BY_NAME.getOrDefault(name,COMMAND_HELP);
        }

        public static boolean isCommand(String name) {
            for(Type type : values())
                if(type.name.matches(name))
                    return type.isCmd;
            return false;
        }

        public final String name;
        @Getter private final boolean isCmd;
        @Getter private final String defVal;
        private final Supplier<? extends ISubType<?>> typeSupplier;

        Type(String name, Supplier<? extends ISubType<?>> typeSupplier) {
            this(name,true,"",typeSupplier);
        }

        Type(String name, String defVal, Supplier<? extends ISubType<?>> typeSupplier) {
            this(name,false,defVal,typeSupplier);
        }

        Type(String name, boolean isCmd, String defVal, Supplier<? extends ISubType<?>> typeSupplier) {
            this.name = name;
            this.isCmd = isCmd;
            this.defVal = defVal;
            this.typeSupplier = typeSupplier;
        }

        public String getDefault() {
            return this.defVal;
        }

        public ISubType<?> make() {
            return this.typeSupplier.get();
        }

        @Override
        public String toString() {
            return this.name;
        }

        public String withDefault() {
            return this.name+"="+this.defVal;
        }

        static {
            for(Type type : values()) {
                if(type.isCmd) SUB_COMMANDS_BY_NAME.put(type.name,type);
                else PARAMETERS_BY_NAME.put(type.name,type);
            }
        }
    }
}
