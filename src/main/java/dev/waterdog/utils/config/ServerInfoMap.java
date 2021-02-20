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

package dev.waterdog.utils.config;

import dev.waterdog.network.serverinfo.BedrockServerInfo;
import dev.waterdog.network.serverinfo.ServerInfo;

import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ServerInfoMap {

    public static final Class<? extends ServerInfo> DEFAULT_TYPE = BedrockServerInfo.class;

    private final Map<String, Class<? extends ServerInfo>> serverInfoTypes = new HashMap<>();
    private final TreeMap<String, ServerInfo> serverList = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public ServerInfoMap() {
        this.addServerInfoType("bedrock", DEFAULT_TYPE);
    }

    public ServerInfo createServerInfo(String serverName, InetSocketAddress address, InetSocketAddress publicAddress, String serverType) throws Exception {
        Class<? extends ServerInfo> clazz = this.serverInfoTypes.get(serverType);
        if (clazz == null) {
            clazz = DEFAULT_TYPE;
        }

        Constructor<? extends ServerInfo> constructor = clazz.getConstructor(String.class, InetSocketAddress.class, InetSocketAddress.class);
        return constructor.newInstance(serverName, address, publicAddress);
    }

    public void addServerInfoType(String typeName, Class<? extends ServerInfo> clazz) {
        this.serverInfoTypes.put(typeName, clazz);
    }

    public Class<? extends ServerInfo> getServerInfoType(String typeName) {
        return this.serverInfoTypes.get(typeName);
    }

    public void removeServerInfoType(String typeName) {
        this.serverInfoTypes.remove(typeName);
    }

    public ServerInfo get(String name) {
        return this.serverList.get(name);
    }

    public ServerInfo putIfAbsent(String name, ServerInfo info) {
        return this.serverList.putIfAbsent(name, info);
    }

    public ServerInfo remove(String name) {
        return this.serverList.remove(name);
    }

    public ServerInfo put(String name, ServerInfo info) {
        return this.serverList.put(name, info);
    }

    public Collection<ServerInfo> values() {
        return this.serverList.values();
    }
}
