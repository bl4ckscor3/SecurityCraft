package net.geforcemods.securitycraft.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.api.OwnableBlockEntity;
import net.geforcemods.securitycraft.blocks.FrameBlock;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class FrameRenderer implements BlockEntityRenderer<OwnableBlockEntity> {
	private static final ResourceLocation ROLL = new ResourceLocation(SecurityCraft.MODID, "textures/block/row_roll.png");

	public FrameRenderer(BlockEntityRendererProvider.Context ctx) {}

	@Override
	public void render(OwnableBlockEntity be, float partialTicks, PoseStack pose, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		BlockState state = be.getBlockState();

		if (!state.getValue(FrameBlock.ACTIVE)) {
			Direction dir = state.getValue(FrameBlock.FACING);
			float px = 1.0F / 16.0F;
			float v0 = 1.0F * px;
			float v1 = 15.0F * px;

			pose.pushPose();

			if (dir == Direction.WEST) {
				pose.translate(v0 - 0.001F, 0, 1);
				pose.mulPose(Vector3f.YP.rotationDegrees(90.0F));
			}
			else if (dir == Direction.EAST) {
				pose.translate(1 - v0 + 0.001F, 0, 0);
				pose.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
			}
			else if (dir == Direction.SOUTH) {
				pose.translate(1, 0, 1 - v0 + 0.001F);
				pose.mulPose(Vector3f.YP.rotationDegrees(180.0F));
			}
			else if (dir == Direction.NORTH)
				pose.translate(0, 0, v0 - 0.001F);

			BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
			Matrix4f m4f = pose.last().pose();
			float frame = (be.getLevel().getGameTime() >> 1) % 26;
			float u0 = (frame - 1.0F) / 26.0F;
			float u1 = frame / 26.0F;

			RenderSystem.enableDepthTest();
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, ROLL);
			bufferBuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
			bufferBuilder.vertex(m4f, v1, v0, 0).uv(u0, 1).endVertex();
			bufferBuilder.vertex(m4f, v0, v0, 0).uv(u1, 1).endVertex();
			bufferBuilder.vertex(m4f, v0, v1, 0).uv(u1, 0).endVertex();
			bufferBuilder.vertex(m4f, v1, v1, 0).uv(u0, 0).endVertex();
			bufferBuilder.end();
			BufferUploader.end(bufferBuilder);
			pose.popPose();
		}
	}
}
