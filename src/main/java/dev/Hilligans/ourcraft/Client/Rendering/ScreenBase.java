package dev.Hilligans.ourcraft.Client.Rendering;

import dev.Hilligans.ourcraft.Client.Client;
import dev.Hilligans.ourcraft.Client.Input.Key.CharPress;
import dev.Hilligans.ourcraft.Client.Input.Key.KeyHandler;
import dev.Hilligans.ourcraft.Client.Input.Key.KeyPress;
import dev.Hilligans.ourcraft.Client.MatrixStack;
import dev.Hilligans.ourcraft.Client.Rendering.Graphics.RenderWindow;
import dev.Hilligans.ourcraft.Client.Rendering.Widgets.Widget;
import dev.Hilligans.ourcraft.Network.Packet.Client.CCloseScreen;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1;

public abstract class ScreenBase implements Screen {

    public ArrayList<Widget> widgets = new ArrayList<>();
    public ArrayList<CharPress> charPresses = new ArrayList<>();
    public ArrayList<KeyPress> keyPresses = new ArrayList<>();

    public Client client;

    public ScreenBase() {}

    public ScreenBase(Client client) {
        this.client = client;
    }

    public RenderWindow window;

    public void drawScreen(MatrixStack matrixStack) {}

    public void render(MatrixStack matrixStack) {
        drawScreen(matrixStack);
        for(Widget widget : widgets) {
            widget.render(matrixStack,0,0);
        }
    }

    public void registerCharPress(CharPress charPress) {
        charPresses.add(charPress);
        KeyHandler.register(charPress);
    }

    public void registerKeyPress(KeyPress keyPress, int id) {
        keyPresses.add(keyPress);
        KeyHandler.register(keyPress,id);

    }

    @Override
    public void close(boolean replaced) {
        for(Widget widget : widgets) {
            widget.screenClose();
        }

        for(CharPress charPress : charPresses) {
            KeyHandler.remove(charPress);
        }

        for(KeyPress keyPress : keyPresses) {
            KeyHandler.remove(keyPress);
        }
        client.sendPacket(new CCloseScreen(replaced));
    }

    @Override
    public void mouseClick(int x, int y, int mouseButton) {
        if(mouseButton == GLFW_MOUSE_BUTTON_1) {
            for (Widget widget : widgets) {
                widget.isFocused = false;
                if (widget.isInBounds(x, y)) {
                    widget.activate(x - widget.x, y - widget.y);
                }
            }
        }
    }

    public void mouseScroll(int x, int y, float amount) {
        for(Widget widget : widgets) {
            widget.mouseScroll(x,y,amount);
        }
    }

    public void resize(int x, int y) {
        for(Widget widget : widgets) {
            widget.onScreenResize(x,y);
        }
    }

    public void addWidget(Widget widget) {
        widgets.add(widget);
        widget.screenBase = this;
        widget.onScreenResize(client.windowX,client.windowY);
    }

    @Override
    public void setWindow(RenderWindow renderWindow) {
        this.window = renderWindow;
    }
}
