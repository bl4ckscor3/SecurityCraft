package net.geforcemods.securitycraft.blockentities;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.ICustomizable;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.BooleanOption;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.blocks.SecretStandingSignBlock;
import net.geforcemods.securitycraft.blocks.SecretWallSignBlock;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.ITickingBlockEntity;
import net.geforcemods.securitycraft.util.ModuleUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SecretSignBlockEntity extends SignBlockEntity implements IOwnable, IModuleInventory, ICustomizable, ITickingBlockEntity
{
	private Owner owner = new Owner();
	private BooleanOption isSecret = new BooleanOption("isSecret", true);
	private NonNullList<ItemStack> modules = NonNullList.<ItemStack>withSize(getMaxNumberOfModules(), ItemStack.EMPTY);

	public SecretSignBlockEntity(BlockPos pos, BlockState state)
	{
		super(pos, state);
	}
	boolean rand = false;
	int ticks = 0;
	boolean converted = false;
	@Override
	public void tick(Level world, BlockPos pos, BlockState state)
	{
		if(!rand)
		{
			ticks = world.getRandom().nextInt(60);
			rand = true;
		}
		else if(!converted && --ticks <= 0)
		{
			Block block = getBlockState().getBlock();

			if(block instanceof SecretStandingSignBlock) {
				Block convertedBlock = null;

				if(block == SCContent.SECRET_OAK_SIGN.get())
					convertedBlock = Blocks.OAK_SIGN;
				else if(block == SCContent.SECRET_SPRUCE_SIGN.get())
					convertedBlock = Blocks.SPRUCE_SIGN;
				else if(block == SCContent.SECRET_BIRCH_SIGN.get())
					convertedBlock = Blocks.BIRCH_SIGN;
				else if(block == SCContent.SECRET_JUNGLE_SIGN.get())
					convertedBlock = Blocks.JUNGLE_SIGN;
				else if(block == SCContent.SECRET_ACACIA_SIGN.get())
					convertedBlock = Blocks.ACACIA_SIGN;
				else if(block == SCContent.SECRET_DARK_OAK_SIGN.get())
					convertedBlock = Blocks.DARK_OAK_SIGN;
				else if(block == SCContent.SECRET_CRIMSON_SIGN.get())
					convertedBlock = Blocks.CRIMSON_SIGN;
				else if(block == SCContent.SECRET_WARPED_SIGN.get())
					convertedBlock = Blocks.WARPED_SIGN;

				if(convertedBlock != null)
				{
					CompoundTag tag = super.save(new CompoundTag());

					world.setBlockAndUpdate(pos, convertedBlock.defaultBlockState().setValue(StandingSignBlock.ROTATION, getBlockState().getValue(StandingSignBlock.ROTATION)).setValue(StandingSignBlock.WATERLOGGED, getBlockState().getValue(StandingSignBlock.WATERLOGGED)));
					world.getBlockEntity(pos).load(tag);
				}
			}
			else if(block instanceof SecretWallSignBlock) {
				Block convertedBlock = null;

				if(block == SCContent.SECRET_OAK_WALL_SIGN.get())
					convertedBlock = Blocks.OAK_WALL_SIGN;
				else if(block == SCContent.SECRET_SPRUCE_WALL_SIGN.get())
					convertedBlock = Blocks.SPRUCE_WALL_SIGN;
				else if(block == SCContent.SECRET_BIRCH_WALL_SIGN.get())
					convertedBlock = Blocks.BIRCH_WALL_SIGN;
				else if(block == SCContent.SECRET_JUNGLE_WALL_SIGN.get())
					convertedBlock = Blocks.JUNGLE_WALL_SIGN;
				else if(block == SCContent.SECRET_ACACIA_WALL_SIGN.get())
					convertedBlock = Blocks.ACACIA_WALL_SIGN;
				else if(block == SCContent.SECRET_DARK_OAK_WALL_SIGN.get())
					convertedBlock = Blocks.DARK_OAK_WALL_SIGN;
				else if(block == SCContent.SECRET_CRIMSON_WALL_SIGN.get())
					convertedBlock = Blocks.CRIMSON_WALL_SIGN;
				else if(block == SCContent.SECRET_WARPED_WALL_SIGN.get())
					convertedBlock = Blocks.WARPED_WALL_SIGN;

				if(convertedBlock != null)
				{
					CompoundTag tag = super.save(new CompoundTag());

					world.setBlockAndUpdate(pos, convertedBlock.defaultBlockState().setValue(WallSignBlock.FACING, getBlockState().getValue(WallSignBlock.FACING)).setValue(WallSignBlock.WATERLOGGED, getBlockState().getValue(WallSignBlock.WATERLOGGED)));
					world.getBlockEntity(pos).load(tag);
				}
			}

			converted = true;
		}
	}
	@Override
	public BlockEntityType<?> getType()
	{
		return SCContent.beTypeSecretSign;
	}

	/**
	 * Writes a tile entity to NBT.
	 * @return
	 */
	@Override
	public CompoundTag save(CompoundTag tag)
	{
		super.save(tag);

		writeModuleInventory(tag);
		writeOptions(tag);

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

		modules = readModuleInventory(tag);
		readOptions(tag);
		owner.setOwnerName(tag.getString("owner"));
		owner.setOwnerUUID(tag.getString("ownerUUID"));
	}

	@Override
	public BlockEntity getTileEntity()
	{
		return this;
	}

	@Override
	public NonNullList<ItemStack> getInventory() {
		return modules;
	}

	@Override
	public ModuleType[] acceptedModules() {
		return new ModuleType[]{ModuleType.ALLOWLIST};
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[]{ isSecret };
	}

	public boolean isSecret() {
		return isSecret.get();
	}

	public boolean isPlayerAllowedToSeeText(Player player) {
		return !isSecret() || getOwner().isOwner(player) || ModuleUtils.isAllowed(this, player);
	}

	@Override
	public void onOptionChanged(Option<?> option)
	{
		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		CompoundTag tag = new CompoundTag();
		save(tag);
		return new ClientboundBlockEntityDataPacket(worldPosition, 1, tag);
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
