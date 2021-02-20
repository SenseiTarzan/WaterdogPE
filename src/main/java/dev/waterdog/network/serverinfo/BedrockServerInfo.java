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

package dev.waterdog.network.serverinfo;

import dev.waterdog.network.session.BedrockDownstream;
import dev.waterdog.network.session.DownstreamConnection;
import dev.waterdog.network.protocol.ProtocolVersion;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * This is default ServerInfo implementation which uses standard UDP RakNet connection.
 */
public class BedrockServerInfo extends ServerInfo {

    public BedrockServerInfo(String serverName, InetSocketAddress address, InetSocketAddress publicAddress) {
        super(serverName, address, publicAddress);
    }

    @Override
    public CompletableFuture<DownstreamConnection> bindNewConnection(ProtocolVersion protocol) {
        BedrockDownstream connection =  new BedrockDownstream(this);
        return connection.bindDownstream(protocol);
    }
}
