package us.potatoboy.headindex.api;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.FileUtils;
import us.potatoboy.headindex.HeadIndex;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;

public class HeadDatabaseAPI {
    private final String apiUrl = "https://minecraft-heads.com/scripts/api.php?cat=%s&tags=true";
    private final Path cachePath = FabricLoader.getInstance().getConfigDir().resolve("headindex-cache");

    public Multimap<Head.Category, Head> getHeads() {
        refreshCacheFromAPI();
        return loadCache();
    }

    private void refreshCacheFromAPI() {
        for (Head.Category category : Head.Category.values()) {
            try {
                HeadIndex.LOGGER.info("Saving {} heads to cache", category.name);
                URLConnection connection = new URL(String.format(apiUrl, category.name)).openConnection();

                var stream = new BufferedInputStream(connection.getInputStream());
                FileUtils.copyInputStreamToFile(stream, cachePath.resolve(category.name + ".json").toFile());
            } catch (IOException e) {
                HeadIndex.LOGGER.warn("Failed to save new heads to cache");
            }

            if (!Files.exists(cachePath.resolve(category.name + ".json"))) {
                HeadIndex.LOGGER.info("Loading fallback {} heads", category.name);
                try {
                    Files.createDirectories(cachePath);
                    Files.copy(
                            FabricLoader.getInstance().getModContainer(HeadIndex.MOD_ID).get().findPath("cache/" + category.name + ".json").get(),
                            cachePath.resolve(category.name + ".json")
                    );
                } catch (IOException e) {
                    HeadIndex.LOGGER.warn("Failed to load fallback heads", e);
                }
            }
        }
    }

    private Multimap<Head.Category, Head> loadCache() {
        Multimap<Head.Category, Head> heads = HashMultimap.create();
        Gson gson = new Gson();

        for (Head.Category category : Head.Category.values()) {
            try {
                HeadIndex.LOGGER.info("Loading {} heads from cache", category.name);
                var stream = Files.newInputStream(cachePath.resolve(category.name + ".json"));
                JsonArray headsJson = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonArray();

                for (JsonElement headJson : headsJson) {
                    try {
                        Head head = gson.fromJson(headJson, Head.class);
                        heads.put(category, head);
                    } catch (Exception e) {
                        e.printStackTrace();
                        HeadIndex.LOGGER.warn("Invalid head: " + headJson);
                    }
                }
            } catch (IOException e) {
                HeadIndex.LOGGER.warn("Failed to load heads from cache", e);
            }
        }

        HeadIndex.LOGGER.info("Finished loading {} heads", heads.size());
        return heads;
    }
}
