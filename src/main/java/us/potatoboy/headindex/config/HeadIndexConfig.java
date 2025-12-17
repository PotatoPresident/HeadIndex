package us.potatoboy.headindex.config;

import com.google.gson.*;
import eu.pb4.common.economy.api.CommonEconomy;
import eu.pb4.common.economy.api.EconomyCurrency;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class HeadIndexConfig {
	private static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(Identifier.class, new IdentifierSerializer())
			.setPrettyPrinting()
			.create();

	public enum EconomyType {
		TAG,
		ITEM,
		ECONOMY,
		FREE,
		LEVEL,
		LEVELPOINTS
	}
    
    private final String _licenseComment = "Enter your license key below. Register here: https://minecraft-heads.com/wiki/minecraft-heads/api-v2-for-users";
    public String license = "LICENSE_HERE";

    private final String _permissionComment = "The default permission level for the commands. Set to 0 to allow all players access";
	public int permissionLevel = 2;

    private final String _economyComment = "The type of economy to use. Set to FREE to disable economy, ITEM to use an item, TAG to use a tag, ECONOMY to use an economy currency, LEVEL to use minecraft levels, or LEVELPOINTS to use level points";
	public EconomyType economyType = EconomyType.FREE;

    private final String _costComment = "The identifier for the item, tag or currency to use for the cost, only needed if economyType is set to ITEM, TAG, or ECONOMY";
	public Identifier costType = Identifier.of("minecraft", "diamond");

    private final String _costAmountComment = "The amount of the item, currency or level to use for the cost";
	public int costAmount = 1;

	/**
	 * Returns a Text component describing the cost based on the economy type.
	 */
	public Text getCost(MinecraftServer server) {
        return switch (economyType) {
            case TAG -> Text.translatable(getCostTag().getTranslationKey())
                    .append(Text.of(" × " + costAmount));
            case ITEM -> Text.empty()
                    .append(getCostItem().getName())
                    .append(Text.of(" × " + costAmount));
            case ECONOMY -> getCostCurrency(server)
                    .formatValueText(costAmount, false);
            case LEVEL ->
                // Cost in experience levels
                    Text.translatable("text.headindex.xp.levels", costAmount);
            case LEVELPOINTS ->
                // Cost in raw XP points
                    Text.translatable("text.headindex.xp.points", costAmount);
            default -> Text.empty();
        };
	}

	/** Get the configured Item for ITEM cost type */
	public Item getCostItem() {
		return Registries.ITEM.get(costType);
	}

	/** Get the configured EconomyCurrency for ECONOMY cost type */
	public EconomyCurrency getCostCurrency(MinecraftServer server) {
		return CommonEconomy.getCurrency(server, costType);
	}

	/** Get the configured TagKey for TAG cost type */
	public TagKey<Item> getCostTag() {
		return TagKey.of(Registries.ITEM.getKey(), costType);
	}
    
    /** Use demo mode if the license has not been set */
    public boolean demoMode() {
        return Objects.equals(license, "LICENSE_HERE");
    }

	/**
	 * Loads the configuration from the given file or creates a default if missing.
	 */
	public static HeadIndexConfig loadConfig(File file) {
		HeadIndexConfig config;

		if (file.exists() && file.isFile()) {
			try (
					FileInputStream fileInputStream = new FileInputStream(file);
					InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
					BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
			) {
				config = GSON.fromJson(bufferedReader, HeadIndexConfig.class);
			} catch (IOException e) {
				throw new RuntimeException("Failed to load config", e);
			}
		} else {
			config = new HeadIndexConfig();
		}

		config.saveConfig(file);
		return config;
	}

	/**
	 * Saves the current configuration to the given file.
	 */
	public void saveConfig(File config) {
		try (
				FileOutputStream stream = new FileOutputStream(config);
				Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)
		) {
			GSON.toJson(this, writer);
		} catch (IOException e) {
			throw new RuntimeException("Failed to save config", e);
		}
	}

	/**
	 * Serializer and deserializer for Minecraft Identifier
	 */
	public static class IdentifierSerializer implements JsonSerializer<Identifier>, JsonDeserializer<Identifier> {
		@Override
		public JsonElement serialize(Identifier src, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(src.toString());
		}

		@Override
		public Identifier deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			return Identifier.tryParse(json.getAsString());
		}
	}
}
