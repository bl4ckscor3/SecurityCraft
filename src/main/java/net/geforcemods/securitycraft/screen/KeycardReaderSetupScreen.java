package net.geforcemods.securitycraft.screen;

import com.mojang.blaze3d.platform.GlStateManager;

import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.containers.GenericTEContainer;
import net.geforcemods.securitycraft.network.server.SetKeycardLevel;
import net.geforcemods.securitycraft.screen.components.IdButton;
import net.geforcemods.securitycraft.tileentity.KeycardReaderTileEntity;
import net.geforcemods.securitycraft.util.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class KeycardReaderSetupScreen extends ContainerScreen<GenericTEContainer>{

	private static final ResourceLocation TEXTURE = new ResourceLocation("securitycraft:textures/gui/container/blank.png");
	private KeycardReaderTileEntity te;
	private IdButton lvlOfSecurityButton;
	private IdButton requiresExactCardButton;
	private boolean requiresExactCard = false;
	private int lvlOfSecurity = 0;

	public KeycardReaderSetupScreen(GenericTEContainer container, PlayerInventory inv, ITextComponent name) {
		super(container, inv, name);
		te = (KeycardReaderTileEntity)container.te;
	}

	@Override
	public void init(){
		super.init();

		addButton(lvlOfSecurityButton = new IdButton(0, width / 2 - (48 * 2 - 23), height / 2 + 20, 150, 20, "", button -> updateButtonText()));
		addButton(requiresExactCardButton = new IdButton(1, width / 2 - (48 * 2 - 11), height / 2 - 28, 125, 20, requiresExactCard ? ClientUtils.localize("gui.securitycraft:keycardSetup.equal").getFormattedText() : ClientUtils.localize("gui.securitycraft:keycardSetup.equalOrHigher").getFormattedText(), this::actionPerformed));
		addButton(new IdButton(2, width / 2 - 48, height / 2 + 30 + 20, 100, 20, ClientUtils.localize("gui.securitycraft:keycardSetup.save").getFormattedText(), button -> saveLvls()));

		updateButtonText();
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the items)
	 */
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		font.drawString(ClientUtils.localize("gui.securitycraft:keycardSetup.explanation.1").getFormattedText(), xSize / 2 - font.getStringWidth(ClientUtils.localize("gui.securitycraft:keycardSetup.explanation.1").getFormattedText()) / 2, 6, 4210752);
		font.drawString(ClientUtils.localize("gui.securitycraft:keycardSetup.explanation.2").getFormattedText(), xSize / 2 - font.getStringWidth(ClientUtils.localize("gui.securitycraft:keycardSetup.explanation.2").getFormattedText()) / 2 - 2, 30 - 10, 4210752);
		font.drawString(ClientUtils.localize("gui.securitycraft:keycardSetup.explanation.3").getFormattedText(), xSize / 2 - font.getStringWidth(ClientUtils.localize("gui.securitycraft:keycardSetup.explanation.3").getFormattedText()) / 2 - 11, 42 - 10, 4210752);
		font.drawString(ClientUtils.localize("gui.securitycraft:keycardSetup.explanation.4").getFormattedText(), xSize / 2 - font.getStringWidth(ClientUtils.localize("gui.securitycraft:keycardSetup.explanation.4").getFormattedText()) / 2 - 10, 54 - 10, 4210752);
		font.drawString(ClientUtils.localize("gui.securitycraft:keycardSetup.explanation.5").getFormattedText(), xSize / 2 + 45, 66 - 5, 4210752);
		font.drawString(ClientUtils.localize("gui.securitycraft:keycardSetup.explanation.6").getFormattedText(), xSize / 2 - font.getStringWidth(ClientUtils.localize("gui.securitycraft:keycardSetup.explanation.6").getFormattedText()) / 2 - 6, 78 - 1, 4210752);
		font.drawString(ClientUtils.localize("gui.securitycraft:keycardSetup.explanation.7").getFormattedText(), xSize / 2 - font.getStringWidth(ClientUtils.localize("gui.securitycraft:keycardSetup.explanation.7").getFormattedText()) / 2 - 20, 90 - 1, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		renderBackground();
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		minecraft.getTextureManager().bindTexture(TEXTURE);
		int startX = (width - xSize) / 2;
		int startY = (height - ySize) / 2;
		this.blit(startX, startY, 0, 0, xSize, ySize);
	}

	private void updateButtonText(){
		if(++lvlOfSecurity > 5)
			lvlOfSecurity = 1;

		lvlOfSecurityButton.setMessage(ClientUtils.localize("gui.securitycraft:keycardSetup.lvlNeeded").getFormattedText() + " " + lvlOfSecurity);
	}

	protected void actionPerformed(IdButton button){
		requiresExactCard = !requiresExactCard;
		requiresExactCardButton.setMessage(requiresExactCard ? ClientUtils.localize("gui.securitycraft:keycardSetup.equal").getFormattedText() : ClientUtils.localize("gui.securitycraft:keycardSetup.equalOrHigher").getFormattedText());
	}

	private void saveLvls() {
		te.setPassword(String.valueOf(lvlOfSecurity));
		te.setRequiresExactKeycard(requiresExactCard);
		SecurityCraft.channel.sendToServer(new SetKeycardLevel(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ(), lvlOfSecurity, requiresExactCard));
		Minecraft.getInstance().player.closeScreen();
	}
}
