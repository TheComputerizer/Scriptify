package mods.thecomputerizer.scriptify.command.parameters;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.util.Collections;
import java.util.List;

public class ParameterZenFileOutput extends Parameter<String> {
    public ParameterZenFileOutput() {
        super(Type.PARAMETER_ZEN_FILE_OUTPUT);
    }

    @Override
    public List<String> getTabCompletions(String... args) {
        return Collections.emptyList();
    }

    @Override
    public int isRequired() {
        return 0;
    }

    @Override
    protected String parse(MinecraftServer server, ICommandSender sender, String valueStr) {
        return valueStr;
    }
}
