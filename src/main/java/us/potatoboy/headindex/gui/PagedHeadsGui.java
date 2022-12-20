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
import us.potatoboy.headindex.api.Head;

import java.util.List;
import java.util.UUID;

public class PagedHeadsGui extends LayeredGui {
    public final List<Head> heads;
    public int page = 0;
    final GuiInterface parent;
    final Layer contentLayer;
    final Layer navigationLayer;

    private static final Style regular = Style.EMPTY.withItalic(false).withColor(Formatting.WHITE);

    private static final ItemStack backwardArrow = new Head(
            UUID.fromString("8aa062dc-9852-42b1-ae37-b2f8a3121c0e"),
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzEwODI5OGZmMmIyNjk1MWQ2ODNlNWFkZTQ2YTQyZTkwYzJmN2M3ZGQ0MWJhYTkwOGJjNTg1MmY4YzMyZTU4MyJ9fX0="
    ).createStack().setCustomName(Text.translatable("spectatorMenu.previous_page").setStyle(regular));

    private static final ItemStack forwardArrow = new Head(
            UUID.fromString("8aa062dc-9852-42b1-ae37-b2f8a3121c0e"),
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzg2MTg1YjFkNTE5YWRlNTg1ZjE4NGMzNGYzZjNlMjBiYjY0MWRlYjg3OWU4MTM3OGU0ZWFmMjA5Mjg3In19fQ=="
    ).createStack().setCustomName(Text.translatable("spectatorMenu.next_page").setStyle(regular));

    public PagedHeadsGui(GuiInterface parent, List<Head> heads) {
        super(ScreenHandlerType.GENERIC_9X6, parent.getPlayer(), false);

        this.heads = heads;
        this.parent = parent;

        Layer content = new Layer(5, 9);
        this.contentLayer = content;
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
                3, (this.page != 0 ? backwardArrow : Items.BLACK_STAINED_GLASS_PANE.getDefaultStack())
                        .setCustomName(Text.translatable("spectatorMenu.previous_page").setStyle(regular)),
                ((index, type, action) -> {
                    this.page -= 1;
                    if (this.page < 0) {
                        this.page = 0;
                    }

                    updatePage();
                })
        );

        navigationLayer.setSlot(
                5, (this.page + 1 < getMaxPage() ? forwardArrow : Items.BLACK_STAINED_GLASS_PANE.getDefaultStack())
                        .setCustomName(Text.translatable("spectatorMenu.next_page").setStyle(regular)),
                ((index, type, action) -> {
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
                contentLayer.setSlot(i, head.createStack(), (index, type, action) -> processHeadClick(head, type));
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
                player.getInventory().insertStack(headStack);
            } else if (type.isMiddle) {
                headStack.setCount(headStack.getMaxCount());
                player.currentScreenHandler.setCursorStack(headStack);
            } else {
                player.currentScreenHandler.setCursorStack(headStack);
            }
        } else if (headStack.isItemEqual(cursorStack) && ItemStack.areNbtEqual(headStack, cursorStack)) {
            if (type.isLeft) {
                cursorStack.increment(1);
            } else if (type.isRight) {
                cursorStack.decrement(1);
            } else if (type.isMiddle) {
                headStack.setCount(headStack.getMaxCount());
                player.currentScreenHandler.setCursorStack(headStack);
            }
        } else {
            player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
        }
    }

    @Override
    public void onClose() {
        parent.open();
    }
}
