/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.presets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ivorius.reccomplex.structures.generic.BiomeGenerationInfo;
import ivorius.reccomplex.utils.ListPresets;

/**
 * Created by lukas on 26.02.15.
 */
public class BiomeMatcherPresets extends ListPresets<BiomeGenerationInfo>
{
    public static final String FILE_SUFFIX = "rcbp";

    private static BiomeMatcherPresets instance;

    public BiomeMatcherPresets(String fileSuffix)
    {
        super(fileSuffix);
    }

    public static BiomeMatcherPresets instance()
    {
        return instance != null ? instance : (instance = new BiomeMatcherPresets(FILE_SUFFIX));
    }

    @Override
    protected Gson createGson()
    {
        return new GsonBuilder().registerTypeAdapter(BiomeGenerationInfo.class, new BiomeGenerationInfo.Serializer()).create();
    }

    @Override
    protected Class<BiomeGenerationInfo[]> getType()
    {
        return BiomeGenerationInfo[].class;
    }
}
