package mods.thecomputerizer.scriptify.command;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.scriptify.ScriptifyRef;
import mods.thecomputerizer.scriptify.command.parameters.types.*;
import mods.thecomputerizer.scriptify.command.subcmd.SubCmdHelp;
import mods.thecomputerizer.scriptify.command.subcmd.SubCmdRecipe;
import mods.thecomputerizer.scriptify.command.subcmd.SubCmdRun;
import mods.thecomputerizer.scriptify.command.subcmd.SubCmdTest;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public interface ISubType<T> {

    ISubType<T> collect(String ... args) throws CommandException;
    T execute(MinecraftServer server, ICommandSender sender) throws CommandException;
    String getLang(String ... args);
    String getName();
    List<String> getTabCompletions(String ... args);
    boolean isParameter();
    int isRequired();
    void send(ByteBuf buf);

    enum Type {

        COMMAND_HELP("help",SubCmdHelp::new),
        COMMAND_RECIPE("recipe",SubCmdRecipe::new),
        COMMAND_RUN("run",SubCmdRun::new),
        COMMAND_TEST("test",SubCmdTest::new),
        PARAMETER_CONTAINER_TYPE("containerType","point", ParameterContainerType::new),
        PARAMETER_CONTAINER_SIZE("containerSize","9x3", ParameterContainerSize::new),
        PARAMETER_MAX_LINE_WIDTH("maxLineWidth","80", ParameterLineWidth::new),
        PARAMETER_NAME("name","name", ParameterName::new),
        PARAMETER_OREDICT("oreDict","[]", ParameterOreDict::new),
        PARAMETER_PARAMETERS("parameters","default", ParameterParameters::new),
        PARAMETER_QUERY("query","help", ParameterParameters::new),
        PARAMETER_TOTAL_SLOTS("totalSlots","27",ParameterTotalSlots::new),
        PARAMETER_TYPE("type","shaped",ParameterType::new),
        PARAMETER_ZEN_FILE_INPUT("zenFileInput","scripts/"+ScriptifyRef.NAME+"/inputs/input.zs",ParameterZenFileInput::new),
        PARAMETER_ZEN_FILE_OUTPUT("zenFileOutput","config/"+ ScriptifyRef.NAME+"/outputs/output.zs",ParameterZenFileOutput::new);

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

        public final String name;
        private final boolean isCmd;
        private final String defVal;
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
