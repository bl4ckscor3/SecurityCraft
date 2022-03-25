package net.geforcemods.securitycraft.network.server;

import java.util.function.Supplier;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.blocks.FrameBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

public class ActivateFrame {
	private BlockPos pos;

	public ActivateFrame() {}

	public ActivateFrame(BlockPos pos) {
		this.pos = pos;
	}

	public static void encode(ActivateFrame message, FriendlyByteBuf buf) {
		buf.writeLong(message.pos.asLong());
	}

	public static ActivateFrame decode(FriendlyByteBuf buf) {
		ActivateFrame message = new ActivateFrame();

		message.pos = BlockPos.of(buf.readLong());
		return message;
	}

	public static void onMessage(ActivateFrame message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Level level = ctx.get().getSender().level;
			BlockState state = level.getBlockState(message.pos);

			if (state.getBlock() == SCContent.FRAME.get()) {
				if (level.getBlockEntity(message.pos) instanceof IOwnable ownable && ownable.getOwner().isOwner(ctx.get().getSender()))
					level.setBlockAndUpdate(message.pos, state.setValue(FrameBlock.ACTIVE, true));
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
