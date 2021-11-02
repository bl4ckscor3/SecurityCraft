package net.geforcemods.securitycraft.api;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.blocks.reinforced.IReinforcedBlock;
import net.geforcemods.securitycraft.network.server.RequestTEOwnableUpdate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

/**
 * Used to give this tile entity an owner
 */
public class OwnableTileEntity extends TileEntity implements IOwnable, ITickableTileEntity {

	private Owner owner = new Owner();

	public OwnableTileEntity()
	{
		this(SCContent.teTypeOwnable);
	}
	boolean rand = false;
	int ticks = 0;
	boolean converted = false;
	@Override
	public void tick()
	{
		if(!rand)
		{
			ticks = world.getRandom().nextInt(60);
			rand = true;
		}
		else if(!converted && --ticks <= 0)
		{
			Block block = getBlockState().getBlock();

			if(block instanceof IReinforcedBlock)
				world.setBlockState(pos, ((IReinforcedBlock)block).getConvertedState(getBlockState()));
			converted = true;
		}
	}

	public OwnableTileEntity(TileEntityType<?> type)
	{
		super(type);
	}

	/**
	 * Writes a tile entity to NBT.
	 * @return
	 */
	@Override
	public CompoundNBT write(CompoundNBT tag)
	{
		super.write(tag);

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
	public void read(BlockState state, CompoundNBT tag)
	{
		super.read(state, tag);

		if (tag.contains("owner"))
			owner.setOwnerName(tag.getString("owner"));

		if (tag.contains("ownerUUID"))
			owner.setOwnerUUID(tag.getString("ownerUUID"));
	}

	@Override
	public CompoundNBT getUpdateTag()
	{
		return write(new CompoundNBT());
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(pos, 1, getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
		read(getBlockState(), packet.getNbtCompound());
	}

	@Override
	public Owner getOwner(){
		return owner;
	}

	@Override
	public void setOwner(String uuid, String name) {
		owner.set(uuid, name);
	}

	@Override
	public void onLoad()
	{
		if(world.isRemote)
			SecurityCraft.channel.sendToServer(new RequestTEOwnableUpdate(this));
	}
}
