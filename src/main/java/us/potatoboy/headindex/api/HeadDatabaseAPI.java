package us.potatoboy.headindex.api;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import us.potatoboy.headindex.HeadIndex;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java          .net.URLConnection;

public class HeadDatabaseAPI {
    private final String apiUrl = "https://minecraft-heads.com/scripts/api.php?cat=%s&tags=true";

    public Multimap<Head.Category, Head> getHeads() {
        Multimap<Head.Category, Head> heads = HashMultimap.create();
        Gson gson = new Gson();

        for (Head.Category category : Head.Category.values()) {
            HeadIndex.LOGGER.info(String.format("Fetching heads from %s category", category.name));
            try {
                URLConnection connection = new URL(String.format(apiUrl, category.name)).openConnection();
                connection.connect();

                JsonArray headsJson = new JsonParser().parse(new InputStreamReader((InputStream) connection.getContent())).getAsJsonArray();

                for (JsonElement headJson : headsJson) {
                    Head head = gson.fromJson(headJson, Head.class);
                    heads.put(category, head);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        HeadIndex.LOGGER.info(String.format("Finished fetching %d heads", heads.size()));
        return heads;
    }
}
