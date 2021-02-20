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

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.TreeMap;

/**
 * This is a wrapper class for a map mapping all the server names to corresponding ServerInfo instances.
 * This class is required for configuration auto parsing
 */
public class ServerList {

    private final TreeMap<String, ServerEntry> serverList = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public ServerEntry get(String name) {
        return this.serverList.get(name);
    }

    public ServerEntry putIfAbsent(String name, ServerEntry info) {
        return this.serverList.putIfAbsent(name, info);
    }

    public ServerEntry remove(String name) {
        return this.serverList.remove(name);
    }

    public ServerEntry put(String name, ServerEntry info) {
        return this.serverList.put(name, info);
    }

    public Collection<ServerEntry> values() {
        return this.serverList.values();
    }

    public ServerList initEmpty() {
        this.putIfAbsent("lobby1", new ServerEntry("lobby1",
                new InetSocketAddress("127.0.0.1", 19133),
                new InetSocketAddress("play.myserver.com", 19133),
                null));
        return this;
    }
}
