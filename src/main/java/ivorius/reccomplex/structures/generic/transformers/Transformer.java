/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.transformers;

import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.structures.StructureLoadContext;
import ivorius.reccomplex.structures.StructurePrepareContext;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.utils.NBTStorable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public abstract class Transformer<S extends NBTStorable>
{
    @Nonnull
    private String id;

    public Transformer(@Nonnull String id)
    {
        this.id = id;
    }

    public static String randomID(Class<? extends Transformer> type)
    {
        Random random = new Random();
        return String.format("%s_%s", StructureRegistry.INSTANCE.getTransformerRegistry().iDForType(type), Integer.toHexString(random.nextInt()));
    }

    public static String randomID(String type)
    {
        Random random = new Random();
        return String.format("%s_%s", type, Integer.toHexString(random.nextInt()));
    }

    @Nonnull
    public String id()
    {
        return id;
    }

    public void setID(@Nonnull String id)
    {
        this.id = id;
    }

    public abstract String getDisplayString();

    public abstract TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate);

    public abstract S prepareInstanceData(StructurePrepareContext context);

    public abstract S loadInstanceData(StructureLoadContext context, NBTBase nbt);

    public abstract boolean skipGeneration(S instanceData, IBlockState state);

    public abstract void transform(S instanceData, Phase phase, StructureSpawnContext context, IvWorldData worldData, List<Pair<Transformer, NBTStorable>> transformers);

    public abstract boolean generatesInPhase(S instanceData, Phase phase);

    public enum Phase
    {
        BEFORE,
        AFTER
    }
}
