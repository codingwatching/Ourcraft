package dev.Hilligans.ourcraft.Data.Other.BlockShapes;

import dev.Hilligans.ourcraft.Client.Rendering.NewRenderer.BlockModel;
import dev.Hilligans.ourcraft.Client.Rendering.NewRenderer.PrimitiveBuilder;
import dev.Hilligans.ourcraft.Data.Other.BlockStates.BlockState;
import dev.Hilligans.ourcraft.Client.Rendering.World.Managers.BlockTextureManager;
import dev.Hilligans.ourcraft.Data.Other.BlockPos;
import dev.Hilligans.ourcraft.Data.Other.BoundingBox;
import dev.Hilligans.ourcraft.Data.Other.BlockStates.DataBlockState;
import dev.Hilligans.ourcraft.World.World;
import org.joml.Vector3f;

public class SlabBlockShape extends BlockShape {

    public SlabBlockShape() {
        data = BlockModel.create("/Models/Blocks/slab.txt");
    }


    public void addVertices(PrimitiveBuilder primitiveBuilder, int side, float size, BlockState blockState, BlockTextureManager blockTextureManager, Vector3f offset) {
        switch (((DataBlockState)blockState).readData()) {
            case 4:
                data.addData(primitiveBuilder,blockTextureManager,side,size,offset,2,0);
                break;
            case 1:
                data.addData(primitiveBuilder,blockTextureManager,side,size,offset,1,0);
                break;
            case 0:
                data.addData(primitiveBuilder,blockTextureManager,side,size,offset,1,2);
                break;
            case 2:
                data.addData(primitiveBuilder,blockTextureManager,side,size,offset,1,1);
                break;
            case 3:
                data.addData(primitiveBuilder,blockTextureManager,side,size,offset,3,1);
                break;
            default:
                data.addData(primitiveBuilder,blockTextureManager,side,size,offset,0,0);
                break;
        }
    }

    @Override
    public int getSide(BlockState blockState, int side) {
        switch (((DataBlockState)blockState).readData()) {

        }
        return super.getSide(blockState, side);
    }

    @Override
    public BoundingBox getBoundingBox(World world, BlockPos pos) {
        switch (((DataBlockState)world.getBlockState(pos)).readData()) {
            case 5:
                return new BoundingBox(0, 0, 0, 1f, 0.5f, 1f);
            case 4:
                return new BoundingBox(0, 0.5f, 0, 1f, 1.0f, 1f);
            case 3:
                return new BoundingBox(0,0,0,0.5f,1,1f);
            case 2:
                return new BoundingBox(0.5f,0,0,1,1,1f);
            case 1:
                return new BoundingBox(0,0,0,1f,1,0.5f);
            default:
                return new BoundingBox(0,0,0.5f,1f,1,1);
        }
    }

}
