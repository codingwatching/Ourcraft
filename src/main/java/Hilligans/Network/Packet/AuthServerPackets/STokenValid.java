package Hilligans.Network.Packet.AuthServerPackets;

import Hilligans.Network.Packet.Client.CHandshakePacket;
import Hilligans.Network.Packet.Server.SDisconnectPacket;
import Hilligans.Network.PacketBase;
import Hilligans.Network.PacketData;
import Hilligans.Network.ServerNetworkHandler;
import Hilligans.ServerMain;
import io.netty.channel.ChannelHandlerContext;

public class STokenValid extends PacketBase {

    String username;
    String uuid;
    boolean valid;
    String tempid;

    public STokenValid() {
        super(2);
    }

    @Override
    public void encode(PacketData packetData) {}

    @Override
    public void decode(PacketData packetData) {
        username = packetData.readString();
        uuid = packetData.readString();
        valid = packetData.readBoolean();
        tempid = packetData.readString();
    }

    @Override
    public void handle() {
        ChannelHandlerContext oldCtx = ServerMain.getServer().playerQueue.get(tempid).typeA;
        CHandshakePacket packet = ServerMain.getServer().waitingPlayers.get(oldCtx);
        ServerMain.getServer().waitingPlayers.remove(oldCtx);
        ServerMain.getServer().playerQueue.remove(tempid);
        //System.out.println(this.toString());
        if(valid) {
            CHandshakePacket.handlePlayer(packet.name, packet.version, oldCtx, uuid);
        } else {
            ServerNetworkHandler.sendPacketClose(new SDisconnectPacket("Could not authorize you account"),oldCtx);
        }
    }

    @Override
    public String toString() {
        return "STokenValid{" +
                "username='" + username + '\'' +
                ", uuid='" + uuid + '\'' +
                ", valid=" + valid +
                ", tempid='" + tempid + '\'' +
                '}';
    }
}
