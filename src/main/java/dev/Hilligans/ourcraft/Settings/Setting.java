package dev.Hilligans.ourcraft.Settings;

import dev.Hilligans.ourcraft.Util.Side;

public abstract class Setting {

    public Side side = Side.COMMON;
    public String name;

    public Setting(String name) {
        this.name = name;
    }

    public Setting(String name, Side side) {
        this(name);
        this.side = side;
    }

    abstract void read(String string);

    abstract String write();
}
