/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.transformers;

import com.google.gson.*;
import net.minecraft.util.math.BlockPos;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.editstructure.transformers.TableDataSourceBTPillar;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.StructureLoadContext;
import ivorius.reccomplex.structures.StructurePrepareContext;
import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.structures.generic.matchers.BlockMatcher;
import net.minecraft.block.state.IBlockState;
import ivorius.reccomplex.utils.BlockStates;
import ivorius.reccomplex.utils.NBTNone;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.lang.reflect.Type;

/**
 * Created by lukas on 25.05.14.
 */
public class TransformerPillar extends TransformerSingleBlock<NBTNone>
{
    public BlockMatcher sourceMatcher;

    public IBlockState destState;

    public TransformerPillar()
    {
        this(randomID(TransformerPillar.class), BlockMatcher.of(RecurrentComplex.specialRegistry, Blocks.STONE, 0), Blocks.STONE.getDefaultState());
    }

    public TransformerPillar(String id, String sourceExpression, IBlockState destState)
    {
        super(id);
        this.sourceMatcher = new BlockMatcher(RecurrentComplex.specialRegistry, sourceExpression);
        this.destState = destState;
    }

    @Override
    public boolean matches(NBTNone instanceData, IBlockState state)
    {
        return sourceMatcher.apply(state);
    }

    @Override
    public void transformBlock(NBTNone instanceData, Phase phase, StructureSpawnContext context, BlockPos coord, IBlockState sourceState)
    {
        if (RecurrentComplex.specialRegistry.isSafe(destState.getBlock()))
        {
            // TODO Fix for partial generation
            World world = context.world;

            int y = coord.getY();

            do
            {
                BlockPos pos = new BlockPos(coord.getX(), y--, coord.getZ());
                context.setBlock(pos, destState, 2);

                IBlockState blockState = world.getBlockState(pos);
                if (!(blockState.getBlock().isReplaceable(world, pos) || blockState.getMaterial() == Material.LEAVES || blockState.getBlock().isFoliage(world, pos)))
                    break;
            }
            while (y > 0);
        }
    }

    @Override
    public NBTNone prepareInstanceData(StructurePrepareContext context)
    {
        return new NBTNone();
    }

    @Override
    public NBTNone loadInstanceData(StructureLoadContext context, NBTBase nbt)
    {
        return new NBTNone();
    }

    @Override
    public String getDisplayString()
    {
        return "Pillar: " + sourceMatcher.getDisplayString() + "->" + destState.getBlock().getLocalizedName();
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceBTPillar(this, navigator, delegate);
    }

    @Override
    public boolean generatesInPhase(NBTNone instanceData, Phase phase)
    {
        return phase == Phase.BEFORE;
    }

    public static class Serializer implements JsonDeserializer<TransformerPillar>, JsonSerializer<TransformerPillar>
    {
        private MCRegistry registry;

        public Serializer(MCRegistry registry)
        {
            this.registry = registry;
        }

        @Override
        public TransformerPillar deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(jsonElement, "transformerPillar");

            String id = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "id", randomID(TransformerPillar.class));

            String expression = TransformerReplace.Serializer.readLegacyMatcher(jsonObject, "source", "sourceMetadata"); // Legacy
            if (expression == null)
                expression = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "sourceExpression", "");

            String destBlock = JsonUtils.getJsonObjectStringFieldValue(jsonObject, "dest");
            Block dest = registry.blockFromID(new ResourceLocation(destBlock));
            IBlockState destState = dest != null ? dest.getStateFromMeta(JsonUtils.getJsonObjectIntegerFieldValue(jsonObject, "destMetadata")) : null;

            return new TransformerPillar(id, expression, destState);
        }

        @Override
        public JsonElement serialize(TransformerPillar transformer, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", transformer.id());
            jsonObject.addProperty("sourceExpression", transformer.sourceMatcher.getExpression());

            jsonObject.addProperty("dest", registry.idFromBlock(transformer.destState.getBlock()).toString());
            jsonObject.addProperty("destMetadata", BlockStates.toMetadata(transformer.destState));

            return jsonObject;
        }
    }
}
