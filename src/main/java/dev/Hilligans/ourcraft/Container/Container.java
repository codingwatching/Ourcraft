package dev.Hilligans.ourcraft.Container;

import dev.Hilligans.ourcraft.Client.Client;
import dev.Hilligans.ourcraft.Client.MatrixStack;
import dev.Hilligans.ourcraft.Client.Rendering.ContainerScreen;
import dev.Hilligans.ourcraft.Client.Rendering.Widgets.Button;
import dev.Hilligans.ourcraft.Client.Rendering.Widgets.Widget;
import dev.Hilligans.ourcraft.Client.Rendering.World.StringRenderer;
import dev.Hilligans.ourcraft.ClientMain;
import dev.Hilligans.ourcraft.Container.Containers.ChestContainer;
import dev.Hilligans.ourcraft.Container.Containers.CreativeContainer;
import dev.Hilligans.ourcraft.Container.Containers.InventoryContainer;
import dev.Hilligans.ourcraft.Data.Other.IInventory;
import dev.Hilligans.ourcraft.Item.ItemStack;
import dev.Hilligans.ourcraft.Network.Packet.Client.CActivateButton;
import dev.Hilligans.ourcraft.Network.Packet.Server.SUpdateContainer;
import dev.Hilligans.ourcraft.Network.ServerNetworkHandler;
import dev.Hilligans.ourcraft.Util.Settings;

import java.nio.DoubleBuffer;
import java.util.ArrayList;

public abstract class Container {

    public int type;

    public int textureX = 0;
    public int textureY = 0;

    public int channelId;

    public int uniqueId = -1;

    public ArrayList<Integer> trackedIntegers = new ArrayList<>();
    public ArrayList<Widget> widgets = new ArrayList<>();

    public IInventory inventory;

    public Container(int type, IInventory inventory) {
        this.type = type;
        this.inventory = inventory;
    }

    //TODO slots should be added with an x based off their texture, the x and y pos need to be able to be recalculated when the screen is resized

    public ArrayList<Slot> slots = new ArrayList<>();

    public Slot getSlot(int slot) {
        if(slot < slots.size()) {
            return slots.get(slot);
        }
        return null;
    }

    public Container setPlayerId(int channelId) {
        this.channelId = channelId;
        return this;
    }

    public Container setUniqueId(int id) {
        this.uniqueId = id;
        return this;
    }

    public void trackInt(int slot, int val) {
        if(setInt(slot,val)) {
            ServerNetworkHandler.sendPacket(new SUpdateContainer((short) slot, val, uniqueId), channelId);
        }
    }

    public boolean setInt(int slot, int val) {
        while(slot >= trackedIntegers.size()) {
            trackedIntegers.add(0);
        }
        return trackedIntegers.set(slot,val) != val;
    }

    public int getInt(int slot) {
        while(slot >= trackedIntegers.size()) {
            trackedIntegers.add(0);
        }
        return trackedIntegers.get(slot);
    }

    public void addWidget(Widget widget) {
        if(widget instanceof Button && !Settings.isServer) {
            ((Button) widget).buttonAction = () -> ClientMain.getClient().sendPacket(new CActivateButton(widgets.size()));
        }
        widgets.add(widget);
    }

    public void addSlot(Slot slot) {
        slot.setContainerAndId((short) slots.size(),this);
        slots.add(slot);
    }

    public void render(MatrixStack matrixStack) {
        for(Slot slot : slots) {
            slot.render(matrixStack);
        }
        DoubleBuffer mousePos = ClientMain.getClient().getMousePos();
        Slot slot = getSlotAt((int)mousePos.get(0),(int)mousePos.get(1));
        if(slot != null && !slot.getContents().isEmpty()) {
            StringRenderer.drawStringWithBackgroundTranslated(matrixStack,slot.getContents().item.getName(),(int)mousePos.get(0) + 16,(int)mousePos.get(1),0.5f);
        }
    }

    public void setTextureSize(int x, int y) {
        this.textureX = x;
        this.textureY = y;
    }

    public void resize() {
        int newX = (int)(ClientMain.getWindowX() / 2 - textureX * Settings.guiSize / 2);
        int newY = (int)(ClientMain.getWindowY() / 2 - textureY * Settings.guiSize / 2);
        for(Slot slot : slots) {
            slot.x = (int)(newX + slot.startX * Settings.guiSize);
            slot.y = (int)(newY + slot.startY * Settings.guiSize);
        }
    }

    public void addPlayerInventorySlots(int startX, int startY, IInventory inventory, int startIndex) {
        for(int x = 0; x < 9; x++) {
            addSlot(new Slot(startX + 16 * x,startY + 69, inventory,startIndex + x));
        }
        for(int y = 0; y < 4; y++) {
            for (int x = 0; x < 9; x++) {
                addSlot(new Slot(startX + x * 16, startY + y * 16,inventory,startIndex + 9 + x + y * 9));
            }
        }
    }


    public abstract ContainerScreen<?> getContainerScreen(Client client);

    public void closeContainer() {
        for(Slot slot : slots) {
            slot.onClose();
        }
    }

    public Slot getSlotAt(int x, int y) {
        for(Slot slot : slots) {
            if(slot.x < x && slot.y < y && slot.x + 16 * Settings.guiSize > x && slot.y + 16 * Settings.guiSize > y) {
                return slot;
            }
        }
        return null;
    }

    public ItemStack swapStack(short slot, ItemStack heldStack) {
        Slot itemSlot = getSlot(slot);
        if(itemSlot != null) {
            if(itemSlot.canItemBeAdded(heldStack)) {
                return itemSlot.swapItemStacks(heldStack);
            }
        }
        return heldStack;
    }

    public ItemStack splitStack(short slot, ItemStack heldStack) {
        if(heldStack.isEmpty()) {
            return getSlot(slot).splitStack();
        }
        return heldStack;
    }

    public boolean putOne(short slot, ItemStack heldStack) {
        if(!heldStack.isEmpty()) {
            if(getSlot(slot).canAdd(1,heldStack)) {
                heldStack.count -= 1;
                return true;
            }
        }
        return false;
    }

    public ItemStack copyStack(short slot, ItemStack heldStack) {
        if(heldStack.isEmpty()) {
            return getSlot(slot).getContents().copy().setCount((byte)64);
        }
        return heldStack;
    }

    public static final ArrayList<ContainerFetcher> CONTAINERS = new ArrayList<>();

    public static Container getContainer(int slot) {
        if(slot >= CONTAINERS.size()) {
            return null;
        } else {
            return CONTAINERS.get(slot).getContainer();
        }
    }

    public static void register() {
        CONTAINERS.add(InventoryContainer::new);
        CONTAINERS.add(ChestContainer::new);
        CONTAINERS.add(CreativeContainer::new);
    }

    static int id = 0;

    public static int getId() {
        return id++;
    }

}
