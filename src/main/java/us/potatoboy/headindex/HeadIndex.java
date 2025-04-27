package us.potatoboy.headindex;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import eu.pb4.common.economy.api.CommonEconomy;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import us.potatoboy.headindex.api.Head;
import us.potatoboy.headindex.api.HeadDatabaseAPI;
import us.potatoboy.headindex.commands.HeadCommand;
import us.potatoboy.headindex.config.HeadIndexConfig;

import java.io.File;
import java.util.Comparator;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;

public class HeadIndex implements ModInitializer {
    public static final String MOD_ID = "headindex";
    public static final Logger LOGGER = LogManager.getLogger();
    public static HeadIndexConfig config;
    public static final HeadDatabaseAPI HEAD_DATABASE = new HeadDatabaseAPI();
    public static Multimap<Head.Category, Head> heads = HashMultimap.create();

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> new HeadCommand(dispatcher));

        CompletableFuture.runAsync(() -> heads = HEAD_DATABASE.getHeads());

        config = HeadIndexConfig.loadConfig(new File(FabricLoader.getInstance().getConfigDir() + "/head-index.json"));
    }

    public static void tryPurchase(ServerPlayerEntity player, int amount, Runnable onPurchase) {
        var trueAmount = amount * HeadIndex.config.costAmount;

        switch (HeadIndex.config.economyType) {
            case FREE -> onPurchase.run();
            case TAG -> {
                var stack = new HashSet<ItemVariant>();
                for (int i = 0; i < player.getInventory().size(); i++) {
                    ItemStack slotStack = player.getInventory().getStack(i);
                    if (slotStack.isIn(HeadIndex.config.getCostTag())) {
                        stack.add(ItemVariant.of(slotStack));
                    }
                }
                try (Transaction transaction = Transaction.openOuter()) {
                    long extracted = 0;
                    for (ItemVariant item : stack) {
                        extracted += PlayerInventoryStorage.of(player).extract(item, trueAmount - extracted, transaction);
                        if (extracted >= trueAmount) {
                            break;
                        }
                    }
                    if (extracted == trueAmount) {
                        transaction.commit();
                        onPurchase.run();
                    }
                }
            }
            case ITEM -> {
                try (Transaction transaction = Transaction.openOuter()) {
                    long extracted = PlayerInventoryStorage.of(player).extract(ItemVariant.of(HeadIndex.config.getCostItem()), trueAmount, transaction);
                    if (extracted == trueAmount) {
                        transaction.commit();
                        onPurchase.run();
                    }
                }
            }
            case ECONOMY -> {
                var account = CommonEconomy.getAccounts(player, HeadIndex.config.getCostCurrency(player.server)).stream().min(Comparator.comparing(x -> -x.balance())).orElse(null);

                if (account != null) {
                    var transaction = account.decreaseBalance(trueAmount);
                    if (transaction.isSuccessful()) {
                        onPurchase.run();
                    }
                }
            }
            case LEVEL -> {
                // Cost in experience levels
                if (player.experienceLevel >= trueAmount) {
                    player.addExperienceLevels(-trueAmount);
                    onPurchase.run();
                }
            }
            case LEVELPOINTS -> {
                // Cost in raw experience points
                if (player.totalExperience >= trueAmount) {
                    player.addExperience(-trueAmount);
                    onPurchase.run();
                }
            }
        }
    }
}