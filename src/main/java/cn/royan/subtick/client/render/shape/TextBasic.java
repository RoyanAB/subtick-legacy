package cn.royan.subtick.client.render.shape;

import cn.royan.subtick.client.render.interfaces.Text;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.platform.GlStateManager;
import java.util.Objects;

public class TextBasic implements Text {
	private final String text;
	private final double x, y, z;
	private final int color;

	public TextBasic(String text, double x, double y, double z, int color) {
		this.text = text;
		this.x = x;
		this.y = y;
		this.z = z;
		this.color = color;
	}

	@Override
	public void render(float tickDelta, double cx, double cy, double cz) {
		Minecraft minecraft = Minecraft.getInstance();
		TextRenderer textRenderer = minecraft.textRenderer;
		GlStateManager.pushMatrix();
		GlStateManager.translatef((float) (x - cx), (float) (y - cy) + 0.07F, (float) (z - cz));
		GlStateManager.normal3f(0.0F, 1.0F, 0.0F);
		GlStateManager.scalef(0.02F, -0.02F, 0.02F);
		EntityRenderDispatcher entityRenderDispatcher = minecraft.getEntityRenderDispatcher();
		GlStateManager.rotatef(-entityRenderDispatcher.cameraYaw, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotatef((float) (entityRenderDispatcher.options.perspective == 2 ? 1 : -1) * entityRenderDispatcher.cameraPitch, 1.0F, 0.0F, 0.0F);
		GlStateManager.disableLighting();
		GlStateManager.enableTexture();
		GlStateManager.scalef(-1.0F, 1.0F, 1.0F);
		textRenderer.draw(text, -textRenderer.getWidth(text) / 2, 0, color);
		GlStateManager.enableLighting();
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();
	}

	@Override
	public boolean equals(Object b) {
		return ((TextBasic) b).x == x && ((TextBasic) b).y == y && ((TextBasic) b).z == z;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y, z);
	}
}
