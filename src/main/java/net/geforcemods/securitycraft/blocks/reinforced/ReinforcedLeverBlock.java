package net.geforcemods.securitycraft.blocks.reinforced;

import net.geforcemods.securitycraft.misc.OwnershipEvent;
import net.geforcemods.securitycraft.tileentity.AllowlistOnlyTileEntity;
import net.geforcemods.securitycraft.util.ModuleUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeverBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class ReinforcedLeverBlock extends LeverBlock implements IReinforcedBlock {

	public ReinforcedLeverBlock(Properties properties)
	{
		super(properties);
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
		if(isAllowedToPress(world, pos, (AllowlistOnlyTileEntity)world.getTileEntity(pos), player))
			return super.onBlockActivated(state, world, pos, player, hand, result);
		return ActionResultType.FAIL;
	}

	public boolean isAllowedToPress(World world, BlockPos pos, AllowlistOnlyTileEntity te, PlayerEntity entity)
	{
		return te.getOwner().isOwner(entity) || ModuleUtils.isAllowed(te, entity);
	}

	@Override
	public Block getVanillaBlock()
	{
		return Blocks.LEVER;
	}

	@Override
	public BlockState getConvertedState(BlockState vanillaState)
	{
		return getVanillaBlock().getDefaultState().with(FACE, vanillaState.get(FACE)).with(HORIZONTAL_FACING, vanillaState.get(HORIZONTAL_FACING)).with(POWERED, vanillaState.get(POWERED));
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{
		if(placer instanceof PlayerEntity)
			MinecraftForge.EVENT_BUS.post(new OwnershipEvent(world, pos, (PlayerEntity)placer));
	}

	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return new AllowlistOnlyTileEntity();
	}
}
