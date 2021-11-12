package net.geforcemods.securitycraft.blocks.reinforced;

import java.util.List;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.blockentities.AllowlistOnlyBlockEntity;
import net.geforcemods.securitycraft.misc.OwnershipEvent;
import net.geforcemods.securitycraft.util.ModuleUtils;
import net.geforcemods.securitycraft.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;

public class ReinforcedPressurePlateBlock extends PressurePlateBlock implements IReinforcedBlock, EntityBlock
{
	private final Block vanillaBlock;

	public ReinforcedPressurePlateBlock(Sensitivity sensitivity, Block.Properties properties, Block vanillaBlock)
	{
		super(sensitivity, properties);

		this.vanillaBlock = vanillaBlock;
	}

	@Override
	public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity)
	{
		int redstoneStrength = getSignalForState(state);

		if(!world.isClientSide && redstoneStrength == 0 && entity instanceof Player player)
		{
			BlockEntity tile = world.getBlockEntity(pos);

			if(tile instanceof AllowlistOnlyBlockEntity te)
			{
				if(isAllowedToPress(world, pos, te, player))
					checkPressed(player, world, pos, state, redstoneStrength);
			}
		}
	}

	@Override
	protected int getSignalStrength(Level world, BlockPos pos)
	{
		AABB aabb = TOUCH_AABB.move(pos);
		List<? extends Entity> list;

		list = world.getEntities(null, aabb);

		if(!list.isEmpty())
		{
			BlockEntity tile = world.getBlockEntity(pos);

			if(tile instanceof AllowlistOnlyBlockEntity te)
			{
				for(Entity entity : list)
				{
					if(entity instanceof Player player && isAllowedToPress(world, pos, te, player))
						return 15;
				}
			}
		}

		return 0;
	}

	public boolean isAllowedToPress(Level world, BlockPos pos, AllowlistOnlyBlockEntity te, Player entity)
	{
		return te.getOwner().isOwner(entity) || ModuleUtils.isAllowed(te, entity);
	}

	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{
		if(placer instanceof Player)
			MinecraftForge.EVENT_BUS.post(new OwnershipEvent(world, pos, (Player)placer));
	}

	@Override
	public Block getVanillaBlock()
	{
		return vanillaBlock;
	}

	@Override
	public BlockState getConvertedState(BlockState vanillaState)
	{
		return getVanillaBlock().defaultBlockState();
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new AllowlistOnlyBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return BaseEntityBlock.createTickerHelper(type, SCContent.beTypeAllowlistOnly, WorldUtils::blockEntityTicker);
	}
}
