/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.villages;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraft.util.math.BlockPos;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.structures.StructureInfo;
import ivorius.reccomplex.structures.StructureInfos;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.generic.gentypes.StructureGenerationInfo;
import ivorius.reccomplex.structures.generic.gentypes.VanillaStructureGenerationInfo;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces;

import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 18.01.15.
 */
public class GenericVillageCreationHandler implements VillagerRegistry.IVillageCreationHandler
{
    protected String structureID;
    protected String generationID;

    public GenericVillageCreationHandler(String structureID, String generationID)
    {
        this.structureID = structureID;
        this.generationID = generationID;
    }

    public static GenericVillageCreationHandler forGeneration(String structureID, String generationID)
    {
        return getPieceClass(structureID, generationID) != null
                ? new GenericVillageCreationHandler(structureID, generationID)
                : null;
    }

    public static Class<? extends GenericVillagePiece> getPieceClass(String structureID, String generationID)
    {
        return VanillaGenerationClassFactory.instance().getClass(structureID, generationID);
    }

    @Override
    public StructureVillagePieces.PieceWeight getVillagePieceWeight(Random random, int villageSize)
    {
        StructureInfo structureInfo = StructureRegistry.INSTANCE.isStructureGenerating(structureID) ? StructureRegistry.INSTANCE.getStructure(structureID) : null;
        if (structureInfo != null)
        {
            StructureGenerationInfo generationInfo = structureInfo.generationInfo(generationID);
            if (generationInfo instanceof VanillaStructureGenerationInfo)
            {
                VanillaStructureGenerationInfo vanillaGenInfo = (VanillaStructureGenerationInfo) generationInfo;

                int spawnLimit = MathHelper.floor_double(MathHelper.getRandomDoubleInRange(random,
                        vanillaGenInfo.minBaseLimit + villageSize * vanillaGenInfo.minScaledLimit,
                        vanillaGenInfo.maxBaseLimit + villageSize * vanillaGenInfo.maxScaledLimit) + 0.5);
                return new StructureVillagePieces.PieceWeight(getComponentClass(), vanillaGenInfo.getVanillaWeight(), spawnLimit);
            }
        }

        return new StructureVillagePieces.PieceWeight(getComponentClass(), 0, 0);
    }

    @Override
    public Class<? extends StructureVillagePieces.Village> getComponentClass()
    {
        return getPieceClass(structureID, generationID);
    }

    @Override
    public StructureVillagePieces.Village buildComponent(StructureVillagePieces.PieceWeight villagePiece, StructureVillagePieces.Start startPiece, List<StructureComponent> pieces, Random random, int x, int y, int z, EnumFacing front, int generationDepth)
    {
        StructureInfo structureInfo = StructureRegistry.INSTANCE.getStructure(structureID);

        if (structureInfo != null)
        {
            StructureGenerationInfo generationInfo = structureInfo.generationInfo(generationID);
            if (generationInfo instanceof VanillaStructureGenerationInfo)
            {
                VanillaStructureGenerationInfo vanillaGenInfo = (VanillaStructureGenerationInfo) generationInfo;

                boolean mirrorX = structureInfo.isMirrorable() && random.nextBoolean();
                AxisAlignedTransform2D transform = GenericVillagePiece.getTransform(vanillaGenInfo, front, mirrorX);

                if (vanillaGenInfo.generatesIn(startPiece.biome) && structureInfo.isRotatable() || transform.getRotation() == 0)
                {
                    int[] structureSize = StructureInfos.structureSize(structureInfo, transform);
                    BlockPos structureShift = new BlockPos(0, 0, 0); // TODO Reserved for future shifts where allowed

                    StructureBoundingBox strucBB = StructureInfos.structureBoundingBox(structureShift, structureSize);
                    int derotatedX = front.getAxis() == EnumFacing.Axis.X ? strucBB.getXSize() : strucBB.getZSize(); // TODO is correct?
                    int derotatedZ = front.getAxis() == EnumFacing.Axis.Z ? strucBB.getXSize() : strucBB.getZSize();
                    strucBB = StructureBoundingBox.getComponentToAddBoundingBox(x + strucBB.minX, y + strucBB.minY, z + strucBB.minZ, 0, 0, 0, derotatedX, strucBB.getYSize(), derotatedZ, front);

                    if (GenericVillagePiece.canVillageGoDeeperC(strucBB) && StructureComponent.findIntersecting(pieces, strucBB) == null)
                    {
                        GenericVillagePiece genericVillagePiece = GenericVillagePiece.create(structureID, generationID, startPiece, generationDepth);

                        if (genericVillagePiece != null)
                        {
                            // Do this after the test because this is raw structure movement
                            strucBB.offset(structureShift.getX(), structureShift.getY(), structureShift.getZ());

                            genericVillagePiece.setIds(structureID, generationID);
                            genericVillagePiece.setOrientation(front, mirrorX, strucBB);
                            genericVillagePiece.prepare(random);

                            return genericVillagePiece;
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenericVillageCreationHandler that = (GenericVillageCreationHandler) o;

        if (!structureID.equals(that.structureID)) return false;
        return generationID.equals(that.generationID);

    }

    @Override
    public int hashCode()
    {
        int result = structureID.hashCode();
        result = 31 * result + generationID.hashCode();
        return result;
    }
}
