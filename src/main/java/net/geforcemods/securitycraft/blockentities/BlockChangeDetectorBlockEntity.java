package net.geforcemods.securitycraft.blockentities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.ILockable;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.IntOption;
import net.geforcemods.securitycraft.blocks.BlockChangeDetectorBlock;
import net.geforcemods.securitycraft.inventory.BlockChangeDetectorMenu;
import net.geforcemods.securitycraft.misc.BlockEntityTracker;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.ITickingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BlockChangeDetectorBlockEntity extends DisguisableBlockEntity implements Container, MenuProvider, ILockable, ITickingBlockEntity {
	private IntOption signalLength = new IntOption(this::getBlockPos, "signalLength", 60, 5, 400, 5, true); //20 seconds max
	private IntOption range = new IntOption(this::getBlockPos, "range", 15, 1, 100, 1, true);
	private DetectionMode mode = DetectionMode.BOTH;
	private boolean tracked = false;
	private List<ChangeEntry> entries = new ArrayList<>();
	private ItemStack filter = ItemStack.EMPTY;

	public BlockChangeDetectorBlockEntity(BlockPos pos, BlockState state) {
		super(SCContent.beTypeBlockChangeDetector, pos, state);
	}

	public void log(Player player, DetectionMode action, BlockPos pos, BlockState state) {
		if (mode != DetectionMode.BOTH && action != mode)
			return;

		//TODO: reenable
		//		if (getOwner().isOwner(player) || ModuleUtils.isAllowed(this, player))
		//			return;

		if (hasModule(ModuleType.SMART) && (filter.getItem() instanceof BlockItem item && item.getBlock() != state.getBlock()))
			return;

		if (hasModule(ModuleType.REDSTONE)) {
			level.setBlockAndUpdate(worldPosition, getBlockState().setValue(BlockChangeDetectorBlock.POWERED, true));
			BlockUtils.updateIndirectNeighbors(level, worldPosition, SCContent.BLOCK_CHANGE_DETECTOR.get());
			level.scheduleTick(worldPosition, SCContent.BLOCK_CHANGE_DETECTOR.get(), signalLength.get());
		}

		entries.add(new ChangeEntry(player.getDisplayName().getString(), player.getUUID(), System.currentTimeMillis(), action, pos, state));
		setChanged();
		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
	}

	@Override
	public void tick(Level level, BlockPos pos, BlockState state) {
		if (!tracked) {
			BlockEntityTracker.BLOCK_CHANGE_DETECTOR.track(this);
			tracked = true;
		}
	}

	@Override
	public void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);

		ListTag entryList = new ListTag();

		entries.stream().map(ChangeEntry::save).forEach(entryList::add);
		tag.putInt("mode", mode.ordinal());
		tag.put("entries", entryList);
		tag.put("filter", filter.save(new CompoundTag()));
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);

		int modeOrdinal = tag.getInt("mode");

		if (modeOrdinal < 0 || modeOrdinal >= DetectionMode.values().length)
			modeOrdinal = 0;

		mode = DetectionMode.values()[modeOrdinal];
		entries = new ArrayList<>();
		tag.getList("entries", Tag.TAG_COMPOUND).stream().map(element -> ChangeEntry.load((CompoundTag) element)).forEach(entries::add);
		filter = ItemStack.of(tag.getCompound("filter"));
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		BlockEntityTracker.BLOCK_CHANGE_DETECTOR.stopTracking(this);
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
		return new BlockChangeDetectorMenu(id, level, worldPosition, inventory);
	}

	@Override
	public Component getDisplayName() {
		return super.getDisplayName();
	}

	public void setMode(DetectionMode mode) {
		this.mode = mode;
		setChanged();
	}

	public DetectionMode getMode() {
		return mode;
	}

	public int getRange() {
		return range.get();
	}

	public List<ChangeEntry> getEntries() {
		return entries;
	}

	@Override
	public ModuleType[] acceptedModules() {
		return new ModuleType[] {
				ModuleType.DISGUISE, ModuleType.ALLOWLIST, ModuleType.SMART, ModuleType.REDSTONE
		};
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[] {
				signalLength, range
		};
	}

	@Override
	public void clearContent() {
		filter = ItemStack.EMPTY;
		setChanged();
	}

	@Override
	public int getContainerSize() {
		return 1;
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return filter.isEmpty();
	}

	@Override
	public ItemStack getItem(int index) {
		return getStackInSlot(index);
	}

	@Override
	public ItemStack removeItem(int index, int count) {
		ItemStack stack = filter;

		if (count >= 1) {
			filter = ItemStack.EMPTY;
			setChanged();
			return stack;
		}

		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
		ItemStack stack = filter;

		filter = ItemStack.EMPTY;
		setChanged();
		return stack;
	}

	@Override
	public void setItem(int index, ItemStack stack) {
		if (stack.getItem() instanceof BlockItem) {
			if (!stack.isEmpty() && stack.getCount() > getMaxStackSize())
				stack = new ItemStack(stack.getItem(), getMaxStackSize());

			filter = stack;
			setChanged();
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public boolean enableHack() {
		return true;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return slot >= 100 ? getModuleInSlot(slot) : (slot == 36 ? filter : ItemStack.EMPTY);
	}

	public static enum DetectionMode {
		BREAK("gui.securitycraft:block_change_detector.mode.break"),
		PLACE("gui.securitycraft:block_change_detector.mode.place"),
		BOTH("gui.securitycraft:block_change_detector.mode.both");

		private String descriptionId;

		private DetectionMode(String desciptionId) {
			this.descriptionId = desciptionId;
		}

		public String getDescriptionId() {
			return descriptionId;
		}
	}

	public static record ChangeEntry(String player, UUID uuid, long timestamp, DetectionMode action, BlockPos pos, BlockState state) {
		public CompoundTag save() {
			CompoundTag tag = new CompoundTag();

			tag.putString("player", player);
			tag.putUUID("uuid", uuid);
			tag.putLong("timestamp", timestamp);
			tag.putInt("action", action.ordinal());
			tag.putLong("pos", pos.asLong());
			tag.put("state", NbtUtils.writeBlockState(state));
			return tag;
		}

		public static ChangeEntry load(CompoundTag tag) {
			int actionOrdinal = tag.getInt("action");

			if (actionOrdinal < 0 || actionOrdinal >= DetectionMode.values().length)
				actionOrdinal = 0;

			//@formatter:off
			return new ChangeEntry(
					tag.getString("player"),
					tag.getUUID("uuid"),
					tag.getLong("timestamp"),
					DetectionMode.values()[actionOrdinal],
					BlockPos.of(tag.getLong("pos")),
					NbtUtils.readBlockState(tag.getCompound("state")));
			//@formatter:on
		}
	}
}
