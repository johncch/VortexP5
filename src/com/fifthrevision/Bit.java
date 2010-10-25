package com.fifthrevision;

import java.nio.FloatBuffer;

public class Bit {

	public float x, y;
	private float vx, vy;
	/* private float radius;
	private float alpha;
	private float mass; */
	
	public int color;
	
	public int lifespan;
	
	public Bit(){
		
	};
	
	public void init(float x, float y, float vx, float vy, int color) {
		this.x = x;
		this.y = y;
		this.vx = vx;
		this.vy = vy;
		this.color = color;
		this.lifespan = 80 + (int)(Math.random() * 20);
	}
	
	public void update() {
		this.lifespan--;
		
		if(vx > 2) {
			vx *= BitsSystem.DRAG_FACTOR;
		}
		if(vy > 2) {
			vy *= BitsSystem.DRAG_FACTOR;
		}
		x += vx;
		y += vy;
		
		if(x < 0) {
			x = 0;
			vx *= -1;
		} else if(x > Vertex.WIDTH) {
			x = Vertex.WIDTH;
			vx *= -1;
		}
		
		if(y<0) {
			y = 0;
			vy *= -1;
		} else if (y > Vertex.HEIGHT) {
			y = Vertex.HEIGHT;
			vy *= -1;
		}
	}
	
	public void updateVertexArrays(int i, FloatBuffer posBuffer, FloatBuffer colBuffer) {
		int vi = i * 4;
		posBuffer.put(vi++, x - vx);
		posBuffer.put(vi++, y - vy);
		posBuffer.put(vi++, x);
		posBuffer.put(vi++, y);
		
		int ci = i * 6;
		/*int red = (0x00ff0000 & this.color) >> 16 / 100;
		int green = (0x0000ff00 & this.color) >> 8 / 100;
		int blue = 0x000000ff & this.color / 100;
		System.out.println(red + ", " + green + ", " + blue); */
		
		float color = (lifespan + 10) / 110f ;
		//System.out.println(color);
		
		colBuffer.put(ci++, color);
		colBuffer.put(ci++, color);
		colBuffer.put(ci++, color);
		colBuffer.put(ci++, color);
		colBuffer.put(ci++, color);
		colBuffer.put(ci++, color);
	}
	
}
