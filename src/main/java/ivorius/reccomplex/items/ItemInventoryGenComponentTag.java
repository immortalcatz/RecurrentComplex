/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.items;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.RCGuiHandler;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollection.Component;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollectionRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;

import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 05.01.15.
 */
public class ItemInventoryGenComponentTag extends Item implements GeneratingItem
{
    public static String componentKey(ItemStack stack)
    {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("itemCollectionKey", Constants.NBT.TAG_STRING))
            return stack.getTagCompound().getString("itemCollectionKey");
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("display", Constants.NBT.TAG_COMPOUND)) // Legacy - Display Name
        {
            NBTTagCompound nbttagcompound = stack.getTagCompound().getCompoundTag("display");
            if (nbttagcompound.hasKey("Name", Constants.NBT.TAG_STRING))
                return nbttagcompound.getString("Name");
        }


        return null;
    }

    public static void setComponentKey(ItemStack stack, String generatorKey)
    {
        stack.setTagInfo("itemCollectionKey", new NBTTagString(generatorKey));
    }

    public static Component component(ItemStack stack)
    {
        return GenericItemCollectionRegistry.INSTANCE.component(componentKey(stack));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        if (component(itemStackIn) != null || componentKey(itemStackIn) == null)
        {
            if (!worldIn.isRemote)
                playerIn.openGui(RecurrentComplex.instance, RCGuiHandler.editInventoryGen, worldIn, playerIn.inventory.currentItem, 0, 0);
        }

        return super.onItemRightClick(itemStackIn, worldIn, playerIn, hand);
    }

    @Override
    public void generateInInventory(WorldServer server, IInventory inventory, Random random, ItemStack stack, int fromSlot)
    {
        Component component = component(stack);

        if (component != null)
            inventory.setInventorySlotContents(fromSlot, component.getRandomItemStack(random));
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        String key = componentKey(stack);

        return key != null ? key : super.getItemStackDisplayName(stack);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advancedInformation)
    {
        super.addInformation(stack, player, list, advancedInformation);

        Component component = component(stack);

        if (component != null)
        {
            list.add(component.inventoryGeneratorID);
            list.add(GenericItemCollectionRegistry.INSTANCE.isActive(componentKey(stack))
                    ? IvTranslations.format("inventoryGen.active", ChatFormatting.GREEN, ChatFormatting.RESET)
                    : IvTranslations.format("inventoryGen.inactive", ChatFormatting.RED, ChatFormatting.RESET));
        }
        else
            list.add(IvTranslations.get("inventoryGen.create"));
    }

    @Override
    public void getSubItems(Item item, CreativeTabs creativeTabs, List list)
    {
        super.getSubItems(item, creativeTabs, list);

        for (String key : GenericItemCollectionRegistry.INSTANCE.allComponentKeys())
        {
            ItemStack stack = new ItemStack(item);
            setComponentKey(stack, key);
            list.add(stack);
        }
    }
}
