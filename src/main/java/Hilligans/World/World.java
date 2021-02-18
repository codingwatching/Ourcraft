package Hilligans.World;

import Hilligans.Block.Block;
import Hilligans.Block.BlockState;
import Hilligans.Block.Blocks;
import Hilligans.Data.Other.BlockPos;
import Hilligans.Data.Other.BoundingBox;
import Hilligans.Data.Other.IInventory;
import Hilligans.Entity.Entities.ItemEntity;
import Hilligans.Entity.Entity;
import Hilligans.Item.ItemStack;
import Hilligans.Network.ClientNetworkHandler;
import Hilligans.Network.Packet.Client.CRequestChunkPacket;
import Hilligans.Util.*;
import Hilligans.Util.Noises.*;
import Hilligans.World.Builders.WorldBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class World {

    public Int2ObjectOpenHashMap<Entity> entities = new Int2ObjectOpenHashMap<>();
    public ConcurrentLinkedQueue<BlockChange> blockChanges = new ConcurrentLinkedQueue<>();
    Long2ObjectOpenHashMap<Chunk> chunks = new Long2ObjectOpenHashMap<>();

    long seed = 1342;

    Noise noise = new Noise(seed);
    Noise biomes = new Noise(new Random(seed).nextInt());

    public BiomeNoise biomeMap;



    SimplexNoise simplexNoise;

    public Random random;


    public ArrayList<WorldBuilder> worldBuilders = new ArrayList<>();

    public World() {
        random = new Random(seed);
        biomeMap = new BiomeNoise(random);
        simplexNoise = new SimplexNoise(random);

    }

    public abstract boolean isServer();

    public Chunk getChunk(long chunkPos) {
        try {
            return chunks.get(chunkPos);
        } catch (ArrayIndexOutOfBoundsException ignored) {
            return null;
        }
    }

    public Chunk getChunk(int x, int z) {
        //System.out.println("Getting " + (x & 4294967295L | ((long)z & 4294967295L) << 32));
        return getChunk((long)x & 4294967295L | ((long)z & 4294967295L) << 32);
    }

    public Chunk getOrGenerateChunk(int x, int z) {
        Chunk chunk = getChunk(x,z);
        if(chunk == null) {
            generateChunk(x,z);
        }
        chunk = getChunk(x,z);
        return chunk;
    }

    public void generateChunk(int x, int z) {
        if(getChunk(x,z) == null) {
            Chunk chunk = new Chunk(x,z,this);
            chunks.put(x & 4294967295L | ((long)z & 4294967295L) << 32,chunk);
            chunk.generate();
        }
    }

    public BlockState getBlockState(int x, int y, int z) {
        Chunk chunk = getChunk(x >> 4,z >> 4);
        if(chunk == null) {
            return Blocks.AIR.getDefaultState();
        }
        return chunk.getBlockState(x,y,z);
    }

    public DataProvider getDataProvider(BlockPos pos) {
        Chunk chunk = getChunk(pos.x >> 4,pos.z >> 4);
        if(chunk == null) {
            return null;
        }
        return chunk.getDataProvider(pos);
    }

    public void setDataProvider(BlockPos pos, DataProvider dataProvider) {
        Chunk chunk = getChunk(pos.x >> 4,pos.z >> 4);
        if(chunk != null) {
            chunk.setDataProvider(pos,dataProvider);
        }
    }

    public BlockState getBlockState(BlockPos pos) {
        return getBlockState(pos.x,pos.y,pos.z);
    }

    public void setBlockState(int x, int y, int z, BlockState blockState) {
        if(y >= Settings.minHeight  && y < Settings.maxHeight) {
            Chunk chunk = getChunk(x >> 4, z >> 4);
            if (chunk == null) {
                return;
            }
            chunk.setBlockState(x, y, z, blockState);
        }
    }

    public void spawnItemEntity(float x, float y, float z, ItemStack itemStack) {
        if(!itemStack.isEmpty()) {
            ItemEntity itemEntity = new ItemEntity(x, y, z, Entity.getNewId(), itemStack);
            itemEntity.velY = 0.30f;
            itemEntity.velX = (float) (Math.random() * 0.4 - 0.2f);
            itemEntity.velZ = (float) (Math.random() * 0.4 - 0.2f);
            itemEntity.pickupDelay = 10;
            addEntity(itemEntity);
        }
    }

    public void setBlockState(BlockPos pos, BlockState blockState) {
        setBlockState(pos.x,pos.y,pos.z,blockState);
    }

    public void tick() {
    }

    public void requestChunk(int x, int z) {
        for (ClientWorld.XZHolder requestedChunk : requestedChunks) {
            if (requestedChunk.x == x && requestedChunk.z == z) {
                return;
            }
        }
        requestedChunks.add(new ClientWorld.XZHolder(x,z));
        ClientNetworkHandler.sendPacket(new CRequestChunkPacket(x, z));
    }

    ConcurrentLinkedQueue<ClientWorld.XZHolder> requestedChunks = new ConcurrentLinkedQueue<>();

    public void setChunk(Chunk chunk) {
        for(ClientWorld.XZHolder xzHolder : requestedChunks) {
            if(xzHolder.x == chunk.x && xzHolder.z == chunk.z) {
                requestedChunks.remove(xzHolder);
                chunks.put(chunk.x & 4294967295L | ((long)chunk.z & 4294967295L) << 32,chunk);
                return;
            }
        }
    }

    public void setChunk(Chunk chunk, int x, int z) {
        chunk.setWorld(this);
        chunk.x = x;
        chunk.z = z;
        chunks.put(x & 4294967295L | ((long)z & 4294967295L) << 32,chunk);
    }

    public static final double stepCount = 0.00005;
    public static final int distance = 5;

    static final float offSet = -0.5f;

    public Vector3f traceBlock(float x, float y, float z, double pitch, double yaw) {
        Vector3d vector3d = new Vector3d();
        boolean placed = false;
        boolean isAir = true;
        for(int a = 0; a < distance / stepCount; a++) {
            final double Z = z - Math.sin(yaw) * Math.cos(pitch) * a * 0.1 + offSet;
            final double Y = y - Math.sin(pitch) * 0.1 * a + offSet;
            final double X = (x - Math.cos(yaw) * Math.cos(pitch) * a * 0.1) + offSet;
            BlockPos pos = new BlockPos((int) Math.round(X), (int) Math.round(Y), (int) Math.round(Z));
            BlockState blockState = getBlockState(pos);
            if(blockState.block != Blocks.AIR) {
                if(blockState.block.getBoundingBox(blockState).intersectVector(new Vector3f((float)X - offSet,(float)Y - offSet,(float)Z - offSet), pos)) {
                    placed = true;
                    break;
                } else {
                    isAir = false;
                }
            } else {
                isAir = true;
            }
            vector3d.x = X;
            vector3d.y = Y;
            vector3d.z = Z;
        }

        if(placed && isAir) {
            return new Vector3f((float)vector3d.x,(float)vector3d.y,(float)vector3d.z);
        } else {
            return null;
        }
    }

    public BlockState traceBlockState(float x, float y, float z, double pitch, double yaw) {
        for(int a = 0; a < distance / stepCount; a++) {
            final double Z = z - Math.sin(yaw) * Math.cos(pitch) * a * 0.1 + offSet;
            final double Y = y - Math.sin(pitch) * 0.1 * a + offSet;
            final double X = (x - Math.cos(yaw) * Math.cos(pitch) * a * 0.1) + offSet;
            BlockState blockState = getBlockState((int) Math.round(X), (int) Math.round(Y), (int) Math.round(Z));
            if(blockState.block != Blocks.AIR) {
                return blockState;
            }
        }
        return null;
    }

    public BlockPos traceBlockToBreak(float x, float y, float z, double pitch, double yaw) {
        for(int a = 0; a < distance / stepCount; a++) {
            final double Z = z - Math.sin(yaw) * Math.cos(pitch) * a * 0.1 + offSet;
            final double Y = y - Math.sin(pitch) * 0.1 * a + offSet;
            final double X = (x - Math.cos(yaw) * Math.cos(pitch) * a * 0.1) + offSet;
            BlockPos pos = new BlockPos((int) Math.round(X), (int) Math.round(Y), (int) Math.round(Z));
            BlockState blockState = getBlockState(pos);
            if(blockState.block != Blocks.AIR) {
                if(blockState.block.getBoundingBox(blockState).intersectVector(new Vector3f((float)X - offSet,(float)Y - offSet,(float)Z - offSet), pos)) {
                    return pos;
                }
            }
        }
        return null;
    }

    public Chunk[] getChunksAround(int x, int z, int radius) {
        if(radius == 0) {
            return new Chunk[] {getChunk(x + 1,z),getChunk(x - 1,z),getChunk(x,z + 1),getChunk(x,z - 1)};
        }
        return null;
    }

    public abstract void addEntity(Entity entity);

    public abstract void removeEntity(int id);

    public BlockPos getWorldSpawn(BoundingBox boundingBox) {
        BlockPos pos = new BlockPos(0,Settings.chunkHeight * 16,0);
        int y;
        out:
        for(y = 0; y < Settings.chunkHeight * 16 - 1; y++) {
            for(int z = -1; z < 2; z++) {
                for(int x = -1; x < 2; x++) {
                    BlockState blockState = getBlockState(new BlockPos(x,pos.y,z));
                    if(blockState.block != Blocks.AIR) {
                        if (boundingBox.intersectsBox(blockState.getBoundingBox(), pos.get3f(), new Vector3f(x, pos.y, z))) {
                            break out;
                        }
                    }
                }
            }
            pos.y -= 1;
        }
        return pos.add(0,3,0);
    }

    public static class BlockChange {
        public int x;
        public int z;
        public int y;
        public  BlockState blockState;

        public BlockChange(int x, int y, int z, BlockState blockState) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.blockState = blockState;
        }

    }




}
