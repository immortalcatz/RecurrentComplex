/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.network;

import ivorius.ivtoolkit.network.SchedulingMessageHandler;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.files.RCFileTypeRegistry;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import ivorius.reccomplex.structures.generic.StructureSaveHandler;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Collections;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketSaveStructureHandler extends SchedulingMessageHandler<PacketSaveStructure, IMessage>
{
    public static void saveStructure(GenericStructureInfo structureInfo, String structureID, boolean saveAsActive, boolean deleteOther)
    {
        RecurrentComplex.network.sendToServer(new PacketSaveStructure(structureInfo, structureID, saveAsActive, deleteOther));
    }

    @Override
    public void processServer(PacketSaveStructure message, MessageContext ctx, WorldServer server)
    {
        NetHandlerPlayServer netHandlerPlayServer = ctx.getServerHandler();
        EntityPlayerMP player = netHandlerPlayServer.playerEntity;

        if (RecurrentComplex.checkPerms(player)) return;

        StructureEntityInfo structureEntityInfo = StructureEntityInfo.getStructureEntityInfo(player);
        GenericStructureInfo genericStructureInfo = message.getStructureInfo();

        if (structureEntityInfo != null)
            genericStructureInfo.worldDataCompound = structureEntityInfo.getCachedExportStructureBlockDataNBT();

        String path = RCFileTypeRegistry.getDirectoryName(message.isSaveAsActive()) + "/";
        String structureID = message.getStructureID();

        if (!StructureSaveHandler.INSTANCE.saveGenericStructure(genericStructureInfo, structureID, message.isSaveAsActive()))
        {
            player.addChatMessage(ServerTranslations.format("structure.save.failure", path + structureID));
        }
        else
        {
            player.addChatMessage(ServerTranslations.format("structure.save.success", path + structureID));

            if (message.isDeleteOther() && StructureSaveHandler.INSTANCE.hasGenericStructure(structureID, !message.isSaveAsActive()))
            {
                String otherPath = RCFileTypeRegistry.getDirectoryName(!message.isSaveAsActive()) + "/";

                if (StructureSaveHandler.INSTANCE.deleteGenericStructure(structureID, !message.isSaveAsActive()))
                    player.addChatMessage(ServerTranslations.format("structure.delete.success", otherPath + structureID));
                else
                    player.addChatMessage(ServerTranslations.format("structure.delete.failure", otherPath + structureID));
            }

            RecurrentComplex.fileTypeRegistry.reloadCustomFiles(Collections.singletonList(StructureSaveHandler.FILE_SUFFIX));
        }
    }
}
