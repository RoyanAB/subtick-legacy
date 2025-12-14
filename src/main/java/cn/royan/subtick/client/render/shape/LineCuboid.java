package cn.royan.subtick.client.render.shape;

import cn.royan.subtick.client.render.interfaces.Line;
import com.mojang.blaze3d.vertex.BufferBuilder;
import malilib.util.data.Color4f;

import java.util.Objects;

public class LineCuboid implements Line {
	private final double x, y, z;
	private final double X, Y, Z;
	private final Color4f color;

	public LineCuboid(double x, double y, double z, double X, double Y, double Z, Color4f color) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.X = X;
		this.Y = Y;
		this.Z = Z;
		this.color = color;
	}

	@Override
	public boolean equals(Object b) {
		return ((LineCuboid) b).x == x && ((LineCuboid) b).y == y && ((LineCuboid) b).z == z && ((LineCuboid) b).X == X && ((LineCuboid) b).Y == Y && ((LineCuboid) b).Z == Z;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y, z, X, Y, Z);
	}

	public void render(BufferBuilder buffer, double cx, double cy, double cz) {
		double x = this.x - cx, y = this.y - cy, z = this.z - cz;
		double X = this.X - cx, Y = this.Y - cy, Z = this.Z - cz;
		buffer.vertex(x, y, z).color(color.r, color.g, color.b, 0.0F).nextVertex();
		buffer.vertex(x, y, z).color(color.r, color.g, color.b, color.a).nextVertex();
		buffer.vertex(X, y, z).color(color.r, color.g, color.b, color.a).nextVertex();
		buffer.vertex(X, y, Z).color(color.r, color.g, color.b, color.a).nextVertex();
		buffer.vertex(x, y, Z).color(color.r, color.g, color.b, color.a).nextVertex();
		buffer.vertex(x, y, z).color(color.r, color.g, color.b, color.a).nextVertex();
		buffer.vertex(x, Y, z).color(color.r, color.g, color.b, color.a).nextVertex();
		buffer.vertex(X, Y, z).color(color.r, color.g, color.b, color.a).nextVertex();
		buffer.vertex(X, Y, Z).color(color.r, color.g, color.b, color.a).nextVertex();
		buffer.vertex(x, Y, Z).color(color.r, color.g, color.b, color.a).nextVertex();
		buffer.vertex(x, Y, z).color(color.r, color.g, color.b, color.a).nextVertex();
		buffer.vertex(x, Y, Z).color(color.r, color.g, color.b, 0.0F).nextVertex();
		buffer.vertex(x, y, Z).color(color.r, color.g, color.b, color.a).nextVertex();
		buffer.vertex(X, Y, Z).color(color.r, color.g, color.b, 0.0F).nextVertex();
		buffer.vertex(X, y, Z).color(color.r, color.g, color.b, color.a).nextVertex();
		buffer.vertex(X, Y, z).color(color.r, color.g, color.b, 0.0F).nextVertex();
		buffer.vertex(X, y, z).color(color.r, color.g, color.b, color.a).nextVertex();
		buffer.vertex(X, y, z).color(color.r, color.g, color.b, 0.0F).nextVertex();
	}
}
