package us.potatoboy.headindex.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.apache.commons.codec.binary.Base64;
import us.potatoboy.headindex.HeadIndex;
import us.potatoboy.headindex.api.Head;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
            index = ++index;
        }

        this.setTitle(new TranslatableText("text.headindex.title"));

        this.setSlot(this.getSize() - 1, new GuiElementBuilder()
                .setItem(Items.NAME_TAG)
                .setName(new TranslatableText("text.headindex.search"))
                .setCallback((index1, type1, action) -> {
                    new SearchInputGui().open();
                }));

        this.setSlot(this.getSize() - 2, new GuiElementBuilder()
                .setItem(Items.PLAYER_HEAD)
                .setName(new TranslatableText("text.headindex.playername").setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.WHITE)))
                .setCallback((index1, type1, action) -> {
                    new PlayerInputGui().open();
                }));
    }

    private void addCategoryButton(int index, Head.Category category) {
        this.setSlot(index, category.createStack(), (index1, type1, action) -> {
            new CategoryGui(category).open();
        });
    }

    private class CategoryGui extends PagedGui {
        private final Head.Category category;
        private final ArrayList<Head> heads;

        public CategoryGui(Head.Category category) {
            super(HeadGui.this.player);

            this.category = category;
            this.heads = new ArrayList<>(HeadIndex.heads.get(category));

            this.setSlot(this.size - 9, new GuiElementBuilder(Items.BARRIER)
                    .setName(new TranslatableText("text.headindex.back"))
                    .setCallback((index, type1, action) -> {
                        this.close();
                    })
            );

            this.updatePage();

            this.setTitle(category.getDisplayName());
        }

        @Override
        int getMaxPage() {
            return (int) Math.ceil((double) this.heads.size() / 45);
        }

        @Override
        public void onPageChange() {
            for (int i = 0; i < 45; i++) {
                if (heads.size() > i + (this.page * 45)) {
                    Head head = heads.get(i + (this.page * 45));
                    this.setSlot(i, head.createStack(), (index, type1, action) -> {
                        player.inventory.setCursorStack(head.createStack());
                    });
                }
            }
        }

        @Override
        public void onClose() {
            this.close(false);
            HeadGui.this.open();
        }
    }

    public void openSearch(String search) {
        new SearchGui(search).open();
    }

    private class SearchGui extends PagedGui {
        private final List<Head> heads;
        private final String search;

        public SearchGui(String search) {
            super(HeadGui.this.player);

            this.search = search.toLowerCase();
            heads = HeadIndex.heads.values().stream().filter(head -> head.name.toLowerCase().contains(this.search) || head.getTagsOrEmpty().toLowerCase().contains(this.search)).collect(Collectors.toList());

            this.updatePage();

            this.setSlot(this.size - 9, new GuiElementBuilder(Items.BARRIER)
                    .setName(new TranslatableText("text.headindex.back"))
                    .setCallback((index, type1, action) -> {
                        HeadGui.this.open();
                    })
            );

            this.setTitle(new TranslatableText("text.headindex.search.output", search));
        }

        @Override
        int getMaxPage() {
            return (int) Math.ceil((double) this.heads.size() / 45);
        }

        @Override
        void onPageChange() {
            for (int i = 0; i < 45; i++) {
                if (heads.size() > i + (this.page * 45)) {
                    Head head = heads.get(i + (this.page * 45));
                    this.setSlot(i, head.createStack(), (index, type1, action) -> {
                        player.inventory.setCursorStack(head.createStack());
                    });
                } else {
                    this.setSlot(i, Items.AIR.getDefaultStack());
                }
            }
        }

        @Override
        public void onClose() {
            HeadGui.this.open();
        }
    }

    private class SearchInputGui extends AnvilInputGui {
        private final ItemStack inputStack = Items.NAME_TAG.getDefaultStack();
        private final ItemStack outputStack = Items.SLIME_BALL.getDefaultStack();

        public SearchInputGui() {
            super(HeadGui.this.player, false);

            inputStack.setCustomName(new TranslatableText("text.headindex.search").setStyle(Style.EMPTY.withItalic(false)));
            outputStack.setCustomName(new TranslatableText("text.headindex.search.output").setStyle(Style.EMPTY.withItalic(false)));

            this.setSlot(1, inputStack);

            this.setSlot(2, outputStack, (index, type1, action) -> {
                openSearch(this.getInput());
            });

            this.setDefaultInputValue("");

            this.setTitle(new TranslatableText("text.headindex.search"));
        }

        @Override
        public void onInput(String input) {
            super.onInput(input);
            outputStack.setCustomName(new TranslatableText("text.headindex.search.output", input).setStyle(Style.EMPTY.withItalic(false)));
            this.setSlot(2, outputStack, (index, type1, action) -> {
                openSearch(this.getInput());
            });
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

            inputStack.setCustomName(new TranslatableText("text.headindex.playername").setStyle(Style.EMPTY.withItalic(false)));
            //CompoundTag ownerTag = outputStack.getOrCreateSubTag("SkullOwner");
            //ownerTag.putString("Name", "");

            this.setSlot(1, inputStack);

            this.setSlot(2, outputStack);

            this.setDefaultInputValue("");

            this.setTitle(new TranslatableText("text.headindex.playername"));
        }

        @Override
        public void onTick() {
            if (apiDebounce != 0 && apiDebounce <= System.currentTimeMillis()) {
                apiDebounce = 0;

                CompletableFuture.runAsync(() -> {
                    MinecraftServer server = player.server;

                    GameProfile profile = server.getUserCache().findByName(this.getInput());
                    MinecraftSessionService sessionService = server.getSessionService();

                    profile = sessionService.fillProfileProperties(profile, true);
                    Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = sessionService.getTextures(profile, true);
                    if (!textures.isEmpty()) {
                        MinecraftProfileTexture texture = textures.get(MinecraftProfileTexture.Type.SKIN);

                        CompoundTag ownerTag = outputStack.getOrCreateSubTag("SkullOwner");
                        ownerTag.putUuid("Id", profile.getId());
                        ownerTag.putString("Name", profile.getName());

                        CompoundTag propertiesTag = new CompoundTag();
                        ListTag texturesTag = new ListTag();
                        CompoundTag textureValue = new CompoundTag();

                        textureValue.putString("Value", new String(Base64.encodeBase64(String.format("{\"textures\":{\"SKIN\":{\"url\":\"%s\"}}}", texture.getUrl()).getBytes()), StandardCharsets.UTF_8));

                        texturesTag.add(textureValue);
                        propertiesTag.put("textures", texturesTag);
                        ownerTag.put("Properties", propertiesTag);
                    } else {
                        outputStack.removeSubTag("SkullOwner");
                    }

                    this.setSlot(2, outputStack, (index, type1, action) -> {
                        player.inventory.setCursorStack(outputStack.copy());
                    });
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
