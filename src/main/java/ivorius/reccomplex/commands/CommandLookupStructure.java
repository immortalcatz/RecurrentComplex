/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import ivorius.reccomplex.structures.generic.Metadata;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandLookupStructure extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "lookup";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.rclookup.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length >= 1)
        {
            String strucKey = args[0];
            GenericStructureInfo structureInfo = CommandExportStructure.getGenericStructureInfo(strucKey);
            Metadata metadata = structureInfo.metadata;

            boolean hasAuthor = !metadata.authors.trim().isEmpty();
            ITextComponent author = hasAuthor ? new TextComponentString(StringUtils.abbreviate(metadata.authors, 30)) : ServerTranslations.format("commands.rclookup.reply.noauthor");
            if (hasAuthor)
                author.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(metadata.authors)));

            boolean hasWeblink = !metadata.weblink.trim().isEmpty();
            ITextComponent weblink = hasWeblink ? new TextComponentString(StringUtils.abbreviate(metadata.weblink, 30)) : ServerTranslations.format("commands.rclookup.reply.nolink");
            if (hasWeblink)
            {
                weblink.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, metadata.weblink));
                weblink.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(metadata.weblink)));
            }

            commandSender.addChatMessage(ServerTranslations.format(
                    StructureRegistry.INSTANCE.isStructureGenerating(strucKey) ? "commands.rclookup.reply.generates" : "commands.rclookup.reply.silent",
                    strucKey, author, weblink));

            if (!metadata.comment.trim().isEmpty())
                commandSender.addChatMessage(ServerTranslations.format("commands.rclookup.reply.comment", metadata.comment));
        }
        else
        {
            throw ServerTranslations.commandException("commands.rclookup.usage");
        }
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1)
        {
            Set<String> allStructureNames = StructureRegistry.INSTANCE.allStructureIDs();

            return getListOfStringsMatchingLastWord(args, allStructureNames);
        }

        return null;
    }
}
