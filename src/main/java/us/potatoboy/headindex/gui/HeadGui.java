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

import net.fabricmc.loader.api.FabricLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class HeadGui extends SimpleGui {
    private final ServerPlayer player;
    private final boolean hasFloodgate = FabricLoader.getInstance().isModLoaded("floodgate");

    public HeadGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x2, player, false);

        this.player = player;

        int index = 0;
        var categories = HeadIndex.heads.keySet().stream().sorted().toList();
        for (Category category : categories) {
            addCategoryButton(index, category);
            ++index;
        }

        this.setTitle(Component.translatable("text.headindex.title"));

        if (HIPermissions.COMMAND_SEARCH.test(player.createCommandSourceStack())) {
            this.setSlot(this.getSize() - 1, new GuiElementBuilder()
                    .setItem(Items.NAME_TAG)
                    .setName(Component.translatable("text.headindex.search").setStyle(Style.EMPTY.withItalic(false)))
                    .setCallback((index1, type1, action, gui) -> {
                        this.close();
                        new SearchInputGui().open();
                    }));
        }

        if (HIPermissions.COMMAND_PLAYER.test(player.createCommandSourceStack())) {
            this.setSlot(this.getSize() - 2, new GuiElementBuilder()
                    .setItem(Items.PLAYER_HEAD)
                    .setName(Component.translatable("text.headindex.playername").setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.WHITE)))
                    .setCallback((index1, type1, action, gui) -> {
                        this.close();
                        new PlayerInputGui().open();
                    }));
        }
    }

    private void addCategoryButton(int index, Category category) {
        var icon = category.getCategoryIcon().createStack(category.getDisplayName());
        icon.remove(DataComponents.LORE);
        
        this.setSlot(index, icon, (i, type, action, gui) -> {
            this.close();
            var headsGui = new PagedHeadsGui(this, new ArrayList<>(HeadIndex.heads.get(category)));
            headsGui.setTitle(category.getDisplayName());
            headsGui.open();
        });
    }

    public void openSearch(String search) {
        this.close();
        var heads = HeadIndex.heads.values().stream().flatMap(Collection::stream)
                .filter(head -> head.name.toLowerCase().contains(search.toLowerCase()) || head.getTags().stream()
                        .anyMatch(name -> name.toLowerCase().contains(search.toLowerCase())))
                .collect(Collectors.toList());

        var headsGui = new PagedHeadsGui(this, heads);
        headsGui.setTitle(Component.translatable("text.headindex.search.output", search));
        headsGui.open();
    }

    private class SearchInputGui extends AnvilInputGui {
        private final ItemStack inputStack = Items.NAME_TAG.getDefaultInstance();
        private final ItemStack outputStack = Items.SLIME_BALL.getDefaultInstance();

        public SearchInputGui() {
            super(HeadGui.this.player, false);

            inputStack.set(DataComponents.CUSTOM_NAME, Component.translatable("text.headindex.search").setStyle(Style.EMPTY.withItalic(false)));
            outputStack.set(DataComponents.CUSTOM_NAME, Component.translatable("text.headindex.search.output").setStyle(Style.EMPTY.withItalic(false)));

            this.setSlot(1, inputStack);

            this.setSlot(2, outputStack, (index, type, action, gui) -> openSearch(this.getInput()));
            this.setDefaultInputValue("");

            this.setTitle(Component.translatable("text.headindex.search"));
        }

        @Override
        public void onInput(String input) {
            super.onInput(input);
            outputStack.set(DataComponents.CUSTOM_NAME, Component.translatable("text.headindex.search.output", input).setStyle(Style.EMPTY.withItalic(false)));
            this.setSlot(2, outputStack, (index, type, action, gui) -> openSearch(this.getInput()));
        }

//        @Override
//        public void onRemoved() {
//            HeadGui.this.open();
//        }
    }

    private class PlayerInputGui extends AnvilInputGui {
        private final ItemStack inputStack = Items.PLAYER_HEAD.getDefaultInstance();
        private final ItemStack outputStack = Items.PLAYER_HEAD.getDefaultInstance();

        private long apiDebounce = 0;

        public PlayerInputGui() {
            super(HeadGui.this.player, false);

            inputStack.set(DataComponents.CUSTOM_NAME, Component.translatable("text.headindex.playername").setStyle(Style.EMPTY.withItalic(false)));

            this.setSlot(1, inputStack);

            this.setSlot(2, outputStack);

            this.setDefaultInputValue("");

            this.setTitle(Component.translatable("text.headindex.playername"));
        }

        @Override
        public void onTick() {
            if (apiDebounce != 0 && apiDebounce <= System.currentTimeMillis()) {
                apiDebounce = 0;

                CompletableFuture.runAsync(() -> {
                    MinecraftServer server = player.level().getServer();

                    Optional<NameAndId> possibleProfile = server.services().profileRepository().findProfileByName(this.getInput());
                    MinecraftSessionService sessionService = server.services().sessionService();

                    if (possibleProfile.isEmpty() && hasFloodgate) {
                        possibleProfile = GeyserHeadDatabaseAPI.getAuthlibProfileFromPlayerName(this.getInput());
                    }
                    if (possibleProfile.isEmpty()) {
                        outputStack.remove(DataComponents.PROFILE);
                        return;
                    }

                    var authlibProfile = possibleProfile.get();
                    ProfileResult profileResult = sessionService.fetchProfile(authlibProfile.id(), false);

                    GameProfile profile = null;
                    if (profileResult != null) {
                        profile = profileResult.profile();
                    }

                    if (profile == null && hasFloodgate) {
                        profile = GeyserHeadDatabaseAPI.getGameProfileByUuidName(authlibProfile.id(), authlibProfile.name());
                    }

                    if (profile == null) {
                        outputStack.remove(DataComponents.PROFILE);
                    } else {
                        outputStack.set(DataComponents.PROFILE, ResolvableProfile.createResolved(profile));
                    }

                    var builder = GuiElementBuilder.from(outputStack);
                    if (HeadIndex.config.economyType != HeadIndexConfig.EconomyType.FREE) {
                        builder.addLoreLine(Component.empty());
                        builder.addLoreLine(Component.translatable("text.headindex.price", HeadIndex.config.getCost(server)).withStyle(style -> style.withColor(ChatFormatting.RED)));
                    }

                    this.setSlot(2, builder.asStack(), (index, type, action, gui) ->
                            HeadIndex.tryPurchase(player, 1, () -> {
                                var cursorStack = getPlayer().containerMenu.getCarried();
                                if (player.containerMenu.getCarried().isEmpty()) {
                                    player.containerMenu.setCarried(outputStack.copy());
                                } else if (ItemStack.isSameItem(outputStack, cursorStack) && cursorStack.getCount() < cursorStack.getMaxStackSize()) {
                                    cursorStack.grow(1);
                                } else {
                                    player.drop(outputStack.copy(), false);
                                }
                            })
                    );
                });
            }
        }

        @Override
        public void onInput(String input) {
            super.onInput(input);

            apiDebounce = System.currentTimeMillis() + 500;
        }

//        @Override
//        public void onRemoved() {
//            HeadGui.this.open();
//        }
    }
}
