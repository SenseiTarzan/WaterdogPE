/*
 * Copyright 2021 WaterdogTEAM
 * Licensed under the GNU General Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.waterdog.network.session;

import com.google.common.base.Preconditions;
import com.nukkitx.protocol.bedrock.BedrockClient;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.BedrockSession;
import dev.waterdog.ProxyServer;
import dev.waterdog.network.bridge.DownstreamBridge;
import dev.waterdog.network.bridge.TransferBatchBridge;
import dev.waterdog.network.bridge.UpstreamBridge;
import dev.waterdog.network.downstream.InitialHandler;
import dev.waterdog.network.downstream.SwitchDownstreamHandler;
import dev.waterdog.network.protocol.ProtocolVersion;
import dev.waterdog.network.serverinfo.ServerInfo;
import dev.waterdog.player.ProxiedPlayer;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Default DownstreamConnection implementation which uses standard UDP RakNet connection based on ProtocolLib.
 */
public class BedrockDownstream implements DownstreamConnection {

    private final ServerInfo serverInfo;
    private BedrockClient client;
    private BedrockDownstreamSession session;

    public BedrockDownstream(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    @Override
    public CompletableFuture<DownstreamConnection> bindDownstream(ProtocolVersion protocol) {
        CompletableFuture<BedrockClient> future =  ProxyServer.getInstance().bindClient(protocol);
        return future.thenApply(client -> {
            this.client = client;
            return this;
        });
    }

    @Override
    public CompletableFuture<DownstreamSession> connect(InetSocketAddress address, long timeout, TimeUnit unit) {
        Preconditions.checkNotNull(this.client, "Client was not initialized!");
        return this.client.connect(address, timeout, unit).thenApply(downstream -> (this.session = new BedrockDownstreamSession(downstream)));
    }

    @Override
    public void onDownstreamConnected(ProxiedPlayer player) {
        BedrockSession upstream = player.getUpstream();
        BedrockSession downstream = this.session.getSession();
        if (player.getServer() == null) {
            ServerConnection server = new ServerConnection(this, this.session, this.serverInfo);
            player.onInitialServerConnected(server);

            downstream.setPacketHandler(new InitialHandler(player));
            downstream.setBatchHandler(new DownstreamBridge(player, upstream));
            upstream.setBatchHandler(new UpstreamBridge(player, downstream));
        } else {
            downstream.setPacketHandler(new SwitchDownstreamHandler(player, this.serverInfo, this));
            downstream.setBatchHandler(new TransferBatchBridge(player, upstream));
        }

        downstream.setPacketCodec(player.getProtocol().getCodec());
        downstream.sendPacketImmediately(player.getLoginData().getLoginPacket());
        downstream.setLogging(true);

        SessionInjections.injectNewDownstream(downstream, player, this.serverInfo, this.client);
    }

    @Override
    public void sendPacket(BedrockPacket packet) {
        assert this.session != null;
        this.session.sendPacket(packet);
    }

    @Override
    public void sendPacketImmediately(BedrockPacket packet) {
        assert this.session != null;
        this.session.sendPacketImmediately(packet);
    }

    @Override
    public InetSocketAddress getBindAddress() {
        Preconditions.checkNotNull(this.client, "Client was not initialized!");
        return this.client.getBindAddress();
    }

    @Override
    public void close(boolean force) {
        this.client.close(force);
    }

    @Override
    public ServerInfo getServerInfo() {
        return this.serverInfo;
    }

    @Override
    public BedrockDownstreamSession getSession() {
        return this.session;
    }
}
