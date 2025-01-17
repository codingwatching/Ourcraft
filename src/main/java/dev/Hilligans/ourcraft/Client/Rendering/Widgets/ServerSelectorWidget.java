package dev.Hilligans.ourcraft.Client.Rendering.Widgets;

import dev.Hilligans.ourcraft.Client.MatrixStack;
import dev.Hilligans.ourcraft.Client.Rendering.Screens.JoinScreen;
import dev.Hilligans.ourcraft.Client.Rendering.Screens.LoadingScreen;
import dev.Hilligans.ourcraft.Client.Rendering.Textures;
import dev.Hilligans.ourcraft.Client.Rendering.World.StringRenderer;
import dev.Hilligans.ourcraft.ClientMain;
import dev.Hilligans.ourcraft.Util.Settings;

public class ServerSelectorWidget extends Widget {

    long lastTime;
    String ip;
    String port;
    JoinScreen joinScreen;

    public ServerSelectorWidget(int x, int y, int width, int height, String ip, String port, JoinScreen joinScreen) {
        super(x, y, width, height);
        this.ip = ip;
        this.port = port;
        this.joinScreen = joinScreen;
    }

    @Override
    public void render(MatrixStack matrixStack, int xOffset, int yOffset) {
        super.render(matrixStack, xOffset, yOffset);
       // Textures.BUTTON.drawTexture(matrixStack,x,y,width,height);
        float sizeX = width / (float)Textures.BUTTON.width;
        float sizeY = height / (float)Textures.BUTTON.height;
        StringRenderer.drawString(matrixStack,ip + ":" + port,x + (int)(sizeX * Settings.guiSize),y + (int)(sizeY * Settings.guiSize),0.5f);
    }

    public void joinServer() {
        try {
            screenBase.client.network.joinServer(ip,port, ClientMain.getClient());
            screenBase.client.closeScreen();
            screenBase.client.serverIP = ip + ":" + port;
            screenBase.client.openScreen(new LoadingScreen(screenBase.client));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void activate(int x, int y) {
        long time = System.currentTimeMillis();
        joinScreen.setActive(this);
        if(time - lastTime < 250) {
            joinServer();
        }
        lastTime = time;
    }
}
