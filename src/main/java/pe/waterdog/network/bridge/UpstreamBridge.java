/**
 * Copyright 2020 WaterdogTEAM
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pe.waterdog.network.bridge;

import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.BedrockSession;
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import pe.waterdog.network.rewrite.RewriteMaps;
import pe.waterdog.player.ProxiedPlayer;
import pe.waterdog.utils.exceptions.CancelSignalException;

public class UpstreamBridge extends ProxyBatchBridge {

    public UpstreamBridge(ProxiedPlayer player, BedrockSession session) {
        super(player, session);
    }

    @Override
    public boolean handlePacket(BedrockPacket packet, BedrockPacketHandler handler) throws CancelSignalException {
        boolean changed = super.handlePacket(packet, handler);

        RewriteMaps rewriteMaps = this.player.getRewriteMaps();
        boolean rewrote = rewriteMaps.getItemMap() != null && rewriteMaps.getItemMap().doRewrite(packet);

        boolean pluginHandled = false;
        if (this.player.getPluginUpstreamHandler() != null) {
            pluginHandled = this.player.getPluginUpstreamHandler().handlePacket(packet);
        }
        return changed || rewrote || pluginHandled;
    }
}
