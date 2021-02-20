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

import com.nukkitx.protocol.bedrock.BedrockPacket;
import dev.waterdog.network.serverinfo.ServerInfo;

import java.net.InetSocketAddress;

/**
 * Class holding a connection between a Player (client) and a MC:BE Server (downstream).
 */
public class ServerConnection {

    private final ServerInfo serverInfo;

    private final DownstreamConnection connection;
    private final DownstreamSession downstream;

    public ServerConnection(DownstreamConnection connection, DownstreamSession session, ServerInfo serverInfo) {
        this.connection = connection;
        this.downstream = session;
        this.serverInfo = serverInfo;
    }

    public void sendPacket(BedrockPacket packet) {
        if (!this.downstream.isClosed()) {
            this.downstream.sendPacket(packet);
        }
    }

    public void disconnect() {
        this.disconnect(false);
    }

    /**
     * Safely close connection with downstream server.
     * @param force if block thread till everything is closed.
     */
    public void disconnect(boolean force) {
        this.connection.close(force);
    }

    public ServerInfo getInfo() {
        return this.serverInfo;
    }

    public DownstreamSession getDownstream() {
        return this.downstream;
    }

    public InetSocketAddress getAddress() {
        return this.downstream.getAddress();
    }

    public boolean isConnected() {
        return !this.downstream.isClosed();
    }
}
