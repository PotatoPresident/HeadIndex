package us.potatoboy.headindex.api;

import com.google.common.collect.ImmutableMultimap;
import com.google.gson.annotations.SerializedName;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import us.potatoboy.headindex.HeadIndex;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class Head implements Comparable<Head> {
    @SerializedName("n")
    public final String name;

    @SerializedName("v")
    public final String value;

    @SerializedName("i")
    private final String uuid;

    @SerializedName("t")
    private final List<Integer> tags;

    public Head(String uuid, String value) {
        this.name = "";
        this.uuid = uuid;
        this.value = value;
        this.tags =  new ArrayList<>();
    }

    public UUID getUuid() {
        return UUID.fromString(uuid);
    }
    
    public List<String> getTags() {
        return tags.stream().map(HeadIndex.HEAD_DATABASE::getTagName).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public ItemStack createStack(Text displayName) {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        if (displayName != null) {
            stack.set(DataComponentTypes.CUSTOM_NAME, displayName);
        }

        if (tags != null && !tags.isEmpty()) {
            stack.set(DataComponentTypes.LORE, new LoreComponent(getTags().stream().map(Text::literal).collect(Collectors.toUnmodifiableList())));
        }

        var props = new PropertyMap(ImmutableMultimap.of("textures", new Property("textures", value, null)));
        var profile = new GameProfile(getUuid(), "", props);
        stack.set(DataComponentTypes.PROFILE, ProfileComponent.ofStatic(profile));

        return stack;
    }

    public ItemStack createStack() {
        return createStack(Text.literal(name).setStyle(Style.EMPTY.withItalic(false)));
    }

    @Override
    public int compareTo(@NotNull Head o) {
        return this.name.compareTo(o.name);
    }
}
