package net.geforcemods.securitycraft.items;

import java.util.ArrayList;
import java.util.List;

import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.misc.EnumModuleType;
import net.geforcemods.securitycraft.util.ClientUtils;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemModule extends Item{

	public static final int MAX_PLAYERS = 50;
	private final EnumModuleType module;
	private final boolean nbtCanBeModified;
	private boolean canBeCustomized;
	private int guiToOpen;
	private int numberOfItemAddons;
	private int numberOfBlockAddons;

	public ItemModule(EnumModuleType module, boolean nbtCanBeModified){
		this(module, nbtCanBeModified, false, -1, 0, 0);
	}

	public ItemModule(EnumModuleType module, boolean nbtCanBeModified, boolean canBeCustomized, int guiToOpen){
		this(module, nbtCanBeModified, canBeCustomized, guiToOpen, 0, 0);
	}

	public ItemModule(EnumModuleType module, boolean nbtCanBeModified, boolean canBeCustomized, int guiToOpen, int itemAddons, int blockAddons){
		this.module = module;
		this.nbtCanBeModified = nbtCanBeModified;
		this.canBeCustomized = canBeCustomized;
		this.guiToOpen = guiToOpen;
		numberOfItemAddons = itemAddons;
		numberOfBlockAddons = blockAddons;

		setMaxStackSize(1);
		setCreativeTab(SecurityCraft.tabSCTechnical);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		TileEntity te = world.getTileEntity(pos);

		if(te instanceof IModuleInventory)
		{
			IModuleInventory inv = (IModuleInventory)te;
			ItemStack stack = player.getHeldItem(hand);
			EnumModuleType type = ((ItemModule)stack.getItem()).getModuleType();

			if(inv.getAcceptedModules().contains(type) && !inv.hasModule(type))
			{
				inv.insertModule(stack);
				inv.onModuleInserted(stack, type);

				if(!player.isCreative())
					stack.shrink(1);

				return EnumActionResult.SUCCESS;
			}
		}

		return EnumActionResult.PASS;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);

		if(!player.isSneaking())
		{
			try
			{
				if(!world.isRemote) {
					if(!stack.hasTagCompound())
						stack.setTagCompound(new NBTTagCompound());

					if(canBeCustomized())
						player.openGui(SecurityCraft.instance, guiToOpen, world, (int) player.posX, (int) player.posY, (int) player.posZ);
				}
			}
			catch(NoSuchMethodError e) {/*:^)*/}
		}

		return ActionResult.newResult(EnumActionResult.PASS, stack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		if(nbtCanBeModified || canBeCustomized())
			list.add(ClientUtils.localize("tooltip.securitycraft:module.modifiable"));
		else
			list.add(ClientUtils.localize("tooltip.securitycraft:module.notModifiable"));

		if(nbtCanBeModified && stack.getTagCompound() != null && !stack.getTagCompound().isEmpty()) {
			list.add(" ");
			list.add(ClientUtils.localize("tooltip.securitycraft:module.playerCustomization.players") + ":");

			for(int i = 1; i <= MAX_PLAYERS; i++)
				if(!stack.getTagCompound().getString("Player" + i).isEmpty())
					list.add(stack.getTagCompound().getString("Player" + i));
		}

		if(canBeCustomized()) {
			if(numberOfItemAddons > 0 && numberOfBlockAddons > 0)
				list.add(ClientUtils.localize("tooltip.securitycraft:module.itemAddons.usage.blocksAndItems").replace("#blocks", numberOfBlockAddons + "").replace("#items", numberOfItemAddons + ""));

			if(numberOfItemAddons > 0 && numberOfBlockAddons == 0)
				list.add(ClientUtils.localize("tooltip.securitycraft:module.itemAddons.usage.items").replace("#", numberOfItemAddons + ""));

			if(numberOfItemAddons == 0 && numberOfBlockAddons > 0)
				list.add(ClientUtils.localize("tooltip.securitycraft:module.itemAddons.usage.blocks").replace("#", numberOfBlockAddons + ""));

			if(getNumberOfAddons() > 0 && !getAddons(stack.getTagCompound()).isEmpty()) {
				list.add(" ");

				list.add(ClientUtils.localize("tooltip.securitycraft:module.itemAddons.added") + ":");

				for(ItemStack addon : getAddons(stack.getTagCompound()))
					list.add("- " + ClientUtils.localize(addon.getTranslationKey() + ".name"));
			}
		}
	}

	public EnumModuleType getModuleType() {
		return module;
	}

	public int getNumberOfAddons(){
		return numberOfItemAddons + numberOfBlockAddons;
	}

	public int getNumberOfItemAddons(){
		return numberOfItemAddons;
	}

	public int getNumberOfBlockAddons(){
		return numberOfBlockAddons;
	}

	public ArrayList<Block> getBlockAddons(NBTTagCompound tag){
		ArrayList<Block> list = new ArrayList<>();

		if(tag == null) return list;

		NBTTagList items = tag.getTagList("ItemInventory", Constants.NBT.TAG_COMPOUND);

		for(int i = 0; i < items.tagCount(); i++) {
			NBTTagCompound item = items.getCompoundTagAt(i);
			int slot = item.getInteger("Slot");

			if(slot < numberOfBlockAddons) {
				ItemStack stack;

				if((stack = new ItemStack(item)).getTranslationKey().startsWith("tile."))
					list.add(Block.getBlockFromItem(stack.getItem()));
			}
		}

		return list;
	}

	public ArrayList<ItemStack> getAddons(NBTTagCompound tag){
		ArrayList<ItemStack> list = new ArrayList<>();

		if(tag == null) return list;

		NBTTagList items = tag.getTagList("ItemInventory", Constants.NBT.TAG_COMPOUND);

		for(int i = 0; i < items.tagCount(); i++) {
			NBTTagCompound item = items.getCompoundTagAt(i);
			int slot = item.getInteger("Slot");

			if(slot < numberOfBlockAddons)
				list.add(new ItemStack(item));
		}

		return list;
	}

	public boolean canBeCustomized(){
		return canBeCustomized;
	}

}