package us.potatoboy.headindex;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import us.potatoboy.headindex.api.Head;
import us.potatoboy.headindex.api.HeadDatabaseAPI;
import us.potatoboy.headindex.commands.HeadCommand;

import java.util.concurrent.CompletableFuture;

public class HeadIndex implements ModInitializer {
    public static final String MOD_ID = "headindex";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final HeadDatabaseAPI HEAD_DATABASE = new HeadDatabaseAPI();
    public static Multimap<Head.Category, Head> heads = HashMultimap.create();

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            new HeadCommand(dispatcher);
        });

        CompletableFuture.runAsync(() -> {
            heads = HEAD_DATABASE.getHeads();
        });
    }
}
