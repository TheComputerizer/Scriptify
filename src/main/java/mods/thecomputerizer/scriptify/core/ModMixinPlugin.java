package mods.thecomputerizer.scriptify.core;

import mods.thecomputerizer.scriptify.ScriptifyRef;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class ModMixinPlugin implements ILateMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        return Collections.singletonList(ScriptifyRef.MODID+"_mods.mixin.json");
    }
}
