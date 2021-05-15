package us.potatoboy.headindex.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public abstract class PagedGui extends SimpleGui {
    public final ServerPlayerEntity player;
    public int page = 0;
    private final Style sel = Style.EMPTY.withItalic(false);
    private final Style nonSel = Style.EMPTY.withItalic(false).withColor(Formatting.DARK_GRAY);

    public PagedGui(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);

        this.player = player;
    }

    abstract int getMaxPage();

    private void updateButtons() {
        this.setSlot(this.size - 8, new GuiElementBuilder(this.page != 0 ? Items.ARROW : Items.BLACK_STAINED_GLASS_PANE)
                .setName(new TranslatableText("spectatorMenu.previous_page").setStyle(this.page != 0 ? sel : nonSel))
                .setCallback((index, type, action) -> {
                    this.page += -1;
                    if (this.page < 0) {
                        this.page = 0;
                    }

                    updatePage();
                }));

        this.setSlot(this.size - 2, new GuiElementBuilder(this.page + 1 < getMaxPage() ? Items.ARROW : Items.BLACK_STAINED_GLASS_PANE)
                .setName(new TranslatableText("spectatorMenu.next_page").setStyle(this.page + 1 < getMaxPage() ? sel : nonSel))
                .setCallback((index, type, action) -> {
                    this.page += 1;
                    if (this.page >= getMaxPage()) {
                        this.page = getMaxPage() - 1;
                    }

                    updatePage();
                }));

        this.setSlot(this.size - 5, new GuiElementBuilder(Items.PAPER)
        .setName(new LiteralText(this.page + 1 + " / " + this.getMaxPage()))
        );
    }

    abstract void onPageChange();

    public void updatePage() {
        onPageChange();
        updateButtons();
    }
}
