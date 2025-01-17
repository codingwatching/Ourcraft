package dev.Hilligans.ourcraft.Network.Packet.AuthServerPackets;

import dev.Hilligans.ourcraft.Network.PacketBase;
import dev.Hilligans.ourcraft.Network.PacketData;

public class CLogin extends PacketBase {

    String username;
    String password;
    String email;


    public CLogin() {
        super(3);
    }

    public CLogin(String username, String password, String email) {
        this();
        this.username = username;
        this.password = password;
        this.email = email;
    }

    @Override
    public void encode(PacketData packetData) {
        packetData.writeString(username);
        packetData.writeString(password);
        packetData.writeString(email);
    }

    @Override
    public void decode(PacketData packetData) {

    }

    @Override
    public void handle() {

    }
}
