/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.structuregenerator;

import ivorius.ivtoolkit.blocks.Directions;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.TableDataSourceBlockPos;
import ivorius.reccomplex.gui.TableDirections;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.scripts.world.WorldScriptStructureGenerator;
import ivorius.reccomplex.structures.StructureRegistry;
import joptsimple.internal.Strings;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

import static ivorius.reccomplex.gui.table.TableCellEnum.Option;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceStructureGenerator extends TableDataSourceSegmented implements TableCellPropertyListener
{
    protected WorldScriptStructureGenerator script;

    protected TableNavigator tableNavigator;
    protected TableDelegate tableDelegate;

    public TableDataSourceStructureGenerator(WorldScriptStructureGenerator script, TableNavigator tableNavigator, TableDelegate tableDelegate)
    {
        this.script = script;
        this.tableNavigator = tableNavigator;
        this.tableDelegate = tableDelegate;

        addManagedSection(2, new TableDataSourceBlockPos(script.getStructureShift(), script::setStructureShift,
                new IntegerRange(-50, 50), new IntegerRange(-50, 50), new IntegerRange(-50, 50),
                IvTranslations.get("reccomplex.worldscript.strucGen.shift.x"), IvTranslations.get("reccomplex.worldscript.strucGen.shift.y"), IvTranslations.get("reccomplex.worldscript.strucGen.shift.z")));
    }

    private static boolean doAllStructuresExist(Iterable<String> structures)
    {
        for (String s : structures)
        {
            if (s.length() != 0 && StructureRegistry.INSTANCE.getStructure(s) == null)
                return false; // s==0 = "No structure"
        }

        return true;
    }

    @Override
    public int numberOfSegments()
    {
        return script.isSimpleMode() ? 4 : 4;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        if (segment == 0)
            return 1;
        else if (segment == 1)
            return 1;
        else if (segment == 3)
            return script.isSimpleMode() ? 2 : 1;

        return super.sizeOfSegment(segment);
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            TableCellBoolean cell = new TableCellBoolean("simpleMode", script.isSimpleMode());
            cell.addPropertyListener(this);
            return new TableElementCell(IvTranslations.get("reccomplex.worldscript.strucGen.mode.simple"), cell);
        }
        else if (segment == 1)
        {
            if (script.isSimpleMode())
            {
                TableCellString cell = new TableCellString("generators", Strings.join(script.getStructureNames(), ","));
                cell.setShowsValidityState(true);
                cell.setValidityState(doAllStructuresExist(script.getStructureNames()) ? GuiValidityStateIndicator.State.VALID : GuiValidityStateIndicator.State.SEMI_VALID);
                cell.addPropertyListener(this);
                cell.setTooltip(IvTranslations.getLines("reccomplex.worldscript.strucGen.simple.generators.tooltip"));
                return new TableElementCell(IvTranslations.get("reccomplex.worldscript.strucGen.simple.generators"), cell);
            }
            else
            {
                TableCellString cell = new TableCellString("listID", script.getStructureListID());
                cell.addPropertyListener(this);
                return new TableElementCell(IvTranslations.get("reccomplex.worldscript.strucGen.mode.list.id"), cell);
            }
        }
        else if (segment == 3)
        {
            if (script.isSimpleMode())
            {
                if (index == 0)
                {
                    TableCellEnum cell = new TableCellEnum<>("rotation", script.getStructureRotation(),
                            new Option<>(0, IvTranslations.get("reccomplex.rotation.clockwise.0")),
                            new Option<>(1, IvTranslations.get("reccomplex.rotation.clockwise.1")),
                            new Option<>(2, IvTranslations.get("reccomplex.rotation.clockwise.2")),
                            new Option<>(3, IvTranslations.get("reccomplex.rotation.clockwise.3")),
                            new Option<>(null, IvTranslations.get("reccomplex.worldscript.strucGen.rotation.random")));
                    cell.addPropertyListener(this);
                    return new TableElementCell(IvTranslations.get("reccomplex.rotation"), cell);
                }
                else if (index == 1)
                {
                    TableCellEnum cell = new TableCellEnum<>("mirror", script.getStructureMirror(),
                            new Option<>(false, IvTranslations.get("gui.false")),
                            new Option<>(true, IvTranslations.get("gui.true")),
                            new Option<>(null, IvTranslations.get("reccomplex.worldscript.strucGen.mirror.random")));
                    cell.addPropertyListener(this);
                    return new TableElementCell(IvTranslations.get("reccomplex.mirror"), cell);
                }
            }
            else
            {
                TableCellEnum cell = new TableCellEnum<>("front", script.getFront(), TableDirections.getDirectionOptions(ArrayUtils.add(Directions.HORIZONTAL, null), "random"));
                cell.addPropertyListener(this);
                return new TableElementCell(IvTranslations.get("reccomplex.worldscript.strucGen.mode.list.front"), cell);
            }
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    @Override
    public void valueChanged(TableCellPropertyDefault cell)
    {
        if (cell.getID() != null)
        {
            switch (cell.getID())
            {
                case "simpleMode":
                    script.setSimpleMode((Boolean) cell.getPropertyValue());
                    tableDelegate.reloadData();
                    break;
                case "generators":
                {
                    String value = ((String) cell.getPropertyValue());
                    script.setStructureNames(Arrays.asList(value.split(",")));
                    ((TableCellString) cell).setValidityState(doAllStructuresExist(script.getStructureNames()) ? GuiValidityStateIndicator.State.VALID : GuiValidityStateIndicator.State.SEMI_VALID);
                    break;
                }
                case "listID":
                {
                    script.setStructureListID((String) cell.getPropertyValue());
                    break;
                }
                case "rotation":
                {
                    script.setStructureRotation((Integer) cell.getPropertyValue());
                    break;
                }
                case "mirror":
                {
                    script.setStructureMirror((Boolean) cell.getPropertyValue());
                    break;
                }
                case "front":
                {
                    script.setFront((EnumFacing) cell.getPropertyValue());
                    break;
                }
            }
        }
    }
}
