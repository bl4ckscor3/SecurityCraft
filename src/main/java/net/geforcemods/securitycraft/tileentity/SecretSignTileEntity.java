package net.geforcemods.securitycraft.tileentity;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.api.ICustomizable;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.BooleanOption;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.blocks.SecretStandingSignBlock;
import net.geforcemods.securitycraft.blocks.SecretWallSignBlock;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.network.server.RequestTEOwnableUpdate;
import net.geforcemods.securitycraft.util.ModuleUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StandingSignBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;

public class SecretSignTileEntity extends SignTileEntity implements IOwnable, IModuleInventory, ICustomizable, ITickableTileEntity
{
	private Owner owner = new Owner();
	private BooleanOption isSecret = new BooleanOption("isSecret", true);
	private NonNullList<ItemStack> modules = NonNullList.<ItemStack>withSize(getMaxNumberOfModules(), ItemStack.EMPTY);

	@Override
	public TileEntityType<?> getType()
	{
		return SCContent.teTypeSecretSign;
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
					CompoundNBT tag = super.write(new CompoundNBT());

					world.setBlockState(pos, convertedBlock.getDefaultState().with(StandingSignBlock.ROTATION, getBlockState().get(StandingSignBlock.ROTATION)).with(StandingSignBlock.WATERLOGGED, getBlockState().get(StandingSignBlock.WATERLOGGED)));
					world.getTileEntity(pos).read(world.getBlockState(pos), tag);
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
					CompoundNBT tag = super.write(new CompoundNBT());

					world.setBlockState(pos, convertedBlock.getDefaultState().with(WallSignBlock.FACING, getBlockState().get(WallSignBlock.FACING)).with(WallSignBlock.WATERLOGGED, getBlockState().get(WallSignBlock.WATERLOGGED)));
					world.getTileEntity(pos).read(world.getBlockState(pos), tag);
				}
			}

			converted = true;
		}
	}
	/**
	 * Writes a tile entity to NBT.
	 * @return
	 */
	@Override
	public CompoundNBT write(CompoundNBT tag)
	{
		super.write(tag);

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
	public void read(BlockState state, CompoundNBT tag)
	{
		super.read(state, tag);

		modules = readModuleInventory(tag);
		readOptions(tag);
		owner.setOwnerName(tag.getString("owner"));
		owner.setOwnerUUID(tag.getString("ownerUUID"));
	}

	@Override
	public TileEntity getTileEntity()
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

	public boolean isPlayerAllowedToSeeText(PlayerEntity player) {
		return !isSecret() || getOwner().isOwner(player) || ModuleUtils.isAllowed(this, player);
	}

	@Override
	public void onOptionChanged(Option<?> option)
	{
		world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		CompoundNBT tag = new CompoundNBT();
		write(tag);
		return new SUpdateTileEntityPacket(pos, 1, tag);
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
			SecurityCraft.channel.sendToServer(new RequestTEOwnableUpdate(getPos()));
	}
}
