package mods.thecomputerizer.scriptify.command.subcmd.help;

import mods.thecomputerizer.scriptify.command.AbstractCommand;
import mods.thecomputerizer.scriptify.command.subcmd.SubCmd;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class SubCmdParameters extends SubCmd {

    public SubCmdParameters() {
        super(Type.PARAMETERS);
    }

    @Override
    public AbstractCommand execute(MinecraftServer server, ICommandSender sender) throws CommandException {
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
