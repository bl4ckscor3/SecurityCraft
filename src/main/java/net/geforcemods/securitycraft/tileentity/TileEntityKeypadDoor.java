package net.geforcemods.securitycraft.tileentity;

import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.api.IPasswordProtected;
import net.geforcemods.securitycraft.blocks.BlockKeypad;
import net.geforcemods.securitycraft.blocks.BlockKeypadDoor;
import net.geforcemods.securitycraft.gui.GuiHandler;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.ClientUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockDoor.EnumDoorHalf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class TileEntityKeypadDoor extends TileEntitySpecialDoor implements IPasswordProtected
{
	private String passcode;

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);

		if(passcode != null && !passcode.isEmpty())
			tag.setString("passcode", passcode);

		return tag;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);

		if (tag.hasKey("passcode"))
			if(tag.getInteger("passcode") != 0)
				passcode = String.valueOf(tag.getInteger("passcode"));
			else
				passcode = tag.getString("passcode");
	}

	@Override
	public void activate(EntityPlayer player) {
		if(!world.isRemote)
		{
			IBlockState state = world.getBlockState(pos);

			if(state.getBlock() instanceof BlockKeypadDoor)
			{
				//for some reason calling BlockKeypadDoor#activate if the block is the upper half does not work, so delegate opening to the lower half
				if(state.getValue(BlockDoor.HALF) == EnumDoorHalf.UPPER)
				{
					pos = pos.down();
					state = world.getBlockState(pos);
				}

				BlockKeypadDoor.activate(world, pos, state, getSignalLength());
			}
		}
	}

	@Override
	public void openPasswordGUI(EntityPlayer player) {
		if(getPassword() != null)
			player.openGui(SecurityCraft.instance, GuiHandler.INSERT_PASSWORD_ID, world, pos.getX(), pos.getY(), pos.getZ());
		else
		{
			if(getOwner().isOwner(player))
				player.openGui(SecurityCraft.instance, GuiHandler.SETUP_PASSWORD_ID, world, pos.getX(), pos.getY(), pos.getZ());
			else
				PlayerUtils.sendMessageToPlayer(player, new TextComponentString("SecurityCraft"), ClientUtils.localize("messages.securitycraft:passwordProtected.notSetUp"), TextFormatting.DARK_RED);
		}
	}

	@Override
	public boolean onCodebreakerUsed(IBlockState blockState, EntityPlayer player, boolean isCodebreakerDisabled) {
		if(isCodebreakerDisabled)
			PlayerUtils.sendMessageToPlayer(player, ClientUtils.localize("tile.securitycraft:keypad_door.name"), ClientUtils.localize("messages.securitycraft:codebreakerDisabled"), TextFormatting.RED);
		else if(!BlockUtils.getBlockProperty(world, pos, BlockKeypad.POWERED)) {
			activate(player);
			return true;
		}

		return false;
	}

	@Override
	public String getPassword() {
		return (passcode != null && !passcode.isEmpty()) ? passcode : null;
	}

	@Override
	public void setPassword(String password) {
		TileEntity te = null;
		IBlockState state = world.getBlockState(pos);

		passcode = password;

		if(state.getValue(BlockDoor.HALF) == EnumDoorHalf.LOWER)
			te = world.getTileEntity(pos.up());
		else if(state.getValue(BlockDoor.HALF) == EnumDoorHalf.UPPER)
			te = world.getTileEntity(pos.down());

		if(te instanceof TileEntityKeypadDoor)
			((TileEntityKeypadDoor)te).setPasswordExclusively(password);
	}

	//only set the password for this door half
	public void setPasswordExclusively(String password)
	{
		passcode = password;
	}

	@Override
	public int defaultSignalLength()
	{
		return 60;
	}
}
