package us.potatoboy.headindex.api;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;
import us.potatoboy.headindex.HeadIndex;

import java.io.*;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class HeadDatabaseAPI {
    private static final String BASE_API = "https://minecraft-heads.com/api/heads";
    private static final String APP_UUID = "0f067fe4-b590-4277-8936-a4ca1e384f94";

    private final Path cacheDir = FabricLoader.getInstance().getConfigDir().resolve("headindex-cache");
    private final Path categoriesFile = cacheDir.resolve("categories.json");
    private final Path tagsFile = cacheDir.resolve("tags.json");
    private final Gson gson = new Gson();

    private final Map<Integer, String> tagCache = new HashMap<>();
    
    public Map<Category, List<Head>> getHeads() {
        try {
            updateTags();
            updateCategories();
            updateHeads();
        } catch (Exception e) {
            HeadIndex.LOGGER.warn("API sync failed, switching to offline mode: {}", e.getMessage());
        }

        loadTagsFromDisk();
        return loadHeadsFromDisk();
    }
    
    @Nullable
    public String getTagName(int id) {
        return tagCache.get(id);
    }
    
    private void updateTags() throws IOException {
        HeadIndex.LOGGER.info("Syncing tags...");
        Files.createDirectories(cacheDir);

        String url = String.format("%s/tags?&app_uuid=%s", BASE_API, APP_UUID);
        if (HeadIndex.config.demoMode()) url += "&demo=true";
        JsonObject response = fetchJson(url);

        if (response != null && response.has("data")) {
            try (FileWriter writer = new FileWriter(tagsFile.toFile())) {
                gson.toJson(response.getAsJsonArray("data"), writer);
            }
        }
    }

    private void loadTagsFromDisk() {
        if (!Files.exists(tagsFile)) return;

        try (Reader reader = new FileReader(tagsFile.toFile())) {
            Tag[] tags = gson.fromJson(reader, Tag[].class);
            tagCache.clear();
            for (Tag tag : tags) {
                tagCache.put(tag.id, tag.name);
            }
            HeadIndex.LOGGER.info("Loaded {} tags into memory.", tagCache.size());
        } catch (IOException e) {
            HeadIndex.LOGGER.error("Failed to load local tags", e);
        }
    }
    
    private List<Category> updateCategories() throws IOException {
        HeadIndex.LOGGER.info("Syncing categories...");
        String url = String.format("%s/categories?&app_uuid=%s", BASE_API, APP_UUID);
        if (HeadIndex.config.demoMode()) url += "&demo=true";
        JsonObject response = fetchJson(url);

        if (response != null && response.has("data")) {
            try (FileWriter writer = new FileWriter(categoriesFile.toFile())) {
                gson.toJson(response.getAsJsonArray("data"), writer);
            }
        }
        return loadCategoriesFromDisk();
    }
    
    private void updateHeads() throws IOException {
        List<Category> categories = loadCategoriesFromDisk();

        for (Category category : categories) {
            HeadIndex.LOGGER.info("Syncing category: {}", category.name);
            List<Head> allHeads = new ArrayList<>();
            int page = 1;
            boolean hasMore = true;

            while (hasMore) {
                String url = String.format(
                        "%s/custom-heads?&category_id=%d&value=true&uuid=true&page=%d&app_uuid=%s",
                        BASE_API, category.id, page, APP_UUID
                );
                
                if (HeadIndex.config.demoMode()) url += "&demo=true";

                JsonObject response = fetchJson(url);
                if (response == null || !response.has("data")) break;

                JsonArray data = response.getAsJsonArray("data");
                JsonObject meta = response.getAsJsonObject("meta");
                if (data.isEmpty() || meta.get("data_limited").getAsBoolean()) {
                    hasMore = false;
                } else {
                    for (JsonElement e : data) allHeads.add(gson.fromJson(e, Head.class));

                    if (data.size() < 10000) hasMore = false;
                    else page++;
                }
            }

            if (!allHeads.isEmpty()) {
                File file = cacheDir.resolve(category.getFileName() + ".json").toFile();
                FileUtils.writeStringToFile(file, gson.toJson(allHeads), StandardCharsets.UTF_8);
            }
        }
    }
    
    private Map<Category, List<Head>> loadHeadsFromDisk() {
        Map<Category, List<Head>> map = new HashMap<>();
        try {
            List<Category> categories = loadCategoriesFromDisk();
            for (Category category : categories) {
                File file = cacheDir.resolve(category.getFileName() + ".json").toFile();
                if (file.exists()) {
                    try (Reader reader = new FileReader(file)) {
                        Head[] heads = gson.fromJson(reader, Head[].class);
                        map.put(category, Arrays.stream(heads).sorted().toList());
                    }
                }
            }
        } catch (IOException e) {
            HeadIndex.LOGGER.error("Failed to load local head cache", e);
        }
        return map;
    }

    private List<Category> loadCategoriesFromDisk() throws IOException {
        if (!Files.exists(categoriesFile)) return new ArrayList<>();
        try (Reader reader = new FileReader(categoriesFile.toFile())) {
            Category[] cats = gson.fromJson(reader, Category[].class);
            return List.of(cats);
        }
    }

    private JsonObject fetchJson(String urlString) {
        try {
            URLConnection connection = URI.create(urlString).toURL().openConnection();
            connection.setRequestProperty("api-key", HeadIndex.config.license);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            try (InputStreamReader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
                var response = JsonParser.parseReader(reader).getAsJsonObject();
                if (response.has("warnings")) {
                    response.getAsJsonArray("warnings").asList().forEach(HeadIndex.LOGGER::warn);
                }
                return response;
            }
        } catch (IOException e) {
            HeadIndex.LOGGER.warn("Fetch failed: {} {}", urlString, e.getMessage());
            return null;
        }
    }
}
