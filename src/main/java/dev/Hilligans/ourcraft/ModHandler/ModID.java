package dev.Hilligans.ourcraft.ModHandler;

import dev.Hilligans.ourcraft.ModHandler.Content.ModContent;

public class ModID {

    public String modID;
    public boolean loaded = true;

    //optional
    public Mod mod;
    public ModContent modContent;

    public ModID(String modID) {
        this.modID = modID;
    }

    public String getModID() {
        return modID;
    }

    public boolean loaded() {
        return loaded;
    }

    public String getNamed(String content) {
        return getModID() + ":" + content;
    }
}
