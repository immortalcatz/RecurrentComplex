/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator.reachability;

import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.maze.SavedMazePath;
import ivorius.reccomplex.structures.generic.maze.SavedMazeReachability;
import ivorius.ivtoolkit.tools.IvTranslations;

import java.util.List;
import java.util.Set;

/**
 * Created by lukas on 16.03.16.
 */
public class TableDataSourceMazeReachability extends TableDataSourceSegmented
{
    protected SavedMazeReachability reachability;

    private TableDelegate tableDelegate;
    private TableNavigator tableNavigator;

    public TableDataSourceMazeReachability(SavedMazeReachability reachability, TableDelegate tableDelegate, TableNavigator tableNavigator, Set<SavedMazePath> expected, List<IntegerRange> bounds)
    {
        this.reachability = reachability;
        this.tableDelegate = tableDelegate;
        this.tableNavigator = tableNavigator;

        TableCellTitle groupTitle = new TableCellTitle("", IvTranslations.get("reccomplex.reachability.groups"));
        groupTitle.setTooltip(IvTranslations.formatLines("reccomplex.reachability.groups.tooltip"));
        addManagedSection(0, new TableDataSourcePreloaded(new TableElementCell(groupTitle)));
        addManagedSection(1, new TableDataSourceMazeReachabilityGroups(reachability.groups, expected, tableDelegate, tableNavigator));

        TableCellTitle crossConnectionsTitle = new TableCellTitle("", IvTranslations.get("reccomplex.reachability.crossconnections"));
        crossConnectionsTitle.setTooltip(IvTranslations.formatLines("reccomplex.reachability.crossconnections.tooltip"));
        addManagedSection(2, new TableDataSourcePreloaded(new TableElementCell(crossConnectionsTitle)));
        addManagedSection(3, new TableDataSourcePathConnectionList(reachability.crossConnections, tableDelegate, tableNavigator, bounds));
    }

    public SavedMazeReachability getReachability()
    {
        return reachability;
    }

    public void setReachability(SavedMazeReachability reachability)
    {
        this.reachability = reachability;
    }

    public TableDelegate getTableDelegate()
    {
        return tableDelegate;
    }

    public void setTableDelegate(TableDelegate tableDelegate)
    {
        this.tableDelegate = tableDelegate;
    }

    public TableNavigator getTableNavigator()
    {
        return tableNavigator;
    }

    public void setTableNavigator(TableNavigator tableNavigator)
    {
        this.tableNavigator = tableNavigator;
    }
}
