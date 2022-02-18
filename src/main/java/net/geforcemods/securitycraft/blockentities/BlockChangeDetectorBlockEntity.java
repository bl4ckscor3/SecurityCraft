package net.geforcemods.securitycraft.blockentities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.ILockable;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.IntOption;
import net.geforcemods.securitycraft.blocks.BlockChangeDetectorBlock;
import net.geforcemods.securitycraft.inventory.GenericBEMenu;
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
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BlockChangeDetectorBlockEntity extends DisguisableBlockEntity implements MenuProvider, ILockable, ITickingBlockEntity {
	private IntOption signalLength = new IntOption(this::getBlockPos, "signalLength", 60, 5, 400, 5, true); //20 seconds max
	private int range = 0;
	private DetectionMode mode = DetectionMode.BREAK;
	private boolean tracked = false;
	private List<ChangeEntry> entries = new ArrayList<>();

	public BlockChangeDetectorBlockEntity(BlockPos pos, BlockState state) {
		super(SCContent.beTypeBlockChangeDetector, pos, state);
	}

	public void log(Player player, DetectionMode action, BlockPos pos, BlockState state) {
		if (mode != DetectionMode.BOTH && action != mode)
			return;
		//TODO: reenable
		//		if (getOwner().isOwner(player) || ModuleUtils.isAllowed(this, player))
		//			return;

		entries.add(new ChangeEntry(player.getDisplayName().getString(), player.getUUID(), System.currentTimeMillis(), action, pos, state));
		level.setBlockAndUpdate(this.worldPosition, state.setValue(BlockChangeDetectorBlock.POWERED, true));
		BlockUtils.updateIndirectNeighbors(level, this.worldPosition, SCContent.BLOCK_CHANGE_DETECTOR.get());
		level.scheduleTick(this.worldPosition, SCContent.BLOCK_CHANGE_DETECTOR.get(), signalLength.get());
		setChanged();
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
		tag.putInt("range", range);
		tag.putInt("mode", mode.ordinal());
		tag.put("entries", entryList);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);

		int modeOrdinal = tag.getInt("mode");

		if (modeOrdinal < 0 || modeOrdinal >= DetectionMode.values().length)
			modeOrdinal = 0;

		range = tag.getInt("range");
		mode = DetectionMode.values()[modeOrdinal];
		entries = new ArrayList<>();
		tag.getList("entries", Tag.TAG_COMPOUND).stream().map(element -> ChangeEntry.load((CompoundTag) element)).forEach(entries::add);
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		BlockEntityTracker.BLOCK_CHANGE_DETECTOR.stopTracking(this);
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
		return new GenericBEMenu(SCContent.mTypeBlockChangeDetector, id, level, worldPosition);
	}

	@Override
	public Component getDisplayName() {
		return super.getDisplayName();
	}

	public void changeMode() {
		if (mode == DetectionMode.BREAK)
			mode = DetectionMode.PLACE;
		else
			mode = DetectionMode.BREAK;

		setChanged();
	}

	public DetectionMode getMode() {
		return mode;
	}

	public void changeRange() {
		if (++range > 15)
			range = 0;

		setChanged();
	}

	public int getRange() {
		return range;
	}

	public List<ChangeEntry> getEntries() {
		return entries;
	}

	@Override
	public ModuleType[] acceptedModules() {
		return new ModuleType[] {
				ModuleType.DISGUISE, ModuleType.ALLOWLIST, ModuleType.SMART
		};
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[] {
				signalLength
		};
	}

	public static enum DetectionMode {
		BREAK,
		PLACE,
		BOTH;
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