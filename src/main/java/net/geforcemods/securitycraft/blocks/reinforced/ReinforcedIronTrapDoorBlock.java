package net.geforcemods.securitycraft.blocks.reinforced;

import javax.annotation.Nullable;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.OwnableBlockEntity;
import net.geforcemods.securitycraft.misc.OwnershipEvent;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.MinecraftForge;

public class ReinforcedIronTrapDoorBlock extends TrapDoorBlock implements IReinforcedBlock, EntityBlock {

	public ReinforcedIronTrapDoorBlock(Block.Properties properties) {
		super(properties);
	}

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos neighbor, boolean flag)
	{
		boolean hasActiveSCBlock = BlockUtils.hasActiveSCBlockNextTo(world, pos);

		if(hasActiveSCBlock != state.getValue(OPEN))
		{
			world.setBlock(pos, state.setValue(OPEN, hasActiveSCBlock), 2);
			playSound((Player)null, world, pos, hasActiveSCBlock);
		}
	}

	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{
		if(placer instanceof Player player)
			MinecraftForge.EVENT_BUS.post(new OwnershipEvent(world, pos, player));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockState blockstate = this.defaultBlockState();
		FluidState fluidstate = ctx.getLevel().getFluidState(ctx.getClickedPos());
		Direction direction = ctx.getClickedFace();

		if (!ctx.replacingClickedOnBlock() && direction.getAxis().isHorizontal()) {
			blockstate = blockstate.setValue(FACING, direction).setValue(HALF, ctx.getClickLocation().y - ctx.getClickedPos().getY() > 0.5D ? Half.TOP : Half.BOTTOM);
		} else {
			blockstate = blockstate.setValue(FACING, ctx.getHorizontalDirection().getOpposite()).setValue(HALF, direction == Direction.UP ? Half.BOTTOM : Half.TOP);
		}

		if (BlockUtils.hasActiveSCBlockNextTo(ctx.getLevel(), ctx.getClickedPos())) {
			blockstate = blockstate.setValue(OPEN, true).setValue(POWERED, true);
		}

		return blockstate.setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
	{
		return InteractionResult.FAIL;
	}

	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving)
	{
		super.onRemove(state, world, pos, newState, isMoving);

		if(!(newState.getBlock() instanceof ReinforcedIronTrapDoorBlock))
			world.removeBlockEntity(pos);
	}

	@Override
	public boolean triggerEvent(BlockState state, Level world, BlockPos pos, int id, int param)
	{
		super.triggerEvent(state, world, pos, id, param);
		BlockEntity tileentity = world.getBlockEntity(pos);
		return tileentity == null ? false : tileentity.triggerEvent(id, param);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new OwnableBlockEntity(pos, state);
	}

	@Override
	public Block getVanillaBlock()
	{
		return Blocks.IRON_TRAPDOOR;
	}

	@Override
	public BlockState getConvertedState(BlockState vanillaState)
	{
		return getVanillaBlock().defaultBlockState().setValue(FACING, vanillaState.getValue(FACING)).setValue(OPEN, vanillaState.getValue(OPEN)).setValue(HALF, vanillaState.getValue(HALF)).setValue(POWERED, vanillaState.getValue(POWERED)).setValue(WATERLOGGED, vanillaState.getValue(WATERLOGGED));
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return world.isClientSide ? null : createTickerHelper(type, SCContent.beTypeOwnable, WorldUtils::blockEntityTicker);
	}
	@Nullable
	public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> p_152133_, BlockEntityType<E> p_152134_, BlockEntityTicker<? super E> p_152135_) {
		return p_152134_ == p_152133_ ? (BlockEntityTicker<A>)p_152135_ : null;
	}
}