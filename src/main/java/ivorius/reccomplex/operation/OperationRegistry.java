/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.operation;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.WorldServer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by lukas on 10.02.15.
 */
public class OperationRegistry
{
    private static BiMap<String, Class<? extends Operation>> operations = HashBiMap.create();

    public static void register(String id, Class<? extends Operation> operation)
    {
        operations.put(id, operation);
    }

    @Nullable
    public static Operation readOperation(@Nonnull NBTTagCompound compound)
    {
        String opID = compound.getString("opID");
        Class<? extends Operation> clazz = operations.get(opID);

        if (clazz == null)
        {
            RecurrentComplex.logger.error(String.format("Unrecognized Operation ID '%s'", opID));
            return null;
        }

        try
        {
            Operation operation = clazz.newInstance();
            operation.readFromNBT(compound);
            return operation;
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            RecurrentComplex.logger.error(String.format("Could not read Operation with ID '%s'", opID), e);
        }

        return null;
    }

    public static NBTTagCompound writeOperation(@Nonnull Operation operation)
    {
        NBTTagCompound compound = new NBTTagCompound();
        operation.writeToNBT(compound);
        compound.setString("opID", operations.inverse().get(operation.getClass()));
        return compound;
    }

    public static void queueOperation(Operation operation, ICommandSender commandSender) throws PlayerNotFoundException
    {
        boolean instant = true;

        if (commandSender instanceof EntityPlayer)
        {
            EntityPlayer player = CommandBase.getCommandSenderAsPlayer(commandSender);
            StructureEntityInfo info = StructureEntityInfo.getStructureEntityInfo(player);
            if (info != null)
            {
                if (info.getPreviewType() != Operation.PreviewType.NONE)
                {
                    info.queueOperation(operation, player);
                    instant = false;

                    ITextComponent confirmComponent = new TextComponentString("/" + RCCommands.confirm.getCommandName());
                    confirmComponent.getStyle().setColor(TextFormatting.GREEN);
                    confirmComponent.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + RCCommands.confirm.getCommandName()));
                    confirmComponent.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ServerTranslations.get("commands.rcconfirm.run")));

                    ITextComponent cancelComponent = new TextComponentString("/" + RCCommands.cancel.getCommandName());
                    cancelComponent.getStyle().setColor(TextFormatting.RED);
                    cancelComponent.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + RCCommands.cancel.getCommandName()));
                    cancelComponent.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ServerTranslations.get("commands.rccancel.run")));

                    ITextComponent component = ServerTranslations.format("commands.rc.queuedOp", confirmComponent, cancelComponent);
                    commandSender.addChatMessage(component);
                }
            }
        }

        if (instant)
            operation.perform((WorldServer) commandSender.getEntityWorld());
    }
}
