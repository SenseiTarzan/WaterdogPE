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

package dev.waterdog.player;

import com.google.common.base.Preconditions;
import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.network.VarInts;
import com.nukkitx.protocol.bedrock.BedrockSession;
import com.nukkitx.protocol.bedrock.data.GameRuleData;
import com.nukkitx.protocol.bedrock.data.GameType;
import com.nukkitx.protocol.bedrock.data.LevelEventType;
import com.nukkitx.protocol.bedrock.data.entity.EntityData;
import com.nukkitx.protocol.bedrock.packet.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Collection of functions to remove various client-sided data sets when switching servers.
 * For example removing client-sided weather, effects, effect particles etc..
 */
public class PlayerRewriteUtils {

    public static final int DIMENSION_OVERWORLD = 0;
    public static final int DIMENSION_NETHER = 1;

    private static final byte[] fakeChunkData;

    static {
        // Here we create hardcoded "empty" chunk which is accepted by client
        // Because client does not accept empty array list we try to hardcode this
        // Keep in mind that this CAN change with newer versions!
        final ByteBuf chunkdata = Unpooled.buffer();
        chunkdata.writeByte(1); // 1 section
        chunkdata.writeByte(8); // New subchunk version!
        chunkdata.writeByte(1); // Zero block storages :O
        chunkdata.writeByte((1 << 1) | 1);  // Runtimeflag and palette id.
        chunkdata.writeZero(512);
        VarInts.writeInt(chunkdata, 1); // Palette size
        VarInts.writeInt(chunkdata, 0); // Air
        chunkdata.writeZero(512); // Map height
        chunkdata.writeZero(256); // Biome data.
        chunkdata.writeByte(0); // Borders

        ByteBuf buffer = Unpooled.buffer();
        VarInts.writeInt(buffer, chunkdata.readUnsignedByte());
        buffer.writeByte(0);
        VarInts.writeInt(buffer, chunkdata.readableBytes());
        buffer.writeBytes(chunkdata);

        fakeChunkData = new byte[buffer.readableBytes()];
        buffer.readBytes(fakeChunkData);
        Preconditions.checkArgument(fakeChunkData.length > 0);
    }

    public static long rewriteId(long from, long rewritten, long origin) {
        return from == origin ? rewritten : (from == rewritten ? origin : from);
    }

    public static int determineDimensionId(int from) {
        return from == DIMENSION_OVERWORLD ? DIMENSION_NETHER : DIMENSION_OVERWORLD;
    }

    public static void injectChunkPublisherUpdate(BedrockSession session, Vector3i defaultSpawn, int radius) {
        if (session == null || session.isClosed()){
            return;
        }
        NetworkChunkPublisherUpdatePacket packet = new NetworkChunkPublisherUpdatePacket();
        packet.setPosition(defaultSpawn);
        packet.setRadius(radius);
        session.sendPacket(packet);
    }

    public static void injectGameMode(BedrockSession session, GameType gameMode) {
        if (session == null || session.isClosed()){
            return;
        }
        SetPlayerGameTypePacket packet = new SetPlayerGameTypePacket();
        packet.setGamemode(gameMode.ordinal());
        session.sendPacket(packet);
    }

    public static void injectGameRules(BedrockSession session, List<GameRuleData<?>> gameRules) {
        if (session == null || session.isClosed()){
            return;
        }
        GameRulesChangedPacket packet = new GameRulesChangedPacket();
        packet.getGameRules().addAll(gameRules);
        session.sendPacket(packet);
    }

    public static void injectClearWeather(BedrockSession session) {
        if (session == null || session.isClosed()){
            return;
        }
        LevelEventPacket stopRain = new LevelEventPacket();
        stopRain.setType(LevelEventType.STOP_RAINING);
        stopRain.setData(10000);
        stopRain.setPosition(Vector3f.ZERO);
        session.sendPacketImmediately(stopRain);

        LevelEventPacket stopThunder = new LevelEventPacket();
        stopThunder.setData(0);
        stopThunder.setPosition(Vector3f.ZERO);
        stopThunder.setType(LevelEventType.STOP_THUNDERSTORM);
        session.sendPacket(stopThunder);
    }

    public static void injectSetDifficulty(BedrockSession session, int difficulty) {
        if (session == null || session.isClosed()){
            return;
        }
        SetDifficultyPacket packet = new SetDifficultyPacket();
        packet.setDifficulty(difficulty);
        session.sendPacket(packet);
    }

    public static void injectRemoveEntity(BedrockSession session, long runtimeId) {
        if (session == null || session.isClosed()){
            return;
        }
        RemoveEntityPacket packet = new RemoveEntityPacket();
        packet.setUniqueEntityId(runtimeId);
        session.sendPacket(packet);
    }

    public static void injectRemoveAllPlayers(BedrockSession session, Collection<UUID> playerList) {
        if (session == null || session.isClosed()){
            return;
        }
        PlayerListPacket packet = new PlayerListPacket();
        packet.setAction(PlayerListPacket.Action.REMOVE);
        List<PlayerListPacket.Entry> entries = new ArrayList<>();
        for (UUID uuid : playerList) {
            entries.add(new PlayerListPacket.Entry(uuid));
        }
        packet.getEntries().addAll(entries);
        session.sendPacket(packet);
    }

    public static void injectRemoveAllEffects(BedrockSession session, long runtimeId) {
        if (session == null || session.isClosed()){
            return;
        }
        for (int i = 0; i < 28; i++) {
            injectRemoveEntityEffect(session, runtimeId, i);
        }
        SetEntityDataPacket packet = new SetEntityDataPacket();
        packet.getMetadata().putShort(EntityData.POTION_AUX_VALUE, 0);
        packet.getMetadata().putInt(EntityData.EFFECT_COLOR, 0);
        packet.getMetadata().putByte(EntityData.EFFECT_AMBIENT, (byte) 0);
        packet.setRuntimeEntityId(runtimeId);
        session.sendPacket(packet);
    }

    public static void injectRemoveEntityEffect(BedrockSession session, long runtimeId, int effect) {
        MobEffectPacket packet = new MobEffectPacket();
        packet.setRuntimeEntityId(runtimeId);
        packet.setEffectId(effect);
        packet.setEvent(MobEffectPacket.Event.REMOVE);
        session.sendPacket(packet);
    }

    public static void injectRemoveObjective(BedrockSession session, String objectiveId) {
        if (session == null || session.isClosed()){
            return;
        }
        RemoveObjectivePacket packet = new RemoveObjectivePacket();
        packet.setObjectiveId(objectiveId);
        session.sendPacket(packet);
    }

    public static void injectRemoveBossbar(BedrockSession session, long bossbarId) {
        if (session == null || session.isClosed()){
            return;
        }
        BossEventPacket packet = new BossEventPacket();
        packet.setAction(BossEventPacket.Action.REMOVE);
        packet.setBossUniqueEntityId(bossbarId);
        session.sendPacket(packet);
    }

    public static void injectPosition(BedrockSession session, Vector3f position, Vector3f rotation, long runtimeId){
        if (session == null || session.isClosed()){
            return;
        }
        MovePlayerPacket packet = new MovePlayerPacket();
        packet.setPosition(position);
        packet.setRuntimeEntityId(runtimeId);
        packet.setRotation(rotation);
        packet.setMode(MovePlayerPacket.Mode.RESPAWN);
        session.sendPacket(packet);
    }

    public static void injectDimensionChange(BedrockSession session, int dimensionId, Vector3f position) {
        if (session == null || session.isClosed()){
            return;
        }
        ChangeDimensionPacket packet = new ChangeDimensionPacket();
        packet.setPosition(position);
        packet.setRespawn(true);
        packet.setDimension(dimensionId);
        session.sendPacket(packet);
        injectChunkPublisherUpdate(session, position.toInt(), 4);
        injectEmptyChunks(session, position, 4);
    }

    public static void injectEmptyChunks(BedrockSession session, Vector3f spawnPosition, int radius){
        int chunkPositionX = spawnPosition.getFloorX() >> 4;
        int chunkPositionZ = spawnPosition.getFloorZ() >> 4;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                injectEmptyChunk(session, chunkPositionX + x, chunkPositionZ + z);
            }
        }
    }

    public static void injectEmptyChunk(BedrockSession session, int chunkX, int chunkZ){
        LevelChunkPacket packet = new LevelChunkPacket();
        packet.setChunkX(chunkX);
        packet.setChunkZ(chunkZ);
        packet.setData(fakeChunkData);
        session.sendPacket(packet);
    }
}