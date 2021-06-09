package us.potatoboy.headindex.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import us.potatoboy.headindex.api.Head;

import java.util.UUID;

public abstract class PagedGui extends SimpleGui {
    public final ServerPlayerEntity player;
    public int page = 0;
    private static final Style regular = Style.EMPTY.withItalic(false).withColor(Formatting.WHITE);

    private static final ItemStack backwardArrow = new Head(
            "",
            UUID.fromString("8aa062dc-9852-42b1-ae37-b2f8a3121c0e"),
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzEwODI5OGZmMmIyNjk1MWQ2ODNlNWFkZTQ2YTQyZTkwYzJmN2M3ZGQ0MWJhYTkwOGJjNTg1MmY4YzMyZTU4MyJ9fX0=",
            null
    ).createStack().setCustomName(new TranslatableText("spectatorMenu.previous_page").setStyle(regular));

    private static final ItemStack forwardArrow = new Head(
            null,
            UUID.fromString("8aa062dc-9852-42b1-ae37-b2f8a3121c0e"),
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzg2MTg1YjFkNTE5YWRlNTg1ZjE4NGMzNGYzZjNlMjBiYjY0MWRlYjg3OWU4MTM3OGU0ZWFmMjA5Mjg3In19fQ==",
            null
    ).createStack().setCustomName(new TranslatableText("spectatorMenu.next_page").setStyle(regular));

    public PagedGui(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);

        this.player = player;
    }

    abstract int getMaxPage();

    private void updateButtons() {
        this.setSlot(
                this.size - 6, this.page != 0 ? backwardArrow : Items.BLACK_STAINED_GLASS_PANE.getDefaultStack()
                        .setCustomName(new TranslatableText("spectatorMenu.next_page").setStyle(regular)),
                ((index, type, action) -> {
                    this.page += -1;
                    if (this.page < 0) {
                        this.page = 0;
                    }

                    updatePage();
                })
        );

        this.setSlot(
                this.size - 4, this.page + 1 < getMaxPage() ? forwardArrow : Items.BLACK_STAINED_GLASS_PANE.getDefaultStack()
                        .setCustomName(new TranslatableText("spectatorMenu.next_page").setStyle(regular)),
                ((index, type, action) -> {
                    this.page += 1;
                    if (this.page >= getMaxPage()) {
                        this.page = getMaxPage() - 1;
                    }

                    updatePage();
                })
        );

        this.setSlot(this.size - 5, new GuiElementBuilder(Items.PAPER)
                .setName(new LiteralText(this.page + 1 + " / " + this.getMaxPage()).setStyle(regular))
        );
    }

    abstract void onPageChange();

    public void updatePage() {
        onPageChange();
        updateButtons();
    }
}
