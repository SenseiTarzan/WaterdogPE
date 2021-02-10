/*
 * Copyright 2021 WaterdogTEAM
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.waterdog.network.session;

import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.BedrockClient;
import com.nukkitx.protocol.bedrock.BedrockClientSession;
import com.nukkitx.protocol.bedrock.packet.SetLocalPlayerAsInitializedPacket;
import com.nukkitx.protocol.bedrock.packet.StopSoundPacket;
import dev.waterdog.event.defaults.TransferCompleteEvent;
import dev.waterdog.network.ServerInfo;
import dev.waterdog.network.bridge.TransferBatchBridge;
import dev.waterdog.network.rewrite.types.RewriteData;
import dev.waterdog.player.PlayerRewriteUtils;
import dev.waterdog.player.ProxiedPlayer;

public class TransferCallback {

    private final ProxiedPlayer player;
    private final BedrockClient client;
    private final ServerInfo targetServer;

    public TransferCallback(ProxiedPlayer player, BedrockClient client, ServerInfo targetServer) {
        this.player = player;
        this.client = client;
        this.targetServer = targetServer;
    }

    public BedrockClientSession getDownstream() {
        return this.client.getSession();
    }

    public TransferBatchBridge getBatchBridge() {
        if (this.getDownstream().getBatchHandler() instanceof TransferBatchBridge) {
            return  (TransferBatchBridge) this.getDownstream().getBatchHandler();
        }
        return null;
    }

    public void onTransferAccepted() {
        RewriteData rewriteData = this.player.getRewriteData();
        rewriteData.setDimension(PlayerRewriteUtils.determineDimensionId(rewriteData.getDimension()));
        PlayerRewriteUtils.injectDimensionChange(this.player.getUpstream(), rewriteData.getDimension(), rewriteData.getSpawnPosition());
        PlayerRewriteUtils.injectPosition(this.player.getUpstream(), rewriteData.getSpawnPosition(), Vector3f.ZERO, rewriteData.getEntityId());

        PlayerRewriteUtils.injectRemoveAllEffects(this.player.getUpstream(), rewriteData.getEntityId());
        PlayerRewriteUtils.injectClearWeather(this.player.getUpstream());
        PlayerRewriteUtils.injectGameRules(this.player.getUpstream(), rewriteData.getGameRules());
    }

    public void onTransferComplete() {
        RewriteData rewriteData = this.player.getRewriteData();
        PlayerRewriteUtils.injectChunkPublisherUpdate(this.player.getUpstream(), rewriteData.getSpawnPosition().toInt(), rewriteData.getChunkRadiusSize());

        TransferBatchBridge batchBridge = this.getBatchBridge();
        if (batchBridge != null) { // Allow transfer queue to be sent
            batchBridge.setDimLockActive(false);
        }

        SetLocalPlayerAsInitializedPacket initializedPacket = new SetLocalPlayerAsInitializedPacket();
        initializedPacket.setRuntimeEntityId(rewriteData.getOriginalEntityId());
        this.getDownstream().sendPacket(initializedPacket);
        this.getDownstream().sendPacket(rewriteData.getChunkRadius());

        StopSoundPacket soundPacket = new StopSoundPacket();
        soundPacket.setSoundName("portal.travel");
        soundPacket.setStoppingAllSound(true);
        this.player.sendPacketImmediately(soundPacket);

        ServerConnection oldServer = this.player.getServer();
        oldServer.getInfo().removePlayer(this.player);
        oldServer.disconnect();

        this.targetServer.addPlayer(this.player);
        this.player.setPendingConnection(null);

        ServerConnection server = new ServerConnection(this.client, this.getDownstream(), this.targetServer);
        SessionInjections.injectPostDownstreamHandlers(server, this.player);

        if (batchBridge != null) {
            // Make sure queue will be sent
            batchBridge.flushQueue(this.getDownstream());
        }

        this.player.setServer(server);
        this.player.setAcceptPlayStatus(true);

        TransferCompleteEvent event = new TransferCompleteEvent(oldServer, server, this.player);
        this.player.getProxy().getEventManager().callEvent(event);
    }
}
