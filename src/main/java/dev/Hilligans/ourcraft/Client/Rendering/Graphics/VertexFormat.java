package dev.Hilligans.ourcraft.Client.Rendering.Graphics;

import dev.Hilligans.ourcraft.ModHandler.Content.ModContent;
import dev.Hilligans.ourcraft.Util.Registry.IRegistryElement;

import java.util.ArrayList;

public class VertexFormat implements IRegistryElement {

    public ModContent modContent;
    public String formatName;

    public ArrayList<VertexPart> parts = new ArrayList<>();

    public int primitiveType;

    public VertexFormat(String formatName, int primitiveType) {
        this.formatName = formatName;
        this.primitiveType = primitiveType;
    }

    public VertexFormat addPart(String name, int primitiveType, int primitiveCount) {
        parts.add(new VertexPart(name, primitiveType, primitiveCount));
        return this;
    }

    public int getPrimitiveType() {
        return primitiveType;
    }

    public int getStride() {
        int stride = 0;
        for(VertexPart vertexPart : parts) {
            stride += vertexPart.getSize();
        }
        return stride;
    }

    @Override
    public String getResourceName() {
        return formatName;
    }

    @Override
    public String getIdentifierName() {
        return modContent.getModID() + ":" + formatName;
    }

    @Override
    public String getUniqueName() {
        return "vertex_format." + modContent.getModID() + "." + formatName;
    }

    @Override
    public void assignModContent(ModContent modContent) {
        this.modContent = modContent;
    }

    public static final int
            POINTS         = 0x0,
            LINES          = 0x1,
            LINE_LOOP      = 0x2,
            LINE_STRIP     = 0x3,
            TRIANGLES      = 0x4,
            TRIANGLE_STRIP = 0x5,
            TRIANGLE_FAN   = 0x6,
            QUADS          = 0x7,
            QUAD_STRIP     = 0x8,
            POLYGON        = 0x9;

    public static final int
            BYTE           = 0x0,
            UNSIGNED_BYTE  = 0x1,
            SHORT          = 0x2,
            UNSIGNED_SHORT = 0x3,
            INT            = 0x4,
            UNSIGNED_INT   = 0x5,
            FLOAT          = 0x6,
            _2_BYTES       = 0x7,
            _3_BYTES       = 0x8,
            _4_BYTES       = 0x9,
            DOUBLE         = 0xA;

    public static class VertexPart {

        public String name;

        public int primitiveSize;
        public int primitiveCount;
        public int primitiveType;

        public boolean normalized;

        public VertexPart(String name, int primitiveType, int count) {
            this.primitiveType = primitiveType;
            this.primitiveSize = VertexFormat.getSize(primitiveType);
            this.primitiveCount = count;
            this.name = name;
        }

        public int getSize() {
            return primitiveSize + primitiveCount;
        }
    }

    public static int getSize(int type) {
        return switch (type) {
            case 0x0, 0x1 -> 1;
            case 0x2, 0x3, 0x7 -> 2;
            case 0x8 -> 3;
            case 0xA -> 8;
            default -> 4;
        };
    }
}
