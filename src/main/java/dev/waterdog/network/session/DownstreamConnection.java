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
import dev.waterdog.network.protocol.ProtocolVersion;
import dev.waterdog.network.serverinfo.ServerInfo;
import dev.waterdog.player.ProxiedPlayer;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Interface which is used to create custom downstream connection.
 * This allows custom packet serialization and opens much more options tu the future.
 */
public interface DownstreamConnection {

    CompletableFuture<DownstreamConnection> bindDownstream(ProtocolVersion protocol);
    CompletableFuture<DownstreamSession> connect(InetSocketAddress address, long timeout, TimeUnit unit);

    void sendPacket(BedrockPacket packet);
    void sendPacketImmediately(BedrockPacket packet);

    InetSocketAddress getBindAddress();

    default void close() {
        this.close(false);
    }

    void close(boolean force);

    void onDownstreamConnected(ProxiedPlayer player);

    ServerInfo getServerInfo();
    DownstreamSession getSession();
}
