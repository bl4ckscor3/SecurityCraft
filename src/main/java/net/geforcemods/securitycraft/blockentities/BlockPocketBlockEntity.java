package net.geforcemods.securitycraft.blockentities;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.SecurityCraftBlockEntity;
import net.geforcemods.securitycraft.util.IBlockPocket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BlockPocketBlockEntity extends SecurityCraftBlockEntity
{
	private BlockPocketManagerBlockEntity manager;
	private BlockPos managerPos;

	public BlockPocketBlockEntity(BlockPos pos, BlockState state)
	{
		super(SCContent.beTypeBlockPocket, pos, state);
	}

	public void setManager(BlockPocketManagerBlockEntity manager)
	{
		this.manager = manager;
		managerPos = manager.getBlockPos();
	}

	public void removeManager()
	{
		managerPos = null;
		manager = null;
	}

	public BlockPocketManagerBlockEntity getManager()
	{
		return manager;
	}

	@Override
	public void tick(Level world, BlockPos pos, BlockState state)
	{
		super.tick(world, pos, state);

		if(manager == null && managerPos != null)
		{
			if(world.getBlockEntity(managerPos) instanceof BlockPocketManagerBlockEntity manager)
				this.manager = manager;
		}
	}

	@Override
	public void onTileEntityDestroyed()
	{
		super.onTileEntityDestroyed();

		if(level.isLoaded(worldPosition) && manager != null && !(level.getBlockState(worldPosition).getBlock() instanceof IBlockPocket))
			manager.disableMultiblock();
	}

	@Override
	public CompoundTag save(CompoundTag tag)
	{
		if(manager != null)
			tag.putLong("ManagerPos", manager.getBlockPos().asLong());
		return super.save(tag);
	}

	@Override
	public void load(CompoundTag tag)
	{
		super.load(tag);

		if(tag.contains("ManagerPos"))
			managerPos = BlockPos.of(tag.getLong("ManagerPos"));
	}
}