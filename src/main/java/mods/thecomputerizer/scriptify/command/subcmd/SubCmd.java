package mods.thecomputerizer.scriptify.command.subcmd;

import mods.thecomputerizer.scriptify.Scriptify;
import mods.thecomputerizer.scriptify.command.AbstractCommand;
import mods.thecomputerizer.scriptify.command.ISubType;
import mods.thecomputerizer.scriptify.command.parameters.Parameter;
import mods.thecomputerizer.scriptify.command.subcmd.help.SubCmdCommands;
import mods.thecomputerizer.scriptify.command.subcmd.help.SubCmdHelp;
import mods.thecomputerizer.scriptify.command.subcmd.help.SubCmdParameters;
import mods.thecomputerizer.scriptify.command.subcmd.recipes.SubCmdCustom;
import mods.thecomputerizer.scriptify.command.subcmd.recipes.SubCmdRecipe;
import mods.thecomputerizer.scriptify.command.subcmd.recipes.SubCmdShaped;
import mods.thecomputerizer.scriptify.command.subcmd.recipes.SubCmdShapeless;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

public abstract class SubCmd extends AbstractCommand implements ISubType<AbstractCommand> {

    private final Type type;
    private SubCmd nextSubCmd;
    protected List<Parameter<?>> parameters;

    public SubCmd(Type type, ISubType<?> ... subTypes) {
        super(subTypes);
        this.type = type;
        this.parameters = new ArrayList<>();
    }

    public Type getType() {
        return this.type;
    }

    @Override
    public ISubType<AbstractCommand> collect(String ... args) throws CommandException {
        if(!hasParameters()) {
            if(args.length==0) throwGeneric(array("usage"), validSubs());
            SubCmd sub = getSubCommand(args[0]);
            if(Objects.nonNull(sub)) this.nextSubCmd = (SubCmd)sub.collect(nextArgs(args));
            else throwGeneric(array("unknown"), args[0]);
        }
        for(String arg : args) {
            Parameter<?> parameter = getParameter(arg);
            if(Objects.nonNull(parameter))
                this.parameters.add((Parameter<?>)parameter.collect(arg));
        }
        return this;
    }

    protected Parameter<?> getParameter(@Nullable String arg) throws CommandException {
        if(Objects.isNull(arg) || arg.isEmpty()) throwGeneric(array("unknown"),arg);
        else
            for(ISubType<?> sub : this.subTypes)
                if(sub instanceof Parameter<?> && sub.getName().matches(arg.split("=",2)[0]))
                    return (Parameter<?>)sub;
        return null;
    }

    @Override
    public AbstractCommand execute(MinecraftServer server, ICommandSender sender) throws CommandException {
        if(Objects.nonNull(this.nextSubCmd)) this.nextSubCmd.execute(server,sender);
        else throwGeneric(array("execute"));
        return this;
    }

    @Override
    public String getName() {
        return this.type.name;
    }

    @Override
    public List<String> getTabCompletions(String ... args) {
        if(args.length==0) return Collections.emptyList();
        if(args.length==1 || hasParameters()) return findMatchingSubTypes(args[args.length-1]);
        SubCmd sub = getSubCommand(args[0]);
        return Objects.nonNull(sub) ? sub.getTabCompletions(Arrays.copyOfRange(args,1,args.length)) :
                Collections.emptyList();
    }

    @Override
    public String getLang(String ... args) {
        return Scriptify.langKey("commands",args);
    }

    @Override
    public boolean isParameter() {
        return false;
    }

    protected abstract boolean hasParameters();

    @Override
    public String toString() {
        return this.type.toString();
    }

    public enum Type {

        COMMANDS("commands", SubCmdCommands::new),
        CUSTOM("custom", SubCmdCustom::new),
        HELP("help", SubCmdHelp::new),
        PARAMETERS("parameters", SubCmdParameters::new),
        RECIPE("recipe", SubCmdRecipe::new),
        SHAPED("shaped", SubCmdShaped::new),
        SHAPELESS("shapeless", SubCmdShapeless::new);

        private static final Map<String, Type> BY_NAME = new HashMap<>();

        public static Type get(String name) {
            return BY_NAME.getOrDefault(name,HELP);
        }

        public final String name;
        private final Supplier<SubCmd> cmdSupplier;

        Type(String name, Supplier<SubCmd> cmdSupplier) {
            this.name = name;
            this.cmdSupplier = cmdSupplier;
        }

        @Override
        public String toString() {
            return this.name;
        }

        public SubCmd make() {
            return this.cmdSupplier.get();
        }

        static {
            for(Type type : values()) BY_NAME.put(type.name,type);
        }
    }
}
