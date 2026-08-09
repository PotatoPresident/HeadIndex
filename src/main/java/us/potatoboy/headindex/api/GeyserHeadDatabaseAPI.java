package us.potatoboy.headindex.api;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import us.potatoboy.headindex.HeadIndex;

import java.io.*;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.geysermc.floodgate.api.FloodgateApi;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.google.common.collect.ImmutableMultimap;
import net.minecraft.server.level.ServerPlayer;

public class GeyserHeadDatabaseAPI {
    private static final String GEYSER_GLOBAL_API_SKIN = "https://api.geysermc.org/v2/skin";

    public static boolean isActiveBedrockPlayer(ServerPlayer player) {
        return FloodgateApi.getInstance().isFloodgatePlayer(player.getUUID());
    }

    public static long getXuidFromUuid(UUID uuid) {
        return uuid.getLeastSignificantBits();
    }

    public static Collection<net.minecraft.server.players.NameAndId> getProfilesFromPlayerName(String playerName) {
        var possibleProfile = getProfileFromPlayerName(playerName);
        if (possibleProfile.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singleton(possibleProfile.get());
    }

    public static Optional<net.minecraft.server.players.NameAndId> getProfileFromPlayerName(String playerName) {
        // Strip off Bedrock player prefix
        String prefix = FloodgateApi.getInstance().getPlayerPrefix();
        if (prefix.length() > 0 && !playerName.startsWith(prefix)) {
            return Optional.empty();
        }
        var bedrockName = playerName.substring(prefix.length());

        try {
            var xuid = FloodgateApi.getInstance().getXuidFor(bedrockName).get();  // Warning - may take time
            var uuid = FloodgateApi.getInstance().createJavaPlayerId(xuid);

            return Optional.of(new net.minecraft.server.players.NameAndId(uuid, playerName));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static Optional<com.mojang.authlib.yggdrasil.response.NameAndId> getAuthlibProfileFromPlayerName(String playerName) {
        var result = getProfileFromPlayerName(playerName);
        return result.map(profile -> new com.mojang.authlib.yggdrasil.response.NameAndId(profile.id(), profile.name()));
    }

    public static GameProfile getGameProfileByUuidName(UUID uuid, String name) {
        if (! FloodgateApi.getInstance().isFloodgateId(uuid)) {
            return null;
        }
        var xuid = getXuidFromUuid(uuid);

        String url = String.format("%s/%d", GEYSER_GLOBAL_API_SKIN, xuid);
        var jsonObject = fetchJson(url);
        if (jsonObject == null) {
            return null;
        }

        String value, signature;
        try {
            value = jsonObject.get("value").getAsString();
            signature = jsonObject.get("signature").getAsString();
        } catch (Exception e) {
            return null;
        }

        var props = new PropertyMap(ImmutableMultimap.of("textures", new Property("textures", value, signature)));
        return new GameProfile(uuid, name, props);
    }

    private static JsonObject fetchJson(String urlString) {
        try {
            URLConnection connection = URI.create(urlString).toURL().openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            HeadIndex.LOGGER.info("Fetching: {}", urlString);
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

