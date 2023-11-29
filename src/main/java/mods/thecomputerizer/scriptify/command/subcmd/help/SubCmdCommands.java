package mods.thecomputerizer.scriptify.command.subcmd.help;

import mods.thecomputerizer.scriptify.command.AbstractCommand;
import mods.thecomputerizer.scriptify.command.subcmd.SubCmd;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class SubCmdCommands extends SubCmd {

    public SubCmdCommands() {
        super(Type.COMMANDS);
    }

    @Override
    public AbstractCommand execute(MinecraftServer server, ICommandSender sender) {
        return this;
    }

    @Override
    public int isRequired() {
        return 2;
    }

    @Override
    protected boolean hasParameters() {
        return true;
    }
}
