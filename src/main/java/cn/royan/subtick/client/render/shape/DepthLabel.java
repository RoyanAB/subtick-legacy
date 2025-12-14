package cn.royan.subtick.client.render.shape;

import cn.royan.subtick.client.render.interfaces.Text;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;

import java.util.Objects;

public class DepthLabel implements Text {
	private final String index;
	private final String depth;
	private final double x, y, z;
	private final int color1;
	private final int color2;

	public DepthLabel(String index, String depth, double x, double y, double z, int color1, int color2) {
		this.index = index;
		this.depth = depth;
		this.x = x;
		this.y = y;
		this.z = z;
		this.color1 = color1;
		this.color2 = color2;
	}

	@Override
	public void render(float tickDelta, double cx, double cy, double cz) {
		Minecraft minecraft = Minecraft.getInstance();
		TextRenderer textRenderer = minecraft.textRenderer;
		GlStateManager.pushMatrix();
		GlStateManager.translatef((float) (x - cx), (float) (y - cy), (float) (z - cz));
		GlStateManager.normal3f(0.0F, 1.0F, 0.0F);
		GlStateManager.scalef(0.07F, -0.07F, 0.08F);
		EntityRenderDispatcher entityRenderDispatcher = minecraft.getEntityRenderDispatcher();
		GlStateManager.rotatef(-entityRenderDispatcher.cameraYaw, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotatef((float) (entityRenderDispatcher.options.perspective == 2 ? 1 : -1) * entityRenderDispatcher.cameraPitch, 1.0F, 0.0F, 0.0F);
		GlStateManager.disableLighting();
		GlStateManager.enableTexture();
		GlStateManager.scalef(-1.0F, 1.0F, 1.0F);
		textRenderer.draw(index, -textRenderer.getWidth(index) / 2, 0, color1);
		GlStateManager.translatef(textRenderer.getWidth(index) / 2F, 0, 0);
		GlStateManager.scalef(0.5F, 0.5F, 0.5F);
		textRenderer.draw(depth, -textRenderer.getWidth(depth) / 2, 0, color2);
		GlStateManager.enableLighting();
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();
	}

	@Override
	public boolean equals(Object b) {
		return ((DepthLabel) b).x == x && ((DepthLabel) b).y == y && ((DepthLabel) b).z == z;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y, z);
	}
}
