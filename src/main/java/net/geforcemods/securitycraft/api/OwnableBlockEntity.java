package net.geforcemods.securitycraft.api;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.blocks.reinforced.IReinforcedBlock;
import net.geforcemods.securitycraft.util.ITickingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Used to give this tile entity an owner
 */
public class OwnableBlockEntity extends BlockEntity implements IOwnable, ITickingBlockEntity {

	private Owner owner = new Owner();

	public OwnableBlockEntity(BlockPos pos, BlockState state)
	{
		this(SCContent.beTypeOwnable, pos, state);
	}

	public OwnableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	boolean rand = false;
	int ticks = 0;
	boolean converted = false;
	@Override
	public void tick(Level level, BlockPos pos, BlockState state)
	{
		if(!rand)
		{
			ticks = level.getRandom().nextInt(60);
			rand = true;
		}
		else if(!converted && --ticks <= 0)
		{
			Block block = getBlockState().getBlock();

			if(block instanceof IReinforcedBlock)
				level.setBlockAndUpdate(pos, ((IReinforcedBlock)block).getConvertedState(getBlockState()));
			converted = true;
		}
	}

	/**
	 * Writes a tile entity to NBT.
	 * @return
	 */
	@Override
	public CompoundTag save(CompoundTag tag)
	{
		super.save(tag);

		if(owner != null){
			tag.putString("owner", owner.getName());
			tag.putString("ownerUUID", owner.getUUID());
		}

		return tag;
	}

	/**
	 * Reads a tile entity from NBT.
	 */
	@Override
	public void load(CompoundTag tag)
	{
		super.load(tag);

		if (tag.contains("owner"))
			owner.setOwnerName(tag.getString("owner"));

		if (tag.contains("ownerUUID"))
			owner.setOwnerUUID(tag.getString("ownerUUID"));
	}

	@Override
	public CompoundTag getUpdateTag()
	{
		return save(new CompoundTag());
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return new ClientboundBlockEntityDataPacket(worldPosition, 1, getUpdateTag());
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
		load(packet.getTag());
	}

	@Override
	public Owner getOwner(){
		return owner;
	}

	@Override
	public void setOwner(String uuid, String name) {
		owner.set(uuid, name);
	}
}
