package Hilligans.World;

import Hilligans.Block.Block;
import Hilligans.Block.BlockState;
import Hilligans.Block.Blocks;
import Hilligans.Data.Other.BlockPos;
import Hilligans.Entity.Entities.ItemEntity;
import Hilligans.Entity.Entity;
import Hilligans.Network.Packet.Server.SCreateEntityPacket;
import Hilligans.Network.Packet.Server.SRemoveEntityPacket;
import Hilligans.Network.ServerNetworkHandler;

import java.util.ArrayList;

public class ServerWorld extends World {

    ArrayList<Integer> entityRemovals = new ArrayList<>();



    @Override
    public void addEntity(Entity entity) {
        entities.put(entity.id,entity);
        ServerNetworkHandler.sendPacket(new SCreateEntityPacket(entity));
    }

    @Override
    public void removeEntity(int id) {
        //entities.remove(id);
        //ServerNetworkHandler.sendPacket(new SRemoveEntityPacket(id));
        entityRemovals.add(id);
    }

    private void handleRemove() {
        for(Integer integer : entityRemovals) {
            entities.remove((int)integer);
            ServerNetworkHandler.sendPacket(new SRemoveEntityPacket(integer));
        }
    }

    @Override
    public void tick() {
        int x = 0;
        handleRemove();
        //System.out.println("TICKING");
        for(Entity entity : entities.values()) {
            //System.out.println(entity.id);
            try {
                entity.tick();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //System.out.println(x);
            x++;
        }
    }

    @Override
    public void setBlockState(int x, int y, int z, BlockState blockState) {
        super.setBlockState(x, y, z, blockState);
        //ServerNetworkHandler.sendPacket(new SSendBlockChanges(x,y,z,blockState.block.id));
    }

    public void createItemEntity(BlockPos blockPos) {
        Block block = getBlockState(blockPos).block;
        if(block != Blocks.AIR) {
            ItemEntity itemEntity = new ItemEntity(blockPos.x,blockPos.y,blockPos.z,Entity.getNewId(),block);
            addEntity(itemEntity);
        }
    }
}
