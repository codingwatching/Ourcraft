package dev.Hilligans.ourcraft.Client.Rendering.Graphics.Tasks;

import dev.Hilligans.ourcraft.Client.Camera;
import dev.Hilligans.ourcraft.Client.Client;
import dev.Hilligans.ourcraft.Client.MatrixStack;
import dev.Hilligans.ourcraft.Client.Rendering.Graphics.API.IDefaultEngineImpl;
import dev.Hilligans.ourcraft.Client.Rendering.Graphics.API.IGraphicsEngine;
import dev.Hilligans.ourcraft.Client.Rendering.Graphics.RenderTarget;
import dev.Hilligans.ourcraft.Client.Rendering.Graphics.RenderTask;
import dev.Hilligans.ourcraft.Client.Rendering.Graphics.RenderTaskSource;
import dev.Hilligans.ourcraft.Client.Rendering.Graphics.RenderWindow;
import dev.Hilligans.ourcraft.Client.Rendering.MeshHolder;
import dev.Hilligans.ourcraft.ClientMain;
import dev.Hilligans.ourcraft.Data.Other.ChunkPos;
import dev.Hilligans.ourcraft.Entity.Entity;
import dev.Hilligans.ourcraft.GameInstance;
import dev.Hilligans.ourcraft.Util.Registry.IRegistryElement;
import dev.Hilligans.ourcraft.Util.Settings;
import dev.Hilligans.ourcraft.World.Chunk;
import dev.Hilligans.ourcraft.World.ClientWorld;
import dev.Hilligans.ourcraft.World.World;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.glDisable;

public class WorldRenderTask extends RenderTaskSource {

    public WorldRenderTask() {
        super("world_render_task", "solid_world_renderer");
    }

    @Override
    public RenderTask getDefaultTask() {
        return new RenderTask() {
            @Override
            public void draw(RenderWindow window, IDefaultEngineImpl<?> imp, IGraphicsEngine<?, ?, ?> engine, Client client, MatrixStack worldStack, MatrixStack screenStack) {
                Vector3d pos = Camera.renderPos;
                World world = client.clientWorld;
                for(int x = 0; x < Settings.renderDistance; x++) {
                    for (int z = 0; z < Settings.renderDistance; z++) {
                        Vector3i playerChunkPos = new Vector3i((int) pos.x >> 4, 0, (int) pos.z >> 4);
                        drawChunk(window,client,imp,worldStack, playerChunkPos, getChunk(playerChunkPos, x, z,world));
                        if (x != 0) {
                            drawChunk(window,client,imp,worldStack, playerChunkPos, getChunk(playerChunkPos, -x, z,world));
                            if (z != 0) {
                                drawChunk(window,client,imp,worldStack, playerChunkPos, getChunk(playerChunkPos, -x, -z,world));
                            }
                        }
                        if (z != 0) {
                            drawChunk(window,client,imp,worldStack, playerChunkPos, getChunk(playerChunkPos, x, -z,world));
                        }
                    }
                }
            }

            Chunk getChunk(Vector3i playerChunkPos, int chunkX, int chunkZ, World world) {



                return null;
            }

            void drawChunk(RenderWindow window, Client client, IDefaultEngineImpl<?> imp, MatrixStack matrixStack, Vector3i playerChunkPos, Chunk chunk) {
                if(chunk != null) {
                    if (matrixStack.frustumIntersection.testAab(new Vector3f((chunk.x - playerChunkPos.x) * 16, 0, (chunk.z - playerChunkPos.z) * 16), new Vector3f((chunk.x + 1 - playerChunkPos.x) * 16, 256f, (chunk.z + 1 - playerChunkPos.z) * 16))) {
                        matrixStack.push();
                        matrixStack.translate((chunk.x - playerChunkPos.x) * 16, 0, (chunk.z - playerChunkPos.z) * 16);

                        MeshHolder meshHolder = chunk.getSolidMesh();
                        int meshId = meshHolder.getId();
                        if (meshId != 0) {
                            imp.drawMesh(window, matrixStack, meshHolder.meshTexture, -1, meshId, meshHolder.index, meshHolder.length);
                        }
                        matrixStack.pop();
                    }
                }
            }
        };
    }
}
