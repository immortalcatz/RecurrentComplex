/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.utils.BlockSurfacePos;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.worldgen.WorldGenStructures;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandDecorateOne extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "decorateone";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.rcdecorateone.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        BlockSurfacePos coord;

        if (args.length >= 2)
            coord = RCCommands.parseSurfaceBlockPos(commandSender, args, 0, false);
        else
            coord = BlockSurfacePos.from(commandSender.getPosition());

        WorldServer entityWorld = (WorldServer) commandSender.getEntityWorld();
        if (!WorldGenStructures.generateRandomStructureInChunk(entityWorld.rand, coord.chunkCoord(), entityWorld, entityWorld.getBiome(coord.blockPos(0))))
        {
            throw ServerTranslations.commandException("commands.rcdecorateone.none");
        }
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1 || args.length == 2)
            return getListOfStringsMatchingLastWord(args, "~");

        return null;
    }
}
