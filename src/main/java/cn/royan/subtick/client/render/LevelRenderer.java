package cn.royan.subtick.client.render;

import cn.royan.subtick.client.render.interfaces.Line;
import cn.royan.subtick.client.render.interfaces.Quad;
import cn.royan.subtick.client.render.interfaces.Text;
import cn.royan.subtick.client.render.shape.DepthLabel;
import cn.royan.subtick.client.render.shape.LineCuboid;
import cn.royan.subtick.client.render.shape.QuadCuboid;
import cn.royan.subtick.client.render.shape.TextBasic;
import cn.royan.subtick.utils.Color4f;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tessellator;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.living.player.PlayerEntity;

import java.util.HashSet;

public class LevelRenderer {
	private static final HashSet<Line> lines = new HashSet<>();
	private static final HashSet<Quad> quads = new HashSet<>();
	private static final HashSet<Text> texts = new HashSet<>();

	public static synchronized void render(float tickDelta) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null && mc.getEntityRenderDispatcher() != null && mc.getEntityRenderDispatcher().options != null) {
			GlStateManager.pushMatrix();
			GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.disableTexture();
			GlStateManager.enableBlend();
			GlStateManager.disableDepthTest();
			GlStateManager.lineWidth(1.0F);

			PlayerEntity playerEntity = mc.player;
			double d = playerEntity.prevTickX + (playerEntity.x - playerEntity.prevTickX) * (double) tickDelta;
			double e = playerEntity.prevTickY + (playerEntity.y - playerEntity.prevTickY) * (double) tickDelta;
			double f = playerEntity.prevTickZ + (playerEntity.z - playerEntity.prevTickZ) * (double) tickDelta;

			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferBuilder = tessellator.getBuilder();
			bufferBuilder.begin(3, DefaultVertexFormat.POSITION_COLOR);
			for (Line line : lines)
				line.render(bufferBuilder, d, e, f);
			tessellator.end();

			bufferBuilder.begin(5, DefaultVertexFormat.POSITION_COLOR);
			for (Quad quad : quads)
				quad.render(bufferBuilder, d, e, f);
			tessellator.end();


			for (Text text : texts)
				text.render(tickDelta, d, e, f);

			GlStateManager.enableTexture();
			GlStateManager.disableBlend();
			GlStateManager.enableDepthTest();
			GlStateManager.popMatrix();
		}
	}

	public static synchronized void clear() {
		lines.clear();
		quads.clear();
		texts.clear();
	}

	public static void addCuboid(int x, int y, int z, Color4f color) {
		addCuboidFaces(x, y, z, x + 1, y + 1, z + 1, color);
		addCuboidEdges(x, y, z, x + 1, y + 1, z + 1, color);
	}

	public static void addCuboidFaces(int x, int y, int z, Color4f color) {
		addCuboidFaces(x, y, z, x + 1, y + 1, z + 1, color);
	}

	public static void addCuboidEdges(int x, int y, int z, Color4f color) {
		addCuboidEdges(x, y, z, x + 1, y + 1, z + 1, color);
	}

	public static synchronized void addCuboidFaces(double x, double y, double z, double X, double Y, double Z, Color4f color) {
		QuadCuboid o = new QuadCuboid(x, y, z, X, Y, Z, color);
		if (!quads.add(o)) {
			quads.remove(o);
			quads.add(o);
		}
	}

	public static synchronized void addCuboidEdges(double x, double y, double z, double X, double Y, double Z, Color4f color) {
		LineCuboid o = new LineCuboid(x, y, z, X, Y, Z, color);
		if (!lines.add(o)) {
			lines.remove(o);
			lines.add(o);
		}
	}

	public static synchronized void addText(String text, int x, int y, int z, Color4f color) {
		TextBasic o = new TextBasic(text, x + 0.5, y + 0.5, z + 0.5, color.intValue);
		if (!texts.add(o)) {
			texts.remove(o);
			texts.add(o);
		}
	}

	public static synchronized void addLabel(int index, int depth, int x, int y, int z, Color4f color1, Color4f color2) {
		DepthLabel o = new DepthLabel(String.valueOf(index), String.valueOf(depth), x + 0.5, y + 0.5, z + 0.5, color1.intValue, color2.intValue);
		if (!texts.add(o)) {
			texts.remove(o);
			texts.add(o);
		}
	}
}
