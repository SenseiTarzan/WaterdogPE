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

import com.nukkitx.protocol.bedrock.BedrockClientSession;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import dev.waterdog.network.bridge.DownstreamBridge;
import dev.waterdog.network.bridge.UpstreamBridge;
import dev.waterdog.network.downstream.ConnectedDownstreamHandler;
import dev.waterdog.player.ProxiedPlayer;

import javax.crypto.SecretKey;
import java.net.InetSocketAddress;

public class BedrockDownstreamSession implements DownstreamSession {

    private final BedrockClientSession session;

    public BedrockDownstreamSession(BedrockClientSession session) {
        this.session = session;
    }

    @Override
    public void onDownstreamInitialize(ProxiedPlayer player, ServerConnection server) {
        this.setHardcodedBlockingId(player.getRewriteData().getShieldBlockingId());
        this.session.setPacketHandler(new ConnectedDownstreamHandler(player, server));
    }

    @Override
    public void onTransferCompleted(ProxiedPlayer player, ServerConnection server) {
        player.getUpstream().setBatchHandler(new UpstreamBridge(player, this.session));
        this.session.setBatchHandler(new DownstreamBridge(player, player.getUpstream()));
    }

    @Override
    public InetSocketAddress getAddress() {
        return this.session.getRealAddress();
    }

    @Override
    public long getLatency() {
        return this.session.getLatency();
    }

    @Override
    public void sendPacket(BedrockPacket packet) {
        this.session.sendPacket(packet);
    }

    @Override
    public void sendPacketImmediately(BedrockPacket packet) {
        this.session.sendPacketImmediately(packet);
    }

    @Override
    public void enableEncryption(SecretKey secretKey) {
        this.session.enableEncryption(secretKey);
    }

    @Override
    public boolean isEncrypted() {
        return this.session.isEncrypted();
    }

    @Override
    public void setHardcodedBlockingId(int runtimeId) {
        this.session.getHardcodedBlockingId().set(runtimeId);
    }

    @Override
    public int getHardcodedBlockingId() {
        return this.session.getHardcodedBlockingId().get();
    }

    @Override
    public boolean isClosed() {
        return this.session.isClosed();
    }

    @Override
    public void disconnect() {
        this.session.disconnect();
    }

    public BedrockClientSession getSession() {
        return this.session;
    }

    @Override
    public boolean isBedrock() {
        return true;
    }
}
