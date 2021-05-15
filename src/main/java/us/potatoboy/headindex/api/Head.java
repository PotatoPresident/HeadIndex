package us.potatoboy.headindex.api;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.UUID;

public class Head {
    public final String name;
    public final UUID uuid;
    public final String value;
    public final String tags;

    public Head(String name, UUID uuid, String value, String tags) {
        this.name = name;
        this.uuid = uuid;
        this.value = value;
        this.tags = tags;
    }

    public String getTagsOrEmpty() {
        return tags == null ? "" : tags;
    }

    public ItemStack createStack() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        stack.setCustomName(new LiteralText(name));

        CompoundTag displayTag = stack.getOrCreateSubTag("display");
        ListTag loreTag = new ListTag();

        loreTag.add(StringTag.of(Text.Serializer.toJson(new LiteralText(tags))));

        displayTag.put("Lore", loreTag);

        CompoundTag ownerTag = stack.getOrCreateSubTag("SkullOwner");
        ownerTag.putUuid("Id", uuid);

        CompoundTag propertiesTag = new CompoundTag();
        ListTag texturesTag = new ListTag();
        CompoundTag textureValue = new CompoundTag();

        textureValue.putString("Value", value);

        texturesTag.add(textureValue);
        propertiesTag.put("textures", texturesTag);
        ownerTag.put("Properties", propertiesTag);

        return stack;
    }

    public enum Category {
        ALPHABET("alphabet", Items.STONE),
        ANIMALS("animals", Items.STONE),
        BLOCKS("blocks", Items.STONE),
        DECORATION("decoration", Items.STONE),
        FOOD_DRINKS("food-drinks", Items.STONE),
        HUMANS("humans", Items.STONE),
        HUMANOID("humanoid", Items.STONE),
        MISC("miscellaneous", Items.STONE),
        MONSTERS("monsters", Items.STONE),
        PLANTS("plants", Items.STONE);

        public final String name;
        public final Item icon;

        Category(String name, Item icon) {
            this.name = name;
            this.icon = icon;
        }

        public ItemStack createStack() {
            return new ItemStack(icon)
                    .setCustomName(getDisplayName()
                            .setStyle(Style.EMPTY.withItalic(false)));
        }

        public TranslatableText getDisplayName() {
            return new TranslatableText("text.headindex.category." + name);
        }
    }
}
