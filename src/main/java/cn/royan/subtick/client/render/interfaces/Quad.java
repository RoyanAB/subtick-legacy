package cn.royan.subtick.client.render.interfaces;

import net.minecraft.client.render.vertex.BufferBuilder;

public interface Quad {
	void render(BufferBuilder buffer, double cx, double cy, double cz);
}
