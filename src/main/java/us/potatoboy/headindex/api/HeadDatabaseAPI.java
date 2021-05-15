package us.potatoboy.headindex.api;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.*;
import us.potatoboy.headindex.HeadIndex;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class HeadDatabaseAPI {
    private final String apiUrl = "https://minecraft-heads.com/scripts/api.php?cat=%s&tags=true";

    public Multimap<Head.Category, Head> getHeads() {
        Multimap<Head.Category, Head> heads = HashMultimap.create();
        Gson gson= new Gson();

        for (Head.Category category : Head.Category.values()) {
            HeadIndex.LOGGER.info(String.format("Fetching heads from %s category", category.name));
            try {
                URLConnection connection = new URL(String.format(apiUrl, category.name)).openConnection();

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(String.format(apiUrl, category.name))).build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                JsonArray headsJson = new JsonParser().parse(response.body()).getAsJsonArray();
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
