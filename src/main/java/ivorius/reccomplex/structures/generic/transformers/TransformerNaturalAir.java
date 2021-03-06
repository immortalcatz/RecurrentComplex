/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.transformers;

import com.google.gson.*;
import net.minecraft.util.math.BlockPos;
import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.blocks.RCBlocks;
import ivorius.reccomplex.gui.editstructure.transformers.TableDataSourceBTNaturalAir;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.StructureLoadContext;
import ivorius.reccomplex.structures.StructurePrepareContext;
import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.structures.generic.matchers.BlockMatcher;
import net.minecraft.block.state.IBlockState;
import ivorius.reccomplex.utils.NBTNone;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public class TransformerNaturalAir extends TransformerSingleBlock<NBTNone>
{
    public static final double DEFAULT_NATURAL_EXPANSION_DISTANCE = 4.0;
    public static final double DEFAULT_NATURAL_EXPANSION_RANDOMIZATION = 10.0;

    public BlockMatcher sourceMatcher;

    public double naturalExpansionDistance;
    public double naturalExpansionRandomization;

    public TransformerNaturalAir()
    {
        this(randomID(TransformerNaturalAir.class), BlockMatcher.of(RecurrentComplex.specialRegistry, RCBlocks.genericSpace, 1), DEFAULT_NATURAL_EXPANSION_DISTANCE, DEFAULT_NATURAL_EXPANSION_RANDOMIZATION);
    }

    public TransformerNaturalAir(String id, String sourceMatcherExpression, double naturalExpansionDistance, double naturalExpansionRandomization)
    {
        super(id);
        this.sourceMatcher = new BlockMatcher(RecurrentComplex.specialRegistry, sourceMatcherExpression);
        this.naturalExpansionDistance = naturalExpansionDistance;
        this.naturalExpansionRandomization = naturalExpansionRandomization;
    }

    @Override
    public boolean matches(NBTNone instanceData, IBlockState state)
    {
        return sourceMatcher.apply(state);
    }

    @Override
    public void transformBlock(NBTNone instanceData, Phase phase, StructureSpawnContext context, BlockPos coord, IBlockState sourceState)
    {
        // TODO Fix for partial generation
        World world = context.world;
        Random random = context.random;

        Biome biome = world.getBiome(coord);
        IBlockState topBlock = biome.topBlock != null ? biome.topBlock : Blocks.AIR.getDefaultState();
        IBlockState fillerBlock = biome.fillerBlock != null ? biome.fillerBlock : Blocks.AIR.getDefaultState();

        coord = coord.up(4);

        int currentY = coord.getY();
        List<int[]> currentList = new ArrayList<>();
        List<int[]> nextList = new ArrayList<>();
        nextList.add(new int[]{coord.getX(), coord.getZ()});

        int worldHeight = world.getHeight();
        while (nextList.size() > 0 && currentY < worldHeight)
        {
            List<int[]> cachedList = currentList;
            currentList = nextList;
            nextList = cachedList;

            while (currentList.size() > 0)
            {
                int[] currentPos = currentList.remove(0);
                int currentX = currentPos[0];
                int currentZ = currentPos[1];

                BlockPos curBlockPos = new BlockPos(currentX, currentY, currentZ);
                IBlockState curBlock = world.getBlockState(curBlockPos);

                Material material = curBlock.getMaterial();
                boolean isFoliage = curBlock.getBlock().isFoliage(world, curBlockPos) || material == Material.LEAVES || material == Material.PLANTS || material == Material.WOOD;
                boolean isCommon = curBlock == Blocks.STONE || curBlock == Blocks.DIRT || curBlock == Blocks.SAND || curBlock == Blocks.STAINED_HARDENED_CLAY || curBlock == Blocks.GRAVEL;
                boolean replaceable = currentY == coord.getY() || curBlock == topBlock || curBlock == fillerBlock || curBlock.getBlock().isReplaceable(world, curBlockPos)
                        || isCommon || isFoliage;

                if (replaceable)
                    context.setBlock(curBlockPos, Blocks.AIR.getDefaultState(), 2);

                if (replaceable || material == Material.AIR)
                {
                    double distToOrigSQ = IvVecMathHelper.distanceSQ(new double[]{coord.getX(), coord.getY(), coord.getZ()}, new double[]{currentX, currentY, currentZ});
                    double add = (random.nextDouble() - random.nextDouble()) * naturalExpansionRandomization;
                    distToOrigSQ += add < 0 ? -(add * add) : (add * add);

                    if (distToOrigSQ < naturalExpansionDistance * naturalExpansionDistance)
                        TransformerNatural.addNewNeighbors(nextList, currentX, currentZ);
                }
            }

            currentY++;
        }
    }

    @Override
    public String getDisplayString()
    {
        return "Natural Air: " + sourceMatcher.getDisplayString();
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceBTNaturalAir(this, navigator, delegate);
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
    public boolean generatesInPhase(NBTNone instanceData, Phase phase)
    {
        return phase == Phase.BEFORE;
    }

    public static class Serializer implements JsonDeserializer<TransformerNaturalAir>, JsonSerializer<TransformerNaturalAir>
    {
        private MCRegistry registry;

        public Serializer(MCRegistry registry)
        {
            this.registry = registry;
        }

        @Override
        public TransformerNaturalAir deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(jsonElement, "transformerNatural");

            String id = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "id", randomID(TransformerNaturalAir.class));

            String expression = TransformerReplace.Serializer.readLegacyMatcher(jsonObject, "source", "sourceMetadata"); // Legacy
            if (expression == null)
                expression = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "sourceExpression", "");

            double naturalExpansionDistance = JsonUtils.getJsonObjectDoubleFieldValueOrDefault(jsonObject, "naturalExpansionDistance", DEFAULT_NATURAL_EXPANSION_DISTANCE);
            double naturalExpansionRandomization = JsonUtils.getJsonObjectDoubleFieldValueOrDefault(jsonObject, "naturalExpansionRandomization", DEFAULT_NATURAL_EXPANSION_RANDOMIZATION);

            return new TransformerNaturalAir(id, expression, naturalExpansionDistance, naturalExpansionRandomization);
        }

        @Override
        public JsonElement serialize(TransformerNaturalAir transformer, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", transformer.id());
            jsonObject.addProperty("sourceExpression", transformer.sourceMatcher.getExpression());

            jsonObject.addProperty("naturalExpansionDistance", transformer.naturalExpansionDistance);
            jsonObject.addProperty("naturalExpansionRandomization", transformer.naturalExpansionRandomization);

            return jsonObject;
        }
    }
}
