package dev.Hilligans.ourcraft;

import dev.Hilligans.ourcraft.Biome.Biome;
import dev.Hilligans.ourcraft.Block.Block;
import dev.Hilligans.ourcraft.Block.Blocks;
import dev.Hilligans.ourcraft.Client.Audio.Sounds;
import dev.Hilligans.ourcraft.Client.Rendering.ClientUtil;
import dev.Hilligans.ourcraft.Client.Rendering.Widgets.Widget;
import dev.Hilligans.ourcraft.Container.Container;
import dev.Hilligans.ourcraft.Data.Descriptors.Tag;
import dev.Hilligans.ourcraft.Entity.Entity;
import dev.Hilligans.ourcraft.Item.Item;
import dev.Hilligans.ourcraft.Item.Items;
import dev.Hilligans.ourcraft.ModHandler.Content.ContentPack;
import dev.Hilligans.ourcraft.ModHandler.Content.ModContent;
import dev.Hilligans.ourcraft.ModHandler.EventBus;
import dev.Hilligans.ourcraft.ModHandler.ModLoader;
import dev.Hilligans.ourcraft.Network.PacketBase;
import dev.Hilligans.ourcraft.Resource.ResourceManager;
import dev.Hilligans.ourcraft.Tag.NBTTag;
import dev.Hilligans.ourcraft.Util.NamedThreadFactory;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class GameInstance {

    public final EventBus EVENT_BUS = new EventBus();
    public final ModLoader MOD_LOADER = new ModLoader(this);
    public final Logger LOGGER = Logger.getGlobal();
    public final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2,new NamedThreadFactory("random_executor"));
    public final ResourceManager RESOURCE_MANAGER = new ResourceManager();
    public final ModContent OURCRAFT = new ModContent("ourcraft",this).addClassLoader(new URLClassLoader(new URL[]{Ourcraft.class.getProtectionDomain().getCodeSource().getLocation()})).addMainClass(Ourcraft.class);
    public final ContentPack CONTENT_PACK = new ContentPack(this);
    public final AtomicBoolean REBUILDING = new AtomicBoolean(false);


    public GameInstance() {
    }

    public void loadContent() {
        registerDefaultContent();
        CONTENT_PACK.mods.put("ourcraft",OURCRAFT);
        MOD_LOADER.loadDefaultMods();

    }

    public String path = System.getProperty("user.dir");

    public final ArrayList<Block> BLOCKS = new ArrayList<>();
    public final HashMap<String, Block> MAPPED_BLOCKS = new HashMap<>();
    public final ArrayList<Item> ITEMS = new ArrayList<>();
    public final HashMap<String, Item> MAPPED_ITEMS = new HashMap<>();
    public final ArrayList<Biome> BIOMES = new ArrayList<>();
    public final HashMap<String, Biome> MAPPED_BIOMES = new HashMap<>();
    public final ArrayList<Tag> TAGS = new ArrayList<>();
    public final HashMap<String, Tag> MAPPED_TAGS = new HashMap<>();


    public void clear() {
        BLOCKS.clear();
        MAPPED_BLOCKS.clear();
        ITEMS.clear();
        MAPPED_ITEMS.clear();
        TAGS.clear();
        MAPPED_TAGS.clear();
    }

    public Item getItem(int id) {
        if(ITEMS.size() > id) {
            return ITEMS.get(id);
        }
        return null;
    }
    public Block getBlockWithID(int id) {
        return BLOCKS.get(id);
    }

    public Block getBlock(String id) {
        return MAPPED_BLOCKS.get(id);
    }


    public void registerBlock(Block block) {
        MAPPED_BLOCKS.put(block.getName(),block);
        BLOCKS.add(block);
    }
    public void registerBlock(Block... blocks) {
        for(Block block : blocks) {
            registerBlock(block);
        }
    }
    public void registerItem(Item item) {
        ITEMS.add(item);
        MAPPED_ITEMS.put(item.name, item);
    }
    public void registerBiome(Biome biome) {
        BIOMES.add(biome);
        MAPPED_BIOMES.put(biome.name, biome);
    }

    public void registerTag(Tag tag) {
        TAGS.add(tag);
        MAPPED_TAGS.put(tag.type + ":" + tag.tagName,tag);
    }


    static short itemId = 0;
    public short blockId = 0;

    public static short getNextItemId() {
        short val = itemId;
        itemId++;
        return val;
    }
    public short getNextBlockID() {
        short val = blockId;
        blockId++;
        return val;
    }

    public void registerDefaultContent() {
        PacketBase.register();
        ClientUtil.register();
        Container.register();
        NBTTag.register();
        Widget.register();
        Entity.register();
        Blocks.register();
        Sounds.SOUNDS.size();
        Items.register();
    }

}
