/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.gentypes;

import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.structures.StructureRegistry;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * Created by lukas on 19.02.15.
 */
public abstract class StructureGenerationInfo
{
    @Nonnull
    protected String id;

    public StructureGenerationInfo(@Nonnull String id)
    {
        this.id = id;
    }

    public static String randomID(Class<? extends StructureGenerationInfo> type)
    {
        Random random = new Random();
        return String.format("%s_%s", StructureRegistry.INSTANCE.getGenerationInfoRegistry().iDForType(type), Integer.toHexString(random.nextInt()));
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

    public abstract String displayString();

    public abstract TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate);
}
