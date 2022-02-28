package dev.Hilligans.ourcraft.Client.Rendering.NewRenderer;

import dev.Hilligans.ourcraft.Data.Primitives.Tuple;
import dev.Hilligans.ourcraft.Ourcraft;
import dev.Hilligans.ourcraft.Resource.ResourceLocation;
import dev.Hilligans.ourcraft.Util.DaisyChain;
import dev.Hilligans.ourcraft.Util.NamedThreadFactory;
import dev.Hilligans.ourcraft.Util.Pipeline;
import dev.Hilligans.ourcraft.Util.PipelineStage;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public class TextAtlas {

    public Image image;
    public int size = 32;
    public int minWidth = 16;
    public ExecutorService executorService;

    public ArrayList<ImageLocation> images = new ArrayList<>();
    public Int2LongOpenHashMap ids = new Int2LongOpenHashMap();
    public HashMap<String, Integer> cache = new HashMap<>();

    long[][] openSpots;
    int arrSize;

    public int texture;
    int id = 0;

    public TextAtlas() {
        image = new Image(size, size);
    }

    public synchronized int register(ImageLocation imageLocation) {
        String key = imageLocation.modId + imageLocation.path;
        if(cache.containsKey(key)) {
            return cache.get(key);
        }
        images.add(imageLocation);
        imageLocation.textAtlas = this;
        imageLocation.index = id;
        if(id == -1) {
            System.out.println("ran out of space");
        }
        cache.put(key,imageLocation.index);
        id++;
        return imageLocation.index;
    }

    public void clear() {
        id = 0;
        ids.clear();
        start();
    }

    private long getLong(long size, int x, int y) {
        return ((long) (short) size << 32) | ((short)x << 16) | (short) y;
    }

    public void assemble() {
        long start = System.currentTimeMillis();

        executorService = Executors.newFixedThreadPool(4,new NamedThreadFactory("texture_atlas_builder"));
        time = 0;
        count = 0;
        for(ImageLocation imageLocation : images) {
            executorService.submit(() -> {
                Image tempImage;
                long spot = -1;
                try {
                    tempImage = (Image)Ourcraft.GAME_INSTANCE.RESOURCE_LOADER.getResource(new ResourceLocation(imageLocation.path, imageLocation.modId));
                    if(tempImage == null) {
                        System.out.println(new ResourceLocation(imageLocation.path, imageLocation.modId).toIdentifier());
                        return;
                    }
                    tempImage.flip(false).ensureSquare();

                    count++;
                    long starts = System.nanoTime();
                    spot = findSpot(tempImage.width, imageLocation.index);
                    image.putImage(tempImage, (int)(spot >> 32), (int)(spot));
                    time += System.nanoTime() - starts;

                } catch (Exception e) {
                    System.out.println("Failed to put image x:" + ((int)(spot >> 32)) + " y:" + (int)(spot));
                    e.printStackTrace();
                }
            });
        }
        System.out.println(time / count);
        executorService.shutdown();
        try {
            executorService.awaitTermination(100000, TimeUnit.MILLISECONDS);
        } catch (Exception ignored) {}

        System.out.println(System.currentTimeMillis() - start);
    }

    public long time = 0;
    public double count  = 0;

    public int upload() {
        assemble();
        texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, image.width, image.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image.buffer);
        glGenerateMipmap(GL_TEXTURE_2D);
        return texture;
    }

    public void start() {
        arrSize = (int) (Math.log((float)size / minWidth) / Math.log(2));
        openSpots = new long[arrSize][3];
        for(int x = 0; x < arrSize; x++) {
            openSpots[x][0] = -1;
            openSpots[x][1] = -1;
            openSpots[x][2] = -1;
        }
        openSpots[arrSize - 1][0] = 0;
    }

    public synchronized long findSpot(int size, int index) {
        int loc = (int) (Math.log((float) size / minWidth) / Math.log(2));
        int ogLoc = loc;
        long spot;
        while (loc < arrSize) {
            for (int z = 2; z >= 0; z--) {
                spot = openSpots[loc][z];
                if (spot != -1) {
                    openSpots[loc][z] = -1;
                    while (loc != ogLoc) {
                        loc--;
                        int x = (int) (spot >> 32);
                        int y = (int) spot;
                        int width = minWidth << (loc);
                        openSpots[loc][0] = spot;
                        openSpots[loc][1] = ((long) x << 32) | (y + width);
                        openSpots[loc][2] = ((long) (x + width) << 32) | y;
                        spot = ((long) (x + width) << 32) | (y + width);
                    }
                    int x = (int) (spot >> 32);
                    int y = (int) spot;
                    ids.put(index, getLong(size, x, y));
                    return spot;
                }
            }
            loc++;
        }
        resize();
        return findSpot(size, index);
    }

    public synchronized void resize() {
        System.out.println("resize");
        arrSize++;
        long[][] newVals = new long[arrSize][3];
        System.arraycopy(openSpots, 0, newVals, 0, arrSize - 1);
        Image newImage = new Image(size * 2, size * 2);
        newImage.putImage(image,0,0);
        image = newImage;
        openSpots = newVals;
        openSpots[arrSize - 1][0] = ((long) (size) << 32) | (size);
        openSpots[arrSize - 1][1] = (size);
        openSpots[arrSize - 1][2] = (long) (size) << 32;
        size *= 2;
    }

    public int loadTextureId(String path, String textureName, String source) {
        return register(new ImageLocation("Images/" + path,source));
    }

    public final static TextAtlas instance = new TextAtlas();

    public static float getMinX(int id) {
        return (get(id,16)) / (float)instance.size;
    }
    public static float getMaxX(int id) {
        return (get(id,16) + get(id,32)) / (float)instance.size;
    }
    public static float getMinY(int id) {
        return (get(id,0)) / (float)instance.size;
    }
    public static float getMaxY(int id) {
        return (get(id,0) + get(id,32)) / (float)instance.size;
    }

    public static int get(int id, int shift) {
        return (short)(instance.ids.get(id) >> shift);
    }
}
