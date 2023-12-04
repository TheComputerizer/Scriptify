package mods.thecomputerizer.scriptify.command;

import mods.thecomputerizer.scriptify.Scriptify;
import mods.thecomputerizer.scriptify.command.subcmd.SubCmd;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Methods used by both the ScriptifyCommand class and sub commands
 */
public abstract class AbstractCommand {

    protected final Collection<ISubType<?>> subTypes;

    protected AbstractCommand(ISubType.Type ... subTypes) {
        this.subTypes = new ArrayList<>();
        for(ISubType.Type type : subTypes) this.subTypes.add(type.make());
    }

    protected String[] array(String ... args) {
        return args;
    }

    protected List<String> findMatchingSubTypes(String arg) {
        List<String> validSubs = new ArrayList<>();
        for(ISubType<?> sub : this.subTypes) {
            String name = sub.getName();
            if(arg.isEmpty() || name.startsWith(arg)) {
                if(sub instanceof SubCmd) validSubs.add(name);
                else validSubs.add(name+"=");
            }
        }
        return validSubs;
    }

    protected @Nullable SubCmd getSubCommand(String arg) {
        for(ISubType<?> sub : this.subTypes) {
            if(sub instanceof SubCmd && sub.getName().matches(arg)) return (SubCmd)sub;
        }
        return null;
    }

    protected @Nullable ISubType<?> getSubType(String arg) {
        for(ISubType<?> sub : this.subTypes) {
            if(sub.getName().matches(arg)) return sub;
        }
        return null;
    }

    protected String lang(String ... args) {
        return Scriptify.langKey("commands",args);
    }

    protected String[] nextArgs(String ... args) {
        return Arrays.copyOfRange(args,1,args.length);
    }

    protected void sendGeneric(ICommandSender sender, String[] args, Object ... parameters) {
        sender.sendMessage(new TextComponentTranslation(lang(args),parameters));
    }

    protected void sendSuccess(ICommandSender sender, Object ... parameters) {
        sendGeneric(sender,array("success"),parameters);
    }

    protected void throwGeneric(String[] args, Object ... parameters) throws CommandException {
        throw new CommandException(lang(args),parameters);
    }

    protected String validSubs() {
        return TextUtil.compileCollection(this.subTypes);
    }
}
