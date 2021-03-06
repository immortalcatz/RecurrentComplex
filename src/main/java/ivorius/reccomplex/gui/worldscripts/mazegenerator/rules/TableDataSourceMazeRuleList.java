/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator.rules;

import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.maze.SavedMazePathConnection;
import ivorius.reccomplex.structures.generic.maze.rules.MazeRule;
import ivorius.reccomplex.structures.generic.maze.rules.MazeRuleRegistry;
import ivorius.reccomplex.utils.IvClasses;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by lukas on 21.03.16.
 */
public class TableDataSourceMazeRuleList extends TableDataSourceList<MazeRule, List<MazeRule>>
{
    private List<SavedMazePathConnection> expected;
    private List<IntegerRange> bounds;

    public TableDataSourceMazeRuleList(List<MazeRule> list, TableDelegate tableDelegate, TableNavigator navigator, List<SavedMazePathConnection> expected, List<IntegerRange> bounds)
    {
        super(list, tableDelegate, navigator);
        this.expected = expected;
        this.bounds = bounds;
        setUsesPresetActionForAdding(true);
    }

    @Override
    public String getDisplayString(MazeRule mazeRule)
    {
        return StringUtils.abbreviate(mazeRule.displayString(), 24);
    }

    @Override
    public MazeRule newEntry(String actionID)
    {
        return IvClasses.instantiate(MazeRuleRegistry.INSTANCE.objectClass(actionID));
    }

    @Override
    public TableDataSource editEntryDataSource(MazeRule mazeRule)
    {
        return mazeRule.tableDataSource(navigator, tableDelegate, expected, bounds);
    }

    @Override
    public TableCellButton[] getAddActions()
    {
        Collection<String> allTypes = MazeRuleRegistry.INSTANCE.allIDs();
        List<TableCellButton> actions = new ArrayList<>(allTypes.size());
        for (String type : allTypes)
        {
            String baseKey = "reccomplex.mazerule." + type;
            actions.add(new TableCellButton(type, type,
                    IvTranslations.get(baseKey),
                    IvTranslations.formatLines(baseKey + ".tooltip")
            ));
        }
        return actions.toArray(new TableCellButton[actions.size()]);
    }
}
