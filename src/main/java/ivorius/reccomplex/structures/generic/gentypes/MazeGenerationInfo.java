/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.gentypes;

import com.google.gson.*;
import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.ivtoolkit.random.WeightedSelector;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.editstructure.gentypes.TableDataSourceMazeGenerationInfo;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.generic.Selection;
import ivorius.reccomplex.structures.generic.maze.*;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;

/**
 * Created by lukas on 07.10.14.
 */
public class MazeGenerationInfo extends StructureGenerationInfo implements WeightedSelector.Item
{
    private static Gson gson = createGson();

    public String mazeID;
    public Double weight;

    public SavedMazeComponent mazeComponent;

    public MazeGenerationInfo()
    {
        this(randomID(MazeGenerationInfo.class), null, "", new SavedMazeComponent(ConnectorStrategy.DEFAULT_WALL));
        mazeComponent.rooms.addAll(Selection.zeroSelection(3));
    }

    public MazeGenerationInfo(String id, Double weight, String mazeID, SavedMazeComponent mazeComponent)
    {
        super(id);
        this.weight = weight;
        this.mazeID = mazeID;
        this.mazeComponent = mazeComponent;
    }

    public static Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(MazeGenerationInfo.class, new Serializer());
        builder.registerTypeAdapter(SavedMazeComponent.class, new SavedMazeComponent.Serializer());
        builder.registerTypeAdapter(MazeRoom.class, new SavedMazeComponent.RoomSerializer());
        builder.registerTypeAdapter(SavedMazeReachability.class, new SavedMazeReachability.Serializer());
        builder.registerTypeAdapter(SavedMazePath.class, new SavedMazePath.Serializer());
        builder.registerTypeAdapter(SavedMazePathConnection.class, new SavedMazePathConnection.Serializer());

        return builder.create();
    }

    public static Gson getGson()
    {
        return gson;
    }

    @Nonnull
    @Override
    public String id()
    {
        return id;
    }

    @Override
    public void setID(@Nonnull String id)
    {
        this.id = id;
    }

    @Override
    public double getWeight()
    {
        return weight != null ? weight : 1.0;
    }

    public boolean hasDefaultWeight()
    {
        return weight == null;
    }

    @Override
    public String displayString()
    {
        return IvTranslations.format("reccomplex.generationInfo.mazeComponent.title", mazeID);
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceMazeGenerationInfo(navigator, delegate, this);
    }

    public static class Serializer implements JsonSerializer<MazeGenerationInfo>, JsonDeserializer<MazeGenerationInfo>
    {
        @Override
        public MazeGenerationInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(json, "MazeGenerationInfo");

            String id = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "id", "");

            String mazeID = JsonUtils.getJsonObjectStringFieldValue(jsonObject, "mazeID");

            JsonObject componentJson = JsonUtils.getJsonObjectFieldOrDefault(jsonObject, "component", new JsonObject());

            Double weight = jsonObject.has("weight") ? JsonUtils.getJsonObjectDoubleFieldValue(jsonObject, "weight") : null;
            if (weight == null) // Legacy, weight was in SavedMazeComponent's JSON
            {
                if (componentJson.has("weightD"))
                    weight = JsonUtils.getJsonObjectDoubleFieldValue(componentJson, "weightD");
                else if (componentJson.has("weight"))
                    weight = JsonUtils.getJsonObjectIntegerFieldValue(componentJson, "weight") * 0.01; // 100 was default
            }

            SavedMazeComponent mazeComponent = gson.fromJson(componentJson, SavedMazeComponent.class);

            return new MazeGenerationInfo(id, weight, mazeID, mazeComponent);
        }

        @Override
        public JsonElement serialize(MazeGenerationInfo src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", src.id);

            if (src.weight != null)
                jsonObject.addProperty("weight", src.weight);

            jsonObject.addProperty("mazeID", src.mazeID);
            jsonObject.add("component", gson.toJsonTree(src.mazeComponent));

            return jsonObject;
        }
    }
}
