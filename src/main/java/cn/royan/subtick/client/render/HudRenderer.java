package cn.royan.subtick.client.render;

import cn.royan.subtick.client.ClientTickHandler;
import cn.royan.subtick.client.config.AlignConfig;
import cn.royan.subtick.client.config.Configs;
import cn.royan.subtick.queue.QueueElement;
import cn.royan.subtick.utils.TickPhase;
import malilib.config.option.ColorConfig;
import malilib.util.data.Color4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.DefaultVertexFormat;
import net.minecraft.client.render.vertex.Tesselator;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class HudRenderer {
	private static final Minecraft mc = Minecraft.getInstance();
	private static final TextRenderer font = mc.textRenderer;

	private static Color4f STEPPED_BG;
	private static int STEPPED_TEXT;
	private static Color4f STEPPING_BG;
	private static int STEPPING_TEXT;
	private static Color4f TO_STEP_BG;
	private static int TO_STEP_TEXT;
	private static Color4f NEW_BG;
	private static int NEW_TEXT;

	private static Pair<Integer, Integer> trimQueue(int max, int maxHighlights) {
		List<QueueElement> queue = ClientTickHandler.queue;
		if (queue.size() <= max)
			return Pair.of(0, queue.size());

		int size = ClientTickHandler.queueIndex2 - ClientTickHandler.queueIndex1 + 1;
		if (size > maxHighlights) {
			int start = Math.min(queue.size() - max, ClientTickHandler.queueIndex2 - maxHighlights);
			return Pair.of(start, start + max);
		}

		int start = Math.min(queue.size() - max, ClientTickHandler.queueIndex1);
		return Pair.of(start, start + max);
	}


	public static void render() {
		synchronized (ClientTickHandler.class) {
			if (!ClientTickHandler.frozen || !Configs.SHOW_HUD.getBooleanValue())
				return;
			GlStateManager.pushMatrix();

			STEPPED_BG = Configs.STEPPED_BG.getColor();
			STEPPED_TEXT = Configs.STEPPED_TEXT.getColor().intValue;
			STEPPING_BG = Configs.STEPPING_BG.getColor();
			STEPPING_TEXT = Configs.STEPPING_TEXT.getColor().intValue;
			TO_STEP_BG = Configs.TO_STEP_BG.getColor();
			TO_STEP_TEXT = Configs.TO_STEP_TEXT.getColor().intValue;
			NEW_BG = Configs.NEW_BG.getColor();
			NEW_TEXT = Configs.NEW_TEXT.getColor().intValue;

			TickPhase tickPhase = ClientTickHandler.tickPhase;

			AlignConfig align = Configs.HUD_ALIGNMENT.getValue();
			int xOff = Configs.HUD_OFFSET_X.getIntegerValue();
			int yOff = Configs.HUD_OFFSET_Y.getIntegerValue();

			int wPhase = 0;
			int wDim = 0;
			for (int phase = 0; phase < TickPhase.totalPhases; phase++)
				wPhase = Math.max(wPhase, font.getWidth(TickPhase.getPhaseName(phase)));
			for (String dim : ClientTickHandler.dimensions)
				wDim = Math.max(wDim, font.getWidth(dim));
			wPhase += 2;
			wDim += 2;
			int wDimPhase = wDim + wPhase + 10;

			int h = font.fontHeight + 1;

			if (ClientTickHandler.queue.isEmpty())
				renderHudA(tickPhase, align.getX(wDimPhase + 10) + xOff, align.getY(h * TickPhase.totalPhases) + yOff, wDim, wPhase, h);
			else {
				Text[] queue = new Text[Math.min(ClientTickHandler.queue.size(), Configs.MAX_QUEUE_SIZE.getIntegerValue())];
				Pair<Integer, Integer> indices = trimQueue(Configs.MAX_QUEUE_SIZE.getIntegerValue(), Configs.MAX_HIGHLIGHT_SIZE.getIntegerValue());
				int wQueue = 0;
				int i = indices.getLeft();
				int j = 0;
				boolean depth = tickPhase.phase == TickPhase.TILE_TICK || tickPhase.phase == TickPhase.BLOCK_EVENT;
				while (i < indices.getRight()) {
					QueueElement element = ClientTickHandler.queue.get(i++);
					Text s = queue[j++] = text(element, i, depth);
					wQueue = Math.max(wQueue, font.getWidth(s.getString()));
				}
				wQueue += 2;
				int wAll = wDimPhase + wQueue + 10;
				renderHudB(queue, tickPhase, ClientTickHandler.queueIndex1 - indices.getLeft(), ClientTickHandler.queueIndex2 - indices.getLeft(), queue.length - ClientTickHandler.newQueueElementCount, align.getX(wAll + 10) + xOff, align.getY(h * Math.max(TickPhase.totalPhases, indices.getRight() - indices.getLeft())) + yOff, wDim, wPhase, wQueue, h);
			}
			GlStateManager.popMatrix();
		}
	}

	private static String color(ColorConfig color) {
		return "#" + color.getStringValue().substring(3);
	}

	private static Text text(QueueElement element, int i, boolean depth) {
		return depth ?
			Text.Serializer.fromJsonLenient(String.format("[\"#%d (\", {\"color\":\"%s\",\"text\":\"%d\"}, \"): %s\"]", i, color(i <= ClientTickHandler.queueIndex1 ? Configs.STEPPED_DEPTH : i <= ClientTickHandler.queueIndex2 ? Configs.STEPPING_DEPTH : i >= ClientTickHandler.queue.size() - ClientTickHandler.newQueueElementCount ? Configs.NEW_DEPTH : Configs.TO_STEP_DEPTH), element.depth, element.label)) :
			new LiteralText(String.format("#%d: %s", i, element.label));
	}

	public static void renderHudA(TickPhase phase, int x, int y, int wDim, int wPhase, int h) {
		GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableTexture();
		GlStateManager.enableBlend();

		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);

		drawTableB(bufferBuilder, x, y, h, wDim, ClientTickHandler.dimensions.size(), phase.dim);
		drawTableA(bufferBuilder, x + wDim + 10, y, h, wPhase, TickPhase.totalPhases, phase.phase);


		tessellator.end();

		x += 2;
		GlStateManager.enableTexture();
		for (int y1 = y + 2, i = 0; i < ClientTickHandler.dimensions.size(); i++, y1 += h)
			font.draw(ClientTickHandler.dimensions.get(i), x, y1, i < phase.dim ? STEPPED_TEXT : TO_STEP_TEXT);

		x += wDim + 10;
		for (int y1 = y + 2, i = 0; i < TickPhase.totalPhases; i++, y1 += h)
			font.draw(TickPhase.getPhaseName(i), x, y1, i < phase.phase ? STEPPED_TEXT : TO_STEP_TEXT);
	}

	public static void renderHudB(Text[] queue, TickPhase phase, int iqueue1, int iqueue2, int iqueue3, int x, int y, int wDim, int wPhase, int wQueue, int h) {
		GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableTexture();
		GlStateManager.enableBlend();

		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);

		drawTableB(bufferBuilder, x, y, h, wDim, ClientTickHandler.dimensions.size(), phase.dim);
		drawTableB(bufferBuilder, x + wDim + 10, y, h, wPhase, TickPhase.totalPhases, phase.phase);
		drawTableC(bufferBuilder, x + wDim + wPhase + 20, y, h, wQueue, queue.length, iqueue1, iqueue2, iqueue3);

		int sx = x + wDim + 10 + wPhase + 1;
		int sy = y + phase.phase * h + 1;
		drawQuad(bufferBuilder,
			sx, sy, sx, sy + h - 1,
			sx + 9, y + queue.length * h, sx + 9, y, Configs.POSITION.getColor());

		tessellator.end();

		x += 2;
		GlStateManager.enableTexture();
		for (int y1 = y + 2, i = 0; i < ClientTickHandler.dimensions.size(); i++, y1 += h)
			font.draw(ClientTickHandler.dimensions.get(i), x, y1, i < phase.dim ? STEPPED_TEXT : TO_STEP_TEXT);

		x += wDim + 10;
		for (int y1 = y + 2, i = 0; i < TickPhase.totalPhases; i++, y1 += h)
			font.draw(TickPhase.getPhaseName(i), x, y1, i < phase.phase ? STEPPED_TEXT : TO_STEP_TEXT);

		x += wPhase + 10;
		for (int y1 = y + 2, i = 0; i < queue.length; i++, y1 += h)
			font.draw(queue[i].getFormattedString(), x, y1, i < iqueue1 ? STEPPED_TEXT : i < iqueue2 ? STEPPING_TEXT : i >= iqueue3 ? NEW_TEXT : TO_STEP_TEXT);
	}

	private static void drawTableA(BufferBuilder buffer, int x, int y, int h, int w, int count, int index) {
		// Left & right borders
		int Y = y + count * h + 1;
		int X = x + w;
		drawRect(buffer, x, y, x + 1, Y, Configs.SEPARATOR.getColor());
		drawRect(buffer, X, y, X + 1, Y, Configs.SEPARATOR.getColor());
		x += 1;
		w -= 1;
		h -= 1;

		// Fill
		for (int i = 0; i < count; i++) {
			if (i == index) {
				drawQuad(buffer, X, y, X, y + 1, X + 5, y + 3, X + 5, y - 3, Configs.POSITION.getColor());
				drawRect(buffer, x, y, X, y += 1, Configs.POSITION.getColor());
			} else
				drawRect(buffer, x, y, X, y += 1, Configs.SEPARATOR.getColor());
			drawRect(buffer, x, y, X, y += h, i < index ? STEPPED_BG : TO_STEP_BG);
		}
		drawRect(buffer, x, y, X, y + 1, Configs.SEPARATOR.getColor());
	}

	private static void drawTableB(BufferBuilder buffer, int x, int y, int h, int w, int count, int index) {
		// Left & right borders
		int Y = y + count * h + 1;
		int X = x + w;
		drawRect(buffer, x, y, x + 1, Y, Configs.SEPARATOR.getColor());
		drawRect(buffer, X, y, X + 1, Y, Configs.SEPARATOR.getColor());
		x += 1;
		w -= 1;
		h -= 1;

		// Middle
		for (int i = 0; i < count; i++) {
			drawRect(buffer, x, y, X, y += 1, Configs.SEPARATOR.getColor()); // Border
			drawRect(buffer, x, y, X, y += h, i < index ? STEPPED_BG : i == index ? Configs.POSITION.getColor() : TO_STEP_BG); // Fill
		}
		drawRect(buffer, x, y, X, y + 1, Configs.SEPARATOR.getColor());
	}

	private static void drawTableC(BufferBuilder buffer, int x, int y, int h, int w, int count, int index1, int index2, int index3) {
		// Left & right borders
		int Y = y + count * h + 1;
		int X = x + w;
		drawRect(buffer, x, y, x + 1, Y, Configs.SEPARATOR.getColor());
		drawRect(buffer, X, y, X + 1, Y, Configs.SEPARATOR.getColor());
		x += 1;
		w -= 1;
		h -= 1;

		// Middle
		for (int i = 0; i < count; i++) {
			if (i == index2) {
				drawQuad(buffer, X, y, X, y + 1, X + 5, y + 3, X + 5, y - 3, Configs.POSITION.getColor());
				drawRect(buffer, x, y, X, y += 1, Configs.POSITION.getColor());
				drawRect(buffer, x, y, X, y += h, TO_STEP_BG);
			} else {
				drawRect(buffer, x, y, X, y += 1, Configs.SEPARATOR.getColor());
				drawRect(buffer, x, y, X, y += h, i < index1 ? STEPPED_BG : i < index2 ? STEPPING_BG : i >= index3 ? NEW_BG : TO_STEP_BG);
			}
		}
		drawRect(buffer, x, y, X, y + 1, Configs.SEPARATOR.getColor());
	}

	private static void drawRect(BufferBuilder buffer, int x, int y, int X, int Y, Color4f color) {
		buffer.vertex(x, y, 0D).color(color.r, color.g, color.b, color.a).nextVertex();
		buffer.vertex(x, Y, 0D).color(color.r, color.g, color.b, color.a).nextVertex();
		buffer.vertex(X, Y, 0D).color(color.r, color.g, color.b, color.a).nextVertex();
		buffer.vertex(X, y, 0D).color(color.r, color.g, color.b, color.a).nextVertex();
	}

	private static void drawQuad(BufferBuilder buffer, int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4, Color4f color) {
		buffer.vertex(x1, y1, 0D).color(color.r, color.g, color.b, color.a).nextVertex();
		buffer.vertex(x2, y2, 0D).color(color.r, color.g, color.b, color.a).nextVertex();
		buffer.vertex(x3, y3, 0D).color(color.r, color.g, color.b, color.a).nextVertex();
		buffer.vertex(x4, y4, 0D).color(color.r, color.g, color.b, color.a).nextVertex();
	}
}
