package dev.Hilligans.ourcraft.Client.Rendering;

public class MeshHolder {

    public int id = 0;
    public int oldID = 0;

    public int index = 0;
    public int length = 0;

    public int meshTexture = 0;

    public void set(int id, int length) {
        oldID = id;
        this.id = id;
        this.length = length;
    }

    public int getId() {
        return id;
    }
}
