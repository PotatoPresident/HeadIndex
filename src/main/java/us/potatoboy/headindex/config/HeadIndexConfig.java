package us.potatoboy.headindex.config;

import com.google.gson.*;
import eu.pb4.common.economy.api.CommonEconomy;
import eu.pb4.common.economy.api.EconomyCurrency;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public class HeadIndexConfig {
	private static final Gson GSON = new GsonBuilder().registerTypeAdapter(Identifier.class, new IdentifierSerializer()).setPrettyPrinting().create();

	public enum EconomyType {
		ITEM,
		ECONOMY,
		FREE
	}
	
	public int permissionLevel = 2;
	
	public EconomyType economyType = EconomyType.FREE;
	public Identifier costType = new Identifier("minecraft", "diamond");
	public int costAmount = 1;

	public Text getCost(MinecraftServer server) {
		return switch (economyType) {
			case ITEM -> Text.empty().append(getCostItem().getName()).append(Text.of(" × " + costAmount));
			case ECONOMY -> getCostCurrency(server).formatValueText(costAmount, false);
			case FREE -> Text.empty();
		};
	}

	public Item getCostItem() {
		return Registries.ITEM.get(costType);
	}

	public EconomyCurrency getCostCurrency(MinecraftServer server) {
		return CommonEconomy.getCurrency(server, costType);
	}
	
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

	public void saveConfig(File config) {
		try (
				FileOutputStream stream = new FileOutputStream(config);
				Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)
		) {
			GSON.toJson(this, writer);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load config", e);
		}
	}

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
