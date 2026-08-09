package us.potatoboy.headindex.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.authlib.yggdrasil.response.NameAndId;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import us.potatoboy.headindex.HeadIndex;
import us.potatoboy.headindex.api.Category;
import us.potatoboy.headindex.api.GeyserHeadDatabaseAPI;
import us.potatoboy.headindex.commands.HIPermissions;
import us.potatoboy.headindex.config.HeadIndexConfig;
import us.potatoboy.headindex.gui.HeadGui;

import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.cumulus.form.CustomForm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class HeadGuiBedrock extends HeadGui {

    public HeadGuiBedrock(ServerPlayer player) {
        super(player);
    }

    @Override
    protected void openSearchInputGui() {
        new SearchInputBedrockGui().open();
    }

    @Override
    protected void openPlayerInputGui() {
        new PlayerInputBedrockGui().open();
    }


    protected class SearchInputBedrockGui {
        public SearchInputBedrockGui() {
        }

        public void open() {
            FloodgateApi.getInstance().sendForm(player.getUUID(),
                CustomForm.builder()
                .title(Component.translatable("text.headindex.search").getString())
                .input(Component.translatable("text.headindex.search.output").getString(), "search")
                .validResultHandler(response -> openSearch(response.next()))
                );
        }
    }

    protected class PlayerInputBedrockGui {
        private CustomForm form;
        public PlayerInputBedrockGui() {
            form = CustomForm.builder()
                .title(Component.translatable("text.headindex.playername").getString())
                .input(Component.translatable("text.headindex.playername.output").getString(), "player")
                .validResultHandler((form, response) -> { executeSearch(response.asInput(0)); } )
                .build();
        }

        public void open() {
            FloodgateApi.getInstance().sendForm(player.getUUID(), form);
        }

        private void executeSearch(String playerName) {
                CompletableFuture.runAsync(() -> {
                    var profile = findPlayerGameProfile(playerName);

                    if (profile == null) {
                        return;
                    }

                    ItemStack outputStack = Items.PLAYER_HEAD.getDefaultInstance();
                    outputStack.set(DataComponents.PROFILE, ResolvableProfile.createResolved(profile));

                    HeadIndex.tryPurchase(player, 1, () -> {
                        getPlayer().getInventory().placeItemBackInInventory(outputStack);
                        });
                });
        }
    }
}
