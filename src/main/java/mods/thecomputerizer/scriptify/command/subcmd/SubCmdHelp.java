package mods.thecomputerizer.scriptify.command.subcmd;

import mods.thecomputerizer.scriptify.Scriptify;
import mods.thecomputerizer.scriptify.command.AbstractCommand;
import mods.thecomputerizer.scriptify.command.ISubType;
import mods.thecomputerizer.scriptify.network.PacketSendContainerInfo;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SubCmdHelp extends SubCmd {

    public static List<String> getTypes(String name, String arg) {
        List<String> ret = new ArrayList<>();
        for(String type : Arrays.asList("desc","sub"))
            if(type.startsWith(arg)) ret.add(name+"="+arg);
        return ret;
    }

    public SubCmdHelp() {
        super(Type.COMMAND_HELP,Type.PARAMETER_PARAMETERS,Type.PARAMETER_QUERY,Type.PARAMETER_TYPE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public AbstractCommand execute(MinecraftServer server, ICommandSender sender) throws CommandException {
        defineParameterSets(server,sender);
        List<String> queries = ((List<String>)getParameter(this.getType(),"query").execute(server,sender));
        if(Objects.nonNull(queries) && !queries.isEmpty()) {
            for(String query : queries) {
                String category = "parameters";
                if(Objects.isNull(Type.getParameter(query))) category = "commands";
                String type = ((String)getParameter(this.getType(),"query").execute(server,sender)).trim().toLowerCase();
                String lang = Scriptify.langKey(category,query,type);
                sender.sendMessage(new TextComponentTranslation(lang,getTypeString()));
            }
        } else throwGeneric(array(getName(),"fail"),TextUtil.listToString(queries,","));
        return this;
    }

    private String getTypeString() {
        List<String> subNames = new ArrayList<>();
        for(ISubType<?> sub : this.subTypes) subNames.add(sub.getName());
        return TextUtil.listToString(subNames," ");
    }

    @Override
    protected boolean hasParameters() {
        return true;
    }

    @Override
    public int isRequired() {
        return 0;
    }

    @Override
    protected void executeOnPacket(MinecraftServer server, @Nullable EntityPlayerMP player, PacketSendContainerInfo packet) {}
}
