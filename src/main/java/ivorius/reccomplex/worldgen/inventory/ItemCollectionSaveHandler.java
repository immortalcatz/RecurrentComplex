/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.inventory;

import ivorius.ivtoolkit.tools.IvFileHelper;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.files.FileLoadContext;
import ivorius.reccomplex.files.FileTypeHandler;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollection.Component;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by lukas on 25.05.14.
 */
public class ItemCollectionSaveHandler implements FileTypeHandler
{
    public static final ItemCollectionSaveHandler INSTANCE = new ItemCollectionSaveHandler();

    public static final String FILE_SUFFIX = "rcig";

    public static Component readInventoryGenerator(Path file) throws IOException, InventoryLoadException
    {
        return GenericItemCollectionRegistry.INSTANCE.createComponentFromJSON(new String(Files.readAllBytes(file)));
    }

    @Override
    public boolean loadFile(Path path, FileLoadContext context)
    {
        try
        {
            Component component = readInventoryGenerator(path);

            String name = context.customID != null ? context.customID : FilenameUtils.getBaseName(path.getFileName().toString());

            if (component.inventoryGeneratorID == null || component.inventoryGeneratorID.length() == 0) // Legacy support
                component.inventoryGeneratorID = name;

            GenericItemCollectionRegistry.INSTANCE.register(component, name, context.domain, context.active, context.custom);

            return true;
        }
        catch (IOException | InventoryLoadException e)
        {
            RecurrentComplex.logger.warn("Error reading inventory generator", e);
        }

        return false;
    }

    @Override
    public void clearCustomFiles()
    {
        GenericItemCollectionRegistry.INSTANCE.clearCustom();
    }

    public static boolean saveInventoryGenerator(@Nonnull Component info, @Nonnull String name)
    {
        File structuresFile = IvFileHelper.getValidatedFolder(RecurrentComplex.proxy.getBaseFolderFile("structures"));
        if (structuresFile != null)
        {
            File inventoryGeneratorsFile = IvFileHelper.getValidatedFolder(structuresFile, "active");
            if (inventoryGeneratorsFile != null)
            {
                File newFile = new File(inventoryGeneratorsFile, String.format("%s.%s", name, FILE_SUFFIX));
                String json = GenericItemCollectionRegistry.INSTANCE.createJSONFromComponent(info);

                try
                {
                    newFile.delete(); // Prevent case mismatching
                    FileUtils.writeStringToFile(newFile, json);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                return newFile.exists();
            }
        }

        return false;
    }
}
