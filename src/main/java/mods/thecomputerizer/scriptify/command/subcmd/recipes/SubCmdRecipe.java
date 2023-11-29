package mods.thecomputerizer.scriptify.command.subcmd.recipes;

import mods.thecomputerizer.scriptify.command.subcmd.SubCmd;

public class SubCmdRecipe extends SubCmd {

    public SubCmdRecipe() {
        super(Type.RECIPE, Type.CUSTOM.make(), Type.SHAPED.make(), Type.SHAPELESS.make());
    }

    @Override
    public int isRequired() {
        return 0;
    }

    @Override
    protected boolean hasParameters() {
        return false;
    }
}
