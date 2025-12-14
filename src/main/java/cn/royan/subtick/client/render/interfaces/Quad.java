package cn.royan.subtick.client.render.interfaces;

import com.mojang.blaze3d.vertex.BufferBuilder;

public interface Quad {
	void render(BufferBuilder buffer, double cx, double cy, double cz);
}
