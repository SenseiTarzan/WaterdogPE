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
import dev.waterdog.player.ProxiedPlayer;

import javax.crypto.SecretKey;
import java.net.InetSocketAddress;

public interface DownstreamSession {

    InetSocketAddress getAddress();
    long getLatency();

    void sendPacket(BedrockPacket packet);
    void sendPacketImmediately(BedrockPacket packet);

    default void enableEncryption(SecretKey secretKey) {
        // Implement if supported
    }

    default boolean isEncrypted() {
        return false;
    }

    default void setHardcodedBlockingId(int runtimeId) {
        // Implement if supported
    }

    default int getHardcodedBlockingId() {
        throw new UnsupportedOperationException("Hardcoded blocking is is not supported!");
    }

    /**
     * Called once player is successfully connected to downstream.
     * Note that this method is used during initial server connection adn during transfer connection to.
     * @param player which owns this downstream connection.
     * @param server ServerConnection which holds this session.
     */
    void onDownstreamInitialize(ProxiedPlayer player, ServerConnection server);

    /**
     * Called once player is successfully transferred to downstream.
     * This method is NOT used for initial connection.
     * @param player which was transferred.
     * @param server ServerConnection which holds this session.
     */
    void onTransferCompleted(ProxiedPlayer player, ServerConnection server);

    boolean isClosed();
    void disconnect();

    boolean isBedrock();
}
