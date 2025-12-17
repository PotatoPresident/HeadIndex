package us.potatoboy.headindex.api;

import com.google.gson.annotations.SerializedName;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

public class Category implements Comparable<Category> {
    @SerializedName("id")
    public int id;

    @SerializedName("n")
    public String name;

    public String getFileName() {
        return name.toLowerCase().replaceAll("[^a-z0-9]", "_");
    }

    public MutableComponent getDisplayName() {
        return Component.literal(name);
    }

    @Override
    public int compareTo(@NotNull Category o) {
        return this.id - o.id;
    }
}