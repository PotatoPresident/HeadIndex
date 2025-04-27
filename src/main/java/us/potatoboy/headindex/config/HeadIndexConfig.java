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

	// Permission level required to use the feature
	public int permissionLevel = 2;

	// Type of economy to use
	public EconomyType economyType = EconomyType.FREE;

	// If using ITEM or TAG or ECONOMY, this identifies the currency/item/tag
	public Identifier costType = Identifier.of("minecraft", "diamond");

	// Amount of the cost
	public int costAmount = 1;

	/**
	 * Returns a Text component describing the cost based on the economy type.
	 */
	public Text getCost(MinecraftServer server) {
		switch (economyType) {
			case TAG:
				return Text.translatable(getCostTag().getTranslationKey())
						.append(Text.of(" × " + costAmount));
			case ITEM:
				return Text.empty()
						.append(getCostItem().getName())
						.append(Text.of(" × " + costAmount));
			case ECONOMY:
				return getCostCurrency(server)
						.formatValueText(costAmount, false);
			case LEVEL:
				// Cost in experience levels
				return Text.of(costAmount + " Levels");
			case LEVELPOINTS:
				// Cost in raw XP points
				return Text.of(costAmount + " XP");
			case FREE:
			default:
				return Text.empty();
		}
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

	/**
	 * Loads the configuration from the given file, or creates a default if missing.
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
