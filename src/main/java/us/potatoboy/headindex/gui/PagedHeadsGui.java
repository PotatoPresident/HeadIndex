package us.potatoboy.headindex.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.GuiInterface;
import eu.pb4.sgui.api.gui.layered.Layer;
import eu.pb4.sgui.api.gui.layered.LayeredGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import us.potatoboy.headindex.HeadIndex;
import us.potatoboy.headindex.api.Head;
import us.potatoboy.headindex.config.HeadIndexConfig;

import java.util.List;

public class PagedHeadsGui extends LayeredGui {
    public final List<Head> heads;
    public int page = 0;
    final GuiInterface parent;
    final Layer contentLayer;
    final Layer navigationLayer;

    private static final Style regular = Style.EMPTY.withItalic(false).withColor(Formatting.WHITE);

    private static final ItemStack backwardArrow = new Head(
            "8aa062dc-9852-42b1-ae37-b2f8a3121c0e",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzEwODI5OGZmMmIyNjk1MWQ2ODNlNWFkZTQ2YTQyZTkwYzJmN2M3ZGQ0MWJhYTkwOGJjNTg1MmY4YzMyZTU4MyJ9fX0="
    ).createStack(Text.translatable("spectatorMenu.previous_page").setStyle(regular));

    private static final ItemStack forwardArrow = new Head(
            "8aa062dc-9852-42b1-ae37-b2f8a3121c0e",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzg2MTg1YjFkNTE5YWRlNTg1ZjE4NGMzNGYzZjNlMjBiYjY0MWRlYjg3OWU4MTM3OGU0ZWFmMjA5Mjg3In19fQ=="
    ).createStack(Text.translatable("spectatorMenu.next_page").setStyle(regular));

    public PagedHeadsGui(GuiInterface parent, List<Head> heads) {
        super(ScreenHandlerType.GENERIC_9X6, parent.getPlayer(), false);

        this.heads = heads;
        this.parent = parent;

        this.contentLayer = new Layer(5, 9);
        updateContent();
        this.addLayer(contentLayer, 0, 0);

        Layer navigation = new Layer(1, 9);
        this.navigationLayer = navigation;
        navigation.setSlot(0, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.translatable("text.headindex.back"))
                .setCallback((index, type, action) -> this.close())
        );
        updateNavigation();
        this.addLayer(navigationLayer, 0, 5);
    }

    private int getMaxPage() {
        return Math.max(1, (int) Math.ceil((double) this.heads.size() / 45));
    }

    private void updateNavigation() {
        navigationLayer.setSlot(
                3, GuiElementBuilder
                        .from(this.page != 0 ? backwardArrow : Items.BLACK_STAINED_GLASS_PANE.getDefaultStack())
                        .setName(Text.translatable("spectatorMenu.previous_page").setStyle(regular))
                        .setCallback((index, type, action) -> {
                            this.page -= 1;
                            if (this.page < 0) {
                                this.page = 0;
                            }

                            updatePage();
                        })
        );

        navigationLayer.setSlot(
                5, GuiElementBuilder
                        .from(this.page + 1 < getMaxPage() ? forwardArrow : Items.BLACK_STAINED_GLASS_PANE.getDefaultStack())
                        .setName(Text.translatable("spectatorMenu.next_page").setStyle(regular))
                        .setCallback((index, type, action) -> {
                            this.page += 1;
                            if (this.page >= getMaxPage()) {
                                this.page = getMaxPage() - 1;
                            }

                            updatePage();
                        })
        );

        navigationLayer.setSlot(4, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal(this.page + 1 + " / " + this.getMaxPage()).setStyle(regular))
        );
    }

    private void updateContent() {
        for (int i = 0; i < 45; i++) {
            if (heads.size() > i + (this.page * 45)) {
                Head head = heads.get(i + (this.page * 45));
				var builder = GuiElementBuilder.from(head.createStack());
				if (HeadIndex.config.economyType != HeadIndexConfig.EconomyType.FREE) {
                    builder.addLoreLine(Text.empty());
					builder.addLoreLine(Text.translatable("text.headindex.price", HeadIndex.config.getCost(getPlayer().getEntityWorld().getServer())).styled(style -> style.withColor(Formatting.RED)));
				}

                contentLayer.setSlot(i, builder.asStack(), (index, type, action) -> processHeadClick(head, type));
            } else {
                contentLayer.setSlot(i, Items.AIR.getDefaultStack());
            }
        }
    }

    public void updatePage() {
        updateNavigation();
        updateContent();
    }

    private void processHeadClick(Head head, ClickType type) {
        var player = getPlayer();

        ItemStack cursorStack = getPlayer().currentScreenHandler.getCursorStack();
        ItemStack headStack = head.createStack();

        if (cursorStack.isEmpty()) {
            if (type.shift) {
                HeadIndex.tryPurchase(player, 1, () -> player.getInventory().insertStack(headStack));
            } else if (type.isMiddle) {
				HeadIndex.tryPurchase(player, headStack.getMaxCount(), () -> {
					headStack.setCount(headStack.getMaxCount());
					player.currentScreenHandler.setCursorStack(headStack);
				});
            } else {
				HeadIndex.tryPurchase(player, 1, () -> player.currentScreenHandler.setCursorStack(headStack));
            }
        } else if (cursorStack.getMaxCount() <= cursorStack.getCount()) {
			return;
		} else if (ItemStack.areItemsEqual(headStack, cursorStack)) {
            if (type.isLeft) {
				HeadIndex.tryPurchase(player, 1, () -> cursorStack.increment(1));
            } else if (type.isRight) {
				if (HeadIndex.config.economyType == HeadIndexConfig.EconomyType.FREE) cursorStack.decrement(1);
            } else if (type.isMiddle) {
				var amount = headStack.getMaxCount() - cursorStack.getCount();
				HeadIndex.tryPurchase(player, amount, () -> {
					headStack.setCount(headStack.getMaxCount());
					player.currentScreenHandler.setCursorStack(headStack);
				});
            }
        } else {
			if (HeadIndex.config.economyType == HeadIndexConfig.EconomyType.FREE) player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
        }
    }

    @Override
    public void onClose() {
        parent.open();
    }
}
