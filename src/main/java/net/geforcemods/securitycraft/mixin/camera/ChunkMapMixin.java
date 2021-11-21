package net.geforcemods.securitycraft.mixin.camera;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.geforcemods.securitycraft.entity.camera.SecurityCamera;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

/**
 * Tracks chunks loaded by cameras to make them being sent to the client
 */
@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {
	@Shadow
	int viewDistance;

	@Shadow
	protected abstract void updateChunkTracking(ServerPlayer player, ChunkPos chunkPos, Packet<?>[] packetCache, boolean wasLoaded, boolean load);

	@Redirect(method = "checkerboardDistance(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/server/level/ServerPlayer;Z)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getLastSectionPos()Lnet/minecraft/core/SectionPos;"))
	private static SectionPos redirectLastSectionPos(ServerPlayer player) {
		if (PlayerUtils.isPlayerMountedOnCamera(player))
			return SectionPos.of(player.getCamera());

		return player.getLastSectionPos();
	}

	@Inject(method = "move", at = @At(value = "TAIL"))
	private void trackCameraLoadedChunks(ServerPlayer player, CallbackInfo callback) {
		if (PlayerUtils.isPlayerMountedOnCamera(player)) {
			SectionPos pos = SectionPos.of(player.getCamera());
			SecurityCamera camera = ((SecurityCamera)player.getCamera());

			for(int i = pos.x() - viewDistance; i <= pos.x() + viewDistance; ++i) {
				for(int j = pos.z() - viewDistance; j <= pos.z() + viewDistance; ++j) {
					ChunkPos chunkPos = new ChunkPos(i, j);

					updateChunkTracking(player, chunkPos, new Packet[2], camera.hasLoadedChunks(), true);
				}
			}

			camera.setHasLoadedChunks(viewDistance);
		}
	}
}
