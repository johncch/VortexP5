package com.fifthrevision;

public class ColorUtil {

	public static int HSVtoRGB(float hue, float sat, float value, float alpha) {
		float x = hue / 255; // h
		float y = sat / 255; // s
		float z = value / 255; // b

		float calcR = 0, calcG = 0, calcB = 0;
		//float calcA = alpha;

		if (y == 0) {  // saturation == 0
			calcR = calcG = calcB = z;
		} else {
			float which = (x - (int)x) * 6.0f;
			float f = which - (int)which;
			float p = z * (1.0f - y);
			float q = z * (1.0f - y * f);
			float t = z * (1.0f - (y * (1.0f - f)));

			switch ((int)which) {
			case 0: calcR = z; calcG = t; calcB = p; break;
			case 1: calcR = q; calcG = z; calcB = p; break;
			case 2: calcR = p; calcG = z; calcB = t; break;
			case 3: calcR = p; calcG = q; calcB = z; break;
			case 4: calcR = t; calcG = p; calcB = z; break;
			case 5: calcR = z; calcG = p; calcB = q; break;
			}
		}
		int calcRi = (int)(255*calcR); int calcGi = (int)(255*calcG);
		int calcBi = (int)(255*calcB); // int calcAi = (int)(255*calcA);
		// System.out.println("Color: " + calcRi + ", " + calcGi + ", " + calcBi);
		return ((int) alpha << 24) | (calcRi << 16) | (calcGi << 8) | calcBi;
	}
	
	public static int red(int color) {
		return (color >> 16) & 0xff;
	}
	
	public static int green(int color) {
		return (color >> 8) & 0xff;
	}

	public static int blue(int color) {
		return color & 0xff;
	}
	
	public static int alpha(int color) {
		return (color >> 24) & 0xff;
	}
	
	public static int blendColor(int color1, int color2) {
		int a1 = color1 >> 24;
		int a2 = color2 >> 24;
		int r1 = (color1 >> 16) & 0xff;
		int r2 = (color2 >> 16) & 0xff;
		int g1 = (color1 >> 8) & 0xff;
		int g2 = (color2 >> 8) & 0xff;
		int b1 = color1 & 0xff;
		int b2 = color2 & 0xff;		

		if(r1 == 0 && g1 == 0 && b1 == 0) {
			return color2;
		} else if (r2 == 0 && g2 == 0 && b2 == 0) {
			return color1;
		}
		
		float ratio1 = ((float)a1 / (a1 + a2));
		float ratio2 = ((float)a2 / (a1 + a2));
		// System.out.println(ratio1 + ", " + ratio2);
		
		int r = (int)(ratio1*r1 + ratio2 * r2);
		int g = (int)(ratio1*g1 + ratio2 * g2);
		int b = (int)(ratio1*b1 + ratio2 * b2);
		
		// System.out.println(r + ", " + g + ", " + b);
		
		return (255 << 24 | r << 16 | g << 8 | b); 
	}
	
}
