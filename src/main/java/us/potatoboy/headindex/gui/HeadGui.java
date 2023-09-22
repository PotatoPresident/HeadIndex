package us.potatoboy.headindex.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import us.potatoboy.headindex.HeadIndex;
import us.potatoboy.headindex.api.Head;
import us.potatoboy.headindex.config.HeadIndexConfig;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class HeadGui extends SimpleGui {
    private final ServerPlayerEntity player;

    public HeadGui(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X2, player, false);

        this.player = player;

        int index = 0;
        for (Head.Category category : Head.Category.values()) {
            addCategoryButton(index, category);
            ++index;
        }

        this.setTitle(Text.translatable("text.headindex.title"));

        if (Permissions.check(player, "headindex.search", HeadIndex.config.permissionLevel)) {
            this.setSlot(this.getSize() - 1, new GuiElementBuilder()
                    .setItem(Items.NAME_TAG)
                    .setName(Text.translatable("text.headindex.search").setStyle(Style.EMPTY.withItalic(false)))
                    .setCallback((index1, type1, action) -> {
                        this.close();
                        new SearchInputGui().open();
                    }));
        }

        if (Permissions.check(player, "headindex.playername", HeadIndex.config.permissionLevel)) {
            this.setSlot(this.getSize() - 2, new GuiElementBuilder()
                    .setItem(Items.PLAYER_HEAD)
                    .setName(Text.translatable("text.headindex.playername").setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.WHITE)))
                    .setCallback((index1, type1, action) -> {
                        this.close();
                        new PlayerInputGui().open();
                    }));
        }
    }

    private void addCategoryButton(int index, Head.Category category) {
        this.setSlot(index, category.createStack(), (i, type, action, gui) -> {
            this.close();
            var headsGui = new PagedHeadsGui(this, new ArrayList<>(HeadIndex.heads.get(category)));
            headsGui.setTitle(category.getDisplayName());
            headsGui.open();
        });
    }

    public void openSearch(String search) {
        this.close();
        var heads = HeadIndex.heads.values().stream()
                .filter(head -> head.name.toLowerCase().contains(search.toLowerCase()) || head.getTagsOrEmpty().toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toList());

        var headsGui = new PagedHeadsGui(this, heads);
        headsGui.setTitle(Text.translatable("text.headindex.search.output", search));
        headsGui.open();
    }

    private class SearchInputGui extends AnvilInputGui {
        private final ItemStack inputStack = Items.NAME_TAG.getDefaultStack();
        private final ItemStack outputStack = Items.SLIME_BALL.getDefaultStack();

        public SearchInputGui() {
            super(HeadGui.this.player, false);

            inputStack.setCustomName(Text.translatable("text.headindex.search").setStyle(Style.EMPTY.withItalic(false)));
            outputStack.setCustomName(Text.translatable("text.headindex.search.output").setStyle(Style.EMPTY.withItalic(false)));

            this.setSlot(1, inputStack);

            this.setSlot(2, outputStack, (index, type, action, gui) -> openSearch(this.getInput()));
            this.setDefaultInputValue("");

            this.setTitle(Text.translatable("text.headindex.search"));
        }

        @Override
        public void onInput(String input) {
            super.onInput(input);
            outputStack.setCustomName(Text.translatable("text.headindex.search.output", input).setStyle(Style.EMPTY.withItalic(false)));
            this.setSlot(2, outputStack, (index, type, action, gui) -> openSearch(this.getInput()));
        }

        @Override
        public void onClose() {
            HeadGui.this.open();
        }
    }

    private class PlayerInputGui extends AnvilInputGui {
        private final ItemStack inputStack = Items.PLAYER_HEAD.getDefaultStack();
        private final ItemStack outputStack = Items.PLAYER_HEAD.getDefaultStack();

        private long apiDebounce = 0;

        public PlayerInputGui() {
            super(HeadGui.this.player, false);

            inputStack.setCustomName(Text.translatable("text.headindex.playername").setStyle(Style.EMPTY.withItalic(false)));

            this.setSlot(1, inputStack);

            this.setSlot(2, outputStack);

            this.setDefaultInputValue("");

            this.setTitle(Text.translatable("text.headindex.playername"));
        }

        @Override
        public void onTick() {
            if (apiDebounce != 0 && apiDebounce <= System.currentTimeMillis()) {
                apiDebounce = 0;

                CompletableFuture.runAsync(() -> {
                    MinecraftServer server = player.server;

                    Optional<GameProfile> possibleProfile = server.getUserCache().findByName(this.getInput());
                    MinecraftSessionService sessionService = server.getSessionService();

                    if (possibleProfile.isEmpty()) {
                        outputStack.removeSubNbt("SkullOwner");
                        return;
                    }

                    ProfileResult profileResult = sessionService.fetchProfile(possibleProfile.get().getId(), false);
                    if (profileResult == null) {
                        outputStack.removeSubNbt("SkullOwner");
                        return;
                    }

                    GameProfile profile = profileResult.profile();
                    Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = sessionService.getTextures(profile, false);

                    if (textures.isEmpty()) {
                        outputStack.removeSubNbt("SkullOwner");
                        return;
                    }

                    MinecraftProfileTexture texture = textures.get(MinecraftProfileTexture.Type.SKIN);

                    NbtCompound ownerTag = outputStack.getOrCreateSubNbt("SkullOwner");
                    ownerTag.putUuid("Id", profile.getId());
                    ownerTag.putString("Name", profile.getName());

                    NbtCompound propertiesTag = new NbtCompound();
                    NbtList texturesTag = new NbtList();
                    NbtCompound textureValue = new NbtCompound();

                    textureValue.putString("Value", new String(Base64.getEncoder().encode(String.format("{\"textures\":{\"SKIN\":{\"url\":\"%s\"}}}", texture.getUrl()).getBytes()), StandardCharsets.UTF_8));

                    texturesTag.add(textureValue);
                    propertiesTag.put("textures", texturesTag);
                    ownerTag.put("Properties", propertiesTag);
                    
                    var builder = GuiElementBuilder.from(outputStack);
                    if (HeadIndex.config.economyType != HeadIndexConfig.EconomyType.FREE) {
                        builder.addLoreLine(Text.empty());
                        builder.addLoreLine(Text.translatable("text.headindex.price", HeadIndex.config.getCost(getPlayer().server)).styled(style -> style.withColor(Formatting.RED)));
                    }

                    this.setSlot(2, builder.asStack(), (index, type, action, gui) ->
                            HeadIndex.tryPurchase(player, 1, () -> {
                                var cursorStack = getPlayer().currentScreenHandler.getCursorStack();
                                if (player.currentScreenHandler.getCursorStack().isEmpty()) {
                                    player.currentScreenHandler.setCursorStack(outputStack.copy());
                                } else if (ItemStack.canCombine(outputStack, cursorStack) && cursorStack.getCount() < cursorStack.getMaxCount()) {
                                    cursorStack.increment(1);
                                } else {
                                    player.dropItem(outputStack.copy(), false);
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

        @Override
        public void onClose() {
            HeadGui.this.open();
        }
    }
}
