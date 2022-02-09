package dev.Hilligans.ourcraft.Client.Rendering.Graphics.Vulkan;

import dev.Hilligans.ourcraft.Client.MatrixStack;
import dev.Hilligans.ourcraft.Client.Rendering.Graphics.API.IGraphicsEngine;
import dev.Hilligans.ourcraft.Client.Rendering.Graphics.Vulkan.Boilerplate.VulkanInstance;
import dev.Hilligans.ourcraft.Client.Rendering.Graphics.Vulkan.Boilerplate.VulkanProperties;
import dev.Hilligans.ourcraft.Client.Rendering.Graphics.Vulkan.Boilerplate.Window.VulkanWindow;
import dev.Hilligans.ourcraft.ClientMain;
import dev.Hilligans.ourcraft.GameInstance;
import dev.Hilligans.ourcraft.ModHandler.Content.ModContent;
import dev.Hilligans.ourcraft.World.Chunk;
import dev.Hilligans.ourcraft.World.ClientWorld;

import java.util.ArrayList;

public class VulkanEngine implements IGraphicsEngine<VulkanGraphicsContainer, VulkanWindow> {

    public VulkanInstance vulkanInstance;
    public GameInstance gameInstance;

    @Override
    public VulkanWindow createWindow() {
        return vulkanInstance.getDefaultDevice().logicalDevice.getWindow();
    }

    @Override
    public VulkanGraphicsContainer getChunkGraphicsContainer(Chunk chunk) {
        return null;
    }

    @Override
    public VulkanGraphicsContainer createChunkGraphicsContainer() {
        return null;
    }

    @Override
    public void putChunkGraphicsContainer(Chunk chunk, VulkanGraphicsContainer container) {

    }

    @Override
    public void render(VulkanWindow window) {

    }

    @Override
    public void renderWorld(MatrixStack matrixStack, ClientWorld world) {

    }

    @Override
    public void renderScreen(MatrixStack screenStack) {

    }

    @Override
    public void setup() {
        vulkanInstance = getVulkanInstance();
        vulkanInstance.run();
    }

    @Override
    public void close() {
        vulkanInstance.exit("closing");
    }

    @Override
    public ArrayList<VulkanWindow> getWindows() {
        return null;
    }

    @Override
    public GameInstance getGameInstance() {
        return gameInstance;
    }

    @Override
    public boolean isCompatible() {
        return true;
    }

    public static VulkanInstance getVulkanInstance() {
        return getVulkanInstance(new VulkanProperties(ClientMain.argumentContainer).warningValidation().errorValidation().addValidationLayers("VK_LAYER_KHRONOS_validation", "VK_LAYER_KHRONOS_validation").enableValidationLayers());
    }

    public static VulkanInstance getVulkanInstance(VulkanProperties vulkanProperties) {
        if(sInstance == null) {
            sInstance = new VulkanInstance(vulkanProperties);
        }
        return sInstance;
    }

    public static VulkanInstance sInstance;

    @Override
    public void assignModContent(ModContent modContent) {
        this.gameInstance = modContent.gameInstance;
    }

    @Override
    public void load(GameInstance gameInstance) {
    }

    @Override
    public String getResourceName() {
        return "vulkanEngine";
    }

    @Override
    public String getIdentifierName() {
        return "ourcraft:vulkanEngine";
    }

    @Override
    public String getUniqueName() {
        return "graphicsEngine.ourcraft.vulkanEngine";
    }
}
