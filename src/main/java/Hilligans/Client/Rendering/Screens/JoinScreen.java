package Hilligans.Client.Rendering.Screens;

import Hilligans.Block.Blocks;
import Hilligans.Client.Client;
import Hilligans.Client.ClientPlayerData;
import Hilligans.Client.Key.KeyPress;
import Hilligans.Client.MatrixStack;
import Hilligans.Client.Rendering.Renderer;
import Hilligans.Client.Rendering.Textures;
import Hilligans.Client.Rendering.Widgets.ServerSelectorWidget;
import Hilligans.Client.Rendering.World.StringRenderer;
import Hilligans.ClientMain;
import Hilligans.Client.Rendering.ScreenBase;
import Hilligans.Client.Rendering.Widgets.Button;
import Hilligans.Client.Rendering.Widgets.ButtonAction;
import Hilligans.Client.Rendering.Widgets.InputField;
import Hilligans.Network.ClientAuthNetworkHandler;
import Hilligans.Network.ClientNetworkHandler;
import Hilligans.Network.ClientNetworkInit;
import Hilligans.Network.Packet.AuthServerPackets.CCreateAccount;
import Hilligans.Network.Packet.AuthServerPackets.CGetToken;
import Hilligans.Network.Packet.Client.CSendBlockChanges;
import Hilligans.Network.ServerNetworkInit;
import Hilligans.Server.IntegratedServer;
import Hilligans.Server.MultiPlayerServer;
import Hilligans.ServerMain;
import Hilligans.Util.Settings;
import Hilligans.World.Builders.OreBuilder;
import Hilligans.World.ServerWorld;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_H;

public class JoinScreen extends ScreenBase {

    public ServerSelectorWidget selected;
    Button play = new Button(100, ClientMain.getWindowY() / 2 + 100, 200, 50, "menu.join", new ButtonAction() {
        @Override
        public void onPress() {
            if(selected != null) {
                selected.joinServer();
            }
        }
    }).isEnabled(false);

    public JoinScreen(Client client) {
        super(client);
        addWidget(play);
        addWidget(new ServerSelectorWidget(100,200,200,80,"72.172.99.188","25586",this));
        addWidget(new ServerSelectorWidget(100,300,200,80,"localhost","25586",this));
        addWidget(new Button(500, 200, 200, 50, "menu.create_account", () -> client.openScreen(new AccountCreationScreen(client))));
        addWidget(new Button(500, 300, 200, 50, "menu.log_in", () -> client.openScreen(new LoginScreen(client))));
        addWidget(new Button(500,400,200,50,"menu.singleplayer", () -> {
            ServerWorld world = new ServerWorld();
            world.worldBuilders.add(new OreBuilder(Blocks.GRASS,Blocks.STONE).setFrequency(20));

            client.multiPlayerServer = new MultiPlayerServer();
            client.multiPlayerServer.addWorld(0,world);
            ServerMain.server = client.multiPlayerServer;
            int port = 0;
            try {
                port = ServerNetworkInit.getOpenPort();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            String portString = port + "";
            Thread thread = new Thread(() -> client.multiPlayerServer.startServer(portString));
            thread.start();
           this.portString = portString;
        }));

        widgets.add(new Button(500,500,200,50,"menu.singleplayerjoin", () -> {
            try {
                client.network.joinServer("localhost",portString,client);
                client.closeScreen();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        widgets.add(new Button(500,600,200,50,"menu.mod_list", () -> client.openScreen(new ModListScreen(client))));

        registerKeyPress(new KeyPress() {
            @Override
            public void onPress() {
                client.openScreen(new TagEditorScreen(client));
            }
        }, GLFW_KEY_H);
    }

    public String portString;

    public void setActive(ServerSelectorWidget selected) {
        this.selected = selected;
        play.enabled = true;
    }

    @Override
    public void drawScreen(MatrixStack matrixStack) {
        super.drawScreen(matrixStack);
        if(client.playerData.valid_account) {
            StringRenderer.drawString(matrixStack,client.playerData.userName, (int) (Settings.guiSize * 8), (int) (1 * Settings.guiSize),0.5f);
            Textures.CHECK_MARK.drawTexture(matrixStack,0,0,(int)(8 * Settings.guiSize), (int)(8 * Settings.guiSize));
        } else {
            Textures.X_MARK.drawTexture(matrixStack,0,0,(int)(8 * Settings.guiSize), (int)(8 * Settings.guiSize));
        }


    }
}
