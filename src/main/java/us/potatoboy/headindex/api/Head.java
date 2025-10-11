package us.potatoboy.headindex.api;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class Head {
    public final String name;
    public final UUID uuid;
    public final String value;
    @Nullable
    public final String tags;

    public Head(@Nullable String name, UUID uuid, String value, @Nullable String tags) {
        this.name = name;
        this.uuid = uuid;
        this.value = value;
        this.tags = tags;
    }

    public Head(UUID uuid, String value) {
        this.name = "";
        this.uuid = uuid;
        this.value = value;
        this.tags = null;
    }

    public String getTagsOrEmpty() {
        return tags == null ? "" : tags;
    }

    public ItemStack createStack(Text displayName) {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        if (displayName != null) {
            stack.set(DataComponentTypes.CUSTOM_NAME, displayName);
        }

        if (tags != null) {
            stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(Text.literal(tags))));
        }

        var props = new PropertyMap(ImmutableMultimap.of("textures", new Property("textures", value, null)));
        var profile = new GameProfile(uuid, "", props);
        stack.set(DataComponentTypes.PROFILE, ProfileComponent.ofStatic(profile));

        return stack;
    }

    public ItemStack createStack() {
        return createStack(name != null ? Text.literal(name).setStyle(Style.EMPTY.withItalic(false)) : null);
    }

    public enum Category {
        ALPHABET("alphabet",
                new Head(
                        UUID.fromString("1f961930-4e97-47b7-a5a1-2cc5150f3764"),
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmMzNWU3MjAyMmUyMjQ5YzlhMTNlNWVkOGE0NTgzNzE3YTYyNjAyNjc3M2Y1NDE2NDQwZDU3M2E5MzhjOTMifX19").createStack()
        ),
        ANIMALS("animals",
                new Head(
                        UUID.fromString("6554e785-2a74-481a-9aac-06fc18620a57"),
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWQxN2U0OGM5MjUzZTY3NDczM2NlYjdiYzNkYTdmNTIxNTFlNTI4OWQwMjEyYzhmMmRkNzFlNDE2ZTRlZTY1In19fQ=="
                ).createStack()
        ),
        BLOCKS("blocks",
                new Head(
                        UUID.fromString("795e1ad8-de6d-4edc-a1b5-4e6aad038403"),
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODQ0OWI5MzE4ZTMzMTU4ZTY0YTQ2YWIwZGUxMjFjM2Q0MDAwMGUzMzMyYzE1NzQ5MzJiM2M4NDlkOGZhMGRjMiJ9fX0="
                ).createStack()
        ),
        DECORATION("decoration",
                new Head(
                        UUID.fromString("f3244903-0c01-4f8d-bbc2-4b13338c6a10"),
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQyYWNiZTVkMmM2MmU0NTViMGQ4ZTY5YzdmNmIwMWJiNjg5NzVmYmZjZmQ5NWMyNzViM2Y5MTYzMTU4NTE5YyJ9fX0="
                ).createStack()
        ),
        FOOD_DRINKS("food-drinks",
                new Head(
                        UUID.fromString("187ab05d-1d27-450b-bea8-a723fd1d3b4a"),
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZiNDhlMmI5NjljNGMxYjg2YzVmODJhMmUyMzc5OWY0YTZmMzFjZTAwOWE1ZjkyYjM5ZjViMjUwNTdiMmRkMCJ9fX0="
                ).createStack()
        ),
        HUMANS("humans",
                new Head(
                        UUID.fromString("68cd5f2e-01d3-4ac8-882e-2f7ce487b33b"),
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGNiN2ExNWVkYTFjYmU0N2E4ZDVkN2Y3ODBlODliYmMzNWUwYzE3N2ZjYjljNjQ4MGExMWIwMmNjODE2NWMxYyJ9fX0="
                ).createStack()
        ),
        HUMANOID("humanoid",
                new Head(
                        UUID.fromString("0d8391c2-1748-4869-8631-935ff2d55e07"),
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNhOGVmMjQ1OGEyYjEwMjYwYjg3NTY1NThmNzY3OWJjYjdlZjY5MWQ0MWY1MzRlZmVhMmJhNzUxMDczMTVjYyJ9fX0="
                ).createStack()
        ),
        MISC("miscellaneous",
                new Head(
                        UUID.fromString("13affe21-698a-4a5e-aff1-ad5183d5f810"),
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTFlNDJiOGY1MGZlZDgyOGQ0Yjk4MWMyN2NhMTNkMDcxY2U4NjNmNjE1NDBiMjc2MzgyNjZmNzcyZDQxZCJ9fX0="
                ).createStack()
        ),
        MONSTERS("monsters",
                new Head(
                        UUID.fromString("a1d05a1e-5937-48ad-973f-70b922d025be"),
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjI2MDJkNWIzNjJhYTE2MzZkMzVhZjIwZmM3MGQyZTc5NDEzMmVhNjRkNjJkMjNmNTVkYjg1MTVhMGM2MTljNyJ9fX0="
                ).createStack()
        ),
        PLANTS("plants", new Head(
                UUID.fromString("6b063c51-34b4-4fcb-be0d-a6aff0783328"),
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTAxOWVjZTEyNWVjNzlmOTUxNzhlMWFmNGRhMmE4Yjk4MmRlNzFlZDQyYzMxY2FjNGIxZDJmNjY1MzU1ZGY1YSJ9fX0="
        ).createStack());

        public final String name;
        public final ItemStack icon;

        Category(String name, ItemStack icon) {
            this.name = name;
            this.icon = icon;
        }

        public ItemStack createStack() {
            icon.set(DataComponentTypes.CUSTOM_NAME, getDisplayName()
                            .setStyle(Style.EMPTY.withItalic(false))
            );
            return icon;
        }

        public MutableText getDisplayName() {
            return Text.translatable("text.headindex.category." + name);
        }
    }
}
