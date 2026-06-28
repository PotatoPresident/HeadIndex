package us.potatoboy.headindex.api;

import com.google.gson.annotations.SerializedName;

public class Tag {
    @SerializedName("id")
    public int id;

    @SerializedName("n")
    public String name;
}
