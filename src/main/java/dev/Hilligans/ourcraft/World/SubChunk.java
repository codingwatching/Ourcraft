package dev.Hilligans.ourcraft.World;

import dev.Hilligans.ourcraft.Block.Block;
import dev.Hilligans.ourcraft.Client.Rendering.NewRenderer.GLRenderer;
import dev.Hilligans.ourcraft.Client.Rendering.NewRenderer.PrimitiveBuilder;
import dev.Hilligans.ourcraft.Client.Rendering.World.Managers.ShaderManager;
import dev.Hilligans.ourcraft.Data.Other.BlockStates.BlockState;
import dev.Hilligans.ourcraft.Block.Blocks;
import dev.Hilligans.ourcraft.Client.MatrixStack;
import dev.Hilligans.ourcraft.Client.Rendering.World.Managers.VAOManager;
import dev.Hilligans.ourcraft.Data.Other.BlockPos;
import dev.Hilligans.ourcraft.Data.Other.BlockStates.DataBlockState;
import dev.Hilligans.ourcraft.World.DataProviders.ShortBlockState;
import dev.Hilligans.ourcraft.Util.Settings;
import org.lwjgl.opengl.GL30;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;

public class SubChunk {

    public int id = -2;
    int verticesCount = -1;
    World world;
    public long y;
    public long x;
    public long z;

    public boolean empty = true;


    public int[] vals;

    public SubChunk(World world, long X, long Y, long Z) {
        this.world = world;
        this.y = Y;
        this.x = X;
        this.z = Z;
    }

    public void fill() {
        vals = new int[16*16*16];
        for(int x = 0; x < 16; x++) {
            for(int i = 0; i < 16; i++) {
                for(int z = 0; z < 16; z++) {
                    setBlockState(x,i,z, Blocks.AIR.getDefaultState());
                }
            }
        }
    }

    public void createMesh1() {
        PrimitiveBuilder primitiveBuilder = getMeshBuilder();
        verticesCount = primitiveBuilder.indices.size();
        id = VAOManager.createVAO(primitiveBuilder);
    }

    public PrimitiveBuilder getMeshBuilder() {
        PrimitiveBuilder primitiveBuilder = new PrimitiveBuilder(GL_TRIANGLES, ShaderManager.worldShader);
        for(int x = 0; x < 16; x++) {
            for(int y = 0; y < 16; y++) {
                for(int z = 0; z < 16; z++) {
                    BlockState block = getBlock(x,y,z);
                    for(int a = 0; a < 6; a++) {
                        if(block.getBlock() != Blocks.AIR) {
                            BlockState blockState = getBlock(new BlockPos(x, y, z).add(Block.getBlockPos(block.getBlock().getSide(block,a))));
                            if ((blockState.getBlock().blockProperties.transparent && (Settings.renderSameTransparent || block.getBlock() != blockState.getBlock())) || block.getBlock().blockProperties.alwaysRender) {
                                block.getBlock().addVertices(primitiveBuilder,a,1.0f,block,new BlockPos(x + this.x,y + this.y,z + this.z),x,z);
                            }
                        }
                    }
                }
            }
        }
        return primitiveBuilder;
    }



    public void destroy() {
        if(id != -1 && id != -2 && id != -3) {
            VAOManager.destroyBuffer(id);
            id = -1;
        }
        world.getChunk((int)x >> 4,(int)z >> 4).destroyMap(-1);
    }

    public BlockState getBlock(BlockPos pos) {
        if(pos.isSubChunkValid()) {
            return getBlock(pos.x,pos.y,pos.z);
        }
        pos.y += y;
        pos.x += x;
        pos.z += z;

        return world.getBlockState(pos);
    }

    public BlockState getBlock(int pos) {
        return getBlock(pos & 0xF,pos & 0x0F,pos & 0x00F);
    }

    public BlockState getBlock(int x, int y, int z) {
        if(empty) {
            return Blocks.AIR.getDefaultState();
        }
        if((vals[x | y << 4 | z << 8] & 65535) == 65535) {
            return new BlockState((short) (vals[x | y << 4 | z << 8] >> 16));
        } else {
            return new DataBlockState((short) (vals[x | y << 4 | z << 8] >> 16),new ShortBlockState((short) (vals[x | y << 4 | z << 8] & 65535)));
        }

       // return blocks[x][y][z];
    }


    public void setBlockState(int x, int y, int z, BlockState blockState) {
       if(!blockState.getBlock().blockProperties.airBlock) {
           getBlock(x & 15, y & 15, z & 15).getBlock().onBreak(world,new BlockPos(x,y,z));
           if (empty) {
               empty = false;
               fill();
           }
       }
       if(!empty) {
           int i = (x & 15) | (y & 15) << 4 | (z & 15) << 8;
           if (blockState instanceof DataBlockState) {
               vals[i] = blockState.blockId << 16 | ((DataBlockState) blockState).blockData.write();
           } else {
               vals[i] = blockState.blockId << 16 | 65535;
           }
       }
    }

    public void updateBlock(BlockPos pos) {
        getBlock(pos.x & 15,pos.y & 15,pos.z & 15).getBlock().onUpdate(world,pos);
    }

    public void renderMesh(MatrixStack matrixStack) {

        if (id == -2) {
            if (world instanceof ClientWorld) {
                ((ClientWorld) world).queuedChunks.add(this);
                id = -3;
            } else {
                id = -1;
            }
        }
        if (id == -1) {
            createMesh1();
        }

        if (verticesCount != 0) {
            GL30.glBindVertexArray(id);
            matrixStack.push();
            matrixStack.applyTransformation(ShaderManager.worldShader.shader);
            GLRenderer.glDrawElements(GL_TRIANGLES, verticesCount, GL_UNSIGNED_INT, 0);
            matrixStack.pop();
        }
    }

    public void set(int block) {
        for(int x = 0; x < 4096; x++) {
            vals[x] = block;
        }
    }
}
