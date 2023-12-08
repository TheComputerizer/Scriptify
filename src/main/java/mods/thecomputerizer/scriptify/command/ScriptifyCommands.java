package mods.thecomputerizer.scriptify.command;

import mcp.MethodsReturnNonnullByDefault;
import mods.thecomputerizer.scriptify.ScriptifyRef;
import mods.thecomputerizer.scriptify.command.ISubType.Type;
import mods.thecomputerizer.scriptify.command.subcmd.SubCmd;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ScriptifyCommands extends AbstractCommand implements ICommand {

    private SubCmd nextSubCmd;
    public ScriptifyCommands() {
        super(Type.COMMAND_HELP,Type.COMMAND_COPY,Type.COMMAND_RECIPE,Type.COMMAND_RELOAD_CACHE,Type.COMMAND_RUN,
                Type.COMMAND_TEST);
    }

    @Override
    public String getName() {
        return ScriptifyRef.MODID;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return lang("usage");
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String ... args) throws CommandException {
        if(args.length==0) throwGeneric(array("usage"),validSubs());
        SubCmd sub = getSubCommand(args[0]);
        if(Objects.nonNull(sub)) {
            sub.setWorkingParameters(server,sender);
            this.nextSubCmd = (SubCmd)sub.collect(nextArgs(args));
        }
        else throwGeneric(array("unknown"),args[0]);
        if(Objects.nonNull(this.nextSubCmd)) this.nextSubCmd.execute();
        this.nextSubCmd.afterExecute(null);
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return sender.canUseCommand(4,this.getName());
    }

    @Override
    public List<String> getTabCompletions(
            MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if(args.length==0) return Collections.emptyList();
        if(args.length==1) return findMatchingSubTypes(args[0]);
        SubCmd sub = getSubCommand(args[0]);
        return Objects.nonNull(sub) ? sub.getTabCompletions(nextArgs(args)) : Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(ICommand other) {
        return this.getName().compareTo(other.getName());
    }
}
