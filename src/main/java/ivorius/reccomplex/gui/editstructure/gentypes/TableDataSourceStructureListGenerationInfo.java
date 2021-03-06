/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import ivorius.ivtoolkit.blocks.Directions;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.RCGuiTables;
import ivorius.reccomplex.gui.TableDataSourceBlockPos;
import ivorius.reccomplex.gui.TableDirections;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.StructureInfos;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.generic.gentypes.StructureListGenerationInfo;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 07.10.14.
 */
public class TableDataSourceStructureListGenerationInfo extends TableDataSourceSegmented
{
    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    private StructureListGenerationInfo generationInfo;

    public TableDataSourceStructureListGenerationInfo(TableNavigator navigator, TableDelegate tableDelegate, StructureListGenerationInfo generationInfo)
    {
        this.navigator = navigator;
        this.tableDelegate = tableDelegate;
        this.generationInfo = generationInfo;

        addManagedSection(0, new TableDataSourceGenerationInfo(generationInfo, navigator, tableDelegate));
        addManagedSection(3, new TableDataSourceBlockPos(generationInfo.shift, generationInfo::setShift, new IntegerRange(-50, 50), new IntegerRange(-50, 50), new IntegerRange(-50, 50),
                IvTranslations.get("reccomplex.generationInfo.structureList.shift.x"), IvTranslations.get("reccomplex.generationInfo.structureList.shift.y"), IvTranslations.get("reccomplex.generationInfo.structureList.shift.z")));
    }

    @Override
    public int numberOfSegments()
    {
        return 5;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
            case 1:
                return 1;
            case 2:
                return 1;
            case 4:
                return 1;
        }
        return super.sizeOfSegment(segment);
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        switch (segment)
        {
            case 1:
            {
                TableCellString cell = new TableCellString("listID", generationInfo.listID);
                cell.setShowsValidityState(true);
                cell.setValidityState(currentStructureListIDState());
                cell.addPropertyListener(cell1 ->
                {
                    generationInfo.listID = cell.getPropertyValue();
                    cell.setValidityState(currentStructureListIDState());
                });
                return new TableElementCell(IvTranslations.get("reccomplex.generationInfo.structureList.id"), cell);
            }
            case 2:
                return RCGuiTables.defaultWeightElement(cell -> generationInfo.weight = TableElements.toDouble((Float) cell.getPropertyValue()), generationInfo.weight);
            case 4:
            {
                TableCellEnum cell = new TableCellEnum<>("front", generationInfo.front, TableDirections.getDirectionOptions(Directions.HORIZONTAL));
                cell.addPropertyListener(cell1 -> generationInfo.front = (EnumFacing) cell.getPropertyValue());
                return new TableElementCell(IvTranslations.get("reccomplex.generationInfo.structureList.front"), cell);
            }
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    @Nonnull
    protected GuiValidityStateIndicator.State currentStructureListIDState()
    {
        return StructureInfos.isSimpleID(generationInfo.listID)
                ? StructureRegistry.INSTANCE.getStructuresInList(generationInfo.listID, null).size() > 0
                ? GuiValidityStateIndicator.State.VALID
                : GuiValidityStateIndicator.State.SEMI_VALID
                : GuiValidityStateIndicator.State.INVALID;
    }
}
