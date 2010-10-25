package com.fifthrevision;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import processing.core.PImage;

import com.sun.opengl.util.BufferUtil;

public class BitsSystem {
	
	final static int MAX_PARTICLES = 5000;
	
	public final static float DRAG_FACTOR = 0.95f;
	public final static int DRIP_SIZE = 3;
	public final static int DRIP_BOUND = DRIP_SIZE * 2 + 1;
	public final static float DRIP_STEP = (float)1/DRIP_BOUND;
	
	// private Bits[] bits;
	private ArrayList<Bit> activeBits;
	private ArrayList<Bit> dormantBits;
	public FloatBuffer posArray;
	public FloatBuffer colArray;
		
	public BitsSystem() {
		// System.out.println(DRIP_SIZE + ", " + DRIP_BOUND + ", " + DRIP_STEP);
		
		activeBits = new ArrayList<Bit>();
		dormantBits = new ArrayList<Bit>();
		
		for(int i = 0; i < MAX_PARTICLES; i++) {
			dormantBits.add(new Bit());
		}
		
		posArray = BufferUtil.newFloatBuffer(MAX_PARTICLES * 2 * 2);
		colArray = BufferUtil.newFloatBuffer(MAX_PARTICLES * 3 * 2);
	}

	public void addBits(int num, int hueBottom, int hueTop, float x, float y, float vx, float vy) {
		for(int i = 0; i < num; i++) {
			if(activeBits.size() >= MAX_PARTICLES) {
				// System.out.println("reached?");
				return;
			} 
			int offset = Vertex.RADIUS - Vertex.RADIUS / 2;
			int color = ColorUtil.HSVtoRGB(hueBottom + (hueTop - hueBottom) / num * i, 200, 200, 255);
			
			Bit bit = dormantBits.remove(0);
			bit.init(x + (float)(Math.random() * offset),y + (float)(Math.random() * offset), vx + rFactor(), vy + rFactor(), color);
			activeBits.add(bit);
		}
	}
	
	private float rFactor() {
		return (float) Math.random() * 8 - 4;
	}
	
	public void update() {
		int i = 0;
		for(i = 0; i < MAX_PARTICLES * 2 * 2; i++) {
			posArray.put(i, 0);
		}
		for(i = 0; i < MAX_PARTICLES * 3 * 2; i++) {
			colArray.put(i, 0);
		}
		
		for(i = 0; i < activeBits.size(); i++) {
			Bit bit = activeBits.get(i);
			bit.update();
			bit.updateVertexArrays(i, posArray, colArray);
		}
		
	}
	
	public void paint(PImage image) {
		// prune
		int i;
		for(i = activeBits.size() - 1; i >= 0; i--) {
			Bit bit = activeBits.get(i);
			if(bit.lifespan == 0) {
				activeBits.remove(i);
				dormantBits.add(bit);
				int normX = (int) bit.x;
				int normY = (int) bit.y;
				/* if(normX > 0 && normX <= Vertex.WIDTH && normY > 0 && normY < Vertex.HEIGHT) {
					image.pixels[normX + normY * Vertex.WIDTH] = bit.color;
					image.pixels[normX + normY * Vertex.WIDTH - 1] = bit.color;
					image.pixels[normX + (normY - 1) * Vertex.WIDTH] = bit.color;
					image.pixels[normX + (normY - 1) * Vertex.WIDTH - 1] = bit.color;
				}*/
				int index = normX + normY * Vertex.WIDTH;
				if(index >= 0 && index < image.pixels.length) {
					image.pixels[index] = bit.color;
					int j, k;
					for(j = 1; j <= DRIP_SIZE; j++){
						for(k = 0; k <= DRIP_SIZE; k++) {
							int colFactor = (int)((float) (DRIP_BOUND - (j + k)) / DRIP_BOUND * 255 + (Math.random() * DRIP_STEP * 255));							
							int color = (255 << 24 | colFactor << 16 | colFactor << 8 | colFactor) & bit.color;
							//System.out.println(colFactor);
							
							int a = normX + ((normY + k) * Vertex.WIDTH) + j;
							if(a >= 0 && a < image.pixels.length) {
								//image.pixels[a] = color; 
								image.pixels[a] = ColorUtil.blendColor(color, image.pixels[a]); 

									//(((j + k) / 5) * 0xffffffff) & bit.color;
								// System.out.println((((j + k) / 5) * 255 |(((j + k) / 5) * 255) >> 8 | (((j + k) / 5) * 255) >> 16));
									// (((j + k) / 5 * 0xff) << 24) | (0x00ffffff & bit.color);
							}
							
							colFactor = (int)((float) (DRIP_BOUND - (j + k)) / DRIP_BOUND * 255 + (Math.random() * DRIP_STEP * 255));							
							color = (255 << 24 | colFactor << 16 | colFactor << 8 | colFactor) & bit.color;
							
							int b = normX + ((normY - k) * Vertex.WIDTH) - j;
							if(b >= 0 && b < image.pixels.length) {
								//image.pixels[b] = color; 
								image.pixels[b] = ColorUtil.blendColor(color, image.pixels[b]); 

									// (((j + k) / 5) * 0xffffffff) & bit.color;//(((j + k) / 5 * 0xff) << 24) | (0x00ffffff & bit.color);
							}
							
							colFactor = (int)((float) (DRIP_BOUND - (j + k)) / DRIP_BOUND * 255 + (Math.random() * DRIP_STEP * 255));							
							color = (255 << 24 | colFactor << 16 | colFactor << 8 | colFactor) & bit.color;
							
							int c = normX + ((normY + j) * Vertex.WIDTH) - k;
							if(c >= 0 && c < image.pixels.length) {
								// image.pixels[c] = color;
								image.pixels[c] = ColorUtil.blendColor(color, image.pixels[c]); 

									// (((j + k) / 5) * 0xffffffff) & bit.color;//(((j + k) / 5 * 0xff) << 24) | (0x00ffffff & bit.color);
							}
							
							colFactor = (int)((float) (DRIP_BOUND - (j + k)) / DRIP_BOUND * 255 + (Math.random() * DRIP_STEP * 255));							
							color = (255 << 24 | colFactor << 16 | colFactor << 8 | colFactor) & bit.color;
							
							int d = normX + ((normY - j) * Vertex.WIDTH) + k;
							if(d >= 0 && d < image.pixels.length) {
								// image.pixels[d] = color;
								image.pixels[d] = ColorUtil.blendColor(color, image.pixels[d]); 

								// (((j + k) / 5) * 0xffffffff) & bit.color;//(((j + k) / 5 * 0xff) << 24) | (0x00ffffff & bit.color);
							}
						}
					}
				}
				
			}
		}
	}
	
}
