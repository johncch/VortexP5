package com.fifthrevision;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.media.opengl.GL;

/* import controlP5.Button;
import controlP5.ControlGroup;
import controlP5.ControlP5;
import controlP5.Textarea; */
import controlP5.Textfield;
import ddf.minim.AudioInput;
import ddf.minim.Minim;
import ddf.minim.analysis.FFT;
import processing.core.PApplet;
import processing.core.PImage;
import processing.opengl.*;
import traer.physics.Particle;
import traer.physics.ParticleSystem;

public class Vertex extends PApplet {

	private static final long serialVersionUID = 1L;
	
	public static final Boolean DEBUG = false;
	public static final int WIDTH = 1024;
	public static final int HEIGHT = 768;
	
	public static final int SAMPLING_FREQ = 8000;
	public static final int BUFFER_SIZE = 1024;
	
	public static final int MAX_FREQ = 1024;
	public static final int NUM_VERTEX = 6;
		
	public static final int DIAMETER = 50;
	public static final int RADIUS = DIAMETER / 2;
	
	public static final int ATTRACTION_FACTOR = 1000;
	public static final int REPELLANT_FACTOR = -1000;
	public static final float BOUNDARY_ATTRITION = -0.9f;

	public static final int THRESHOLD_FACTOR = 4;
	
	public static final int FRAME_RATE = 15;
	
	private float freqBand = 0;
	private int numSamples = 0;
	private float[] vertexSums;
	
	private Minim minim;
	private AudioInput in;
	
	private ParticleSystem physics;
	private Particle[] vertexes;
	
	private BitsSystem bitsys;
	
	private int currentFrame = 0;
	
	// ControlP5 stuff
/*	private Textarea textarea;
	private Button button;
	private Textfield maxFFT;
	private Textfield maxFreq;*/
	
	private Textfield[] vertexTextfields;
	
	private Boolean paused = false;
	
	private FFT fft;
	
	private PImage canvas;
	
	public void setup() {
		int i, j;
		
		size(WIDTH, HEIGHT, OPENGL);
		colorMode(HSB);
		
		// hint(ENABLE_OPENGL_4X_SMOOTH);
		frameRate(FRAME_RATE);
		
		minim = new Minim(this);
		// minim.debugOn();
		in = minim.getLineIn(Minim.MONO, BUFFER_SIZE, SAMPLING_FREQ);
		fft = new FFT(in.bufferSize(), in.sampleRate());
		freqBand = (SAMPLING_FREQ / 2) / fft.specSize();
		numSamples = (int) Math.ceil((MAX_FREQ - (MAX_FREQ % freqBand)) / (NUM_VERTEX * freqBand));
		vertexSums = new float[NUM_VERTEX];
		// println("num samples is " + numSamples);
		// println("Frequency band is " + freqBand);
		
		physics = new ParticleSystem();
		// physics.setIntegrator(ParticleSystem.MODIFIED_EULER);
		bitsys = new BitsSystem();
		
		if(DEBUG) {
			/* ControlP5 c3p0 = new ControlP5(this);
			button = c3p0.addButton("Pause", 0, 20, 20, 60, 20);
			vertexTextfields = new Textfield[NUM_VERTEX];
			for(i = 0; i < NUM_VERTEX; i++) {
				vertexTextfields[i] = c3p0.addTextfield("Vertex " + i, 120, i * 40 + 20, 80, 20);
			}*/
		}
		
		vertexes = new Particle[NUM_VERTEX];
		for(i = 0; i < NUM_VERTEX; i++) {
			 vertexes[i] = physics.makeParticle(1.0f, WIDTH/2 + ((float) Math.random() * 200), HEIGHT/2 + ((float) Math.random() * 200), 0); 
		}
		
		for(i = 1; i < NUM_VERTEX; i++) {
			physics.makeAttraction(vertexes[i], vertexes[0], ATTRACTION_FACTOR, DIAMETER * 3);
			for(j = i + 1; j < NUM_VERTEX; j++) {
				physics.makeAttraction(vertexes[i], vertexes[j], REPELLANT_FACTOR, DIAMETER * 3);
			}	
		}
		
		canvas = createImage(WIDTH, HEIGHT, RGB);
	}
	
	private float oldTotal;
	
	public void draw() {		
		background(0);
		stroke(255);
		
		fft.forward(in.mix);
		int i, count;
		float total = 0;
		for(i = 0; i < NUM_VERTEX; i++){
			vertexSums[i] = 0;
		}
		
		for(i = 0, count = 0; i < NUM_VERTEX; count++) {
			vertexSums[i] += fft.getBand(count);
			total+= fft.getBand(i);
			if(count > 0 && count % numSamples == 0) {
				i++;
			}
		}
		
		for(i = 0; i < NUM_VERTEX; i++) {
			vertexSums[i] = vertexSums[i] - THRESHOLD_FACTOR;
			if (vertexSums[i] < 0) vertexSums[i] = 0;
			this.handleBoundaryCollisions(vertexes[i]);
		}

		float factor = total / oldTotal;
		if(factor == 0 || Float.isNaN(factor) || Float.isInfinite(factor)) { factor = 1; }
		if(factor > 1) factor /= 3;
		//System.out.println(oldTotal +", " + total + ", " + factor);
		vertexes[0].velocity().multiplyBy(factor);
		//println("vel: " + vertexes[0].velocity());
		oldTotal = total;
		
		physics.tick();
		
		//first pass
		/* for(i = 0; i < canvas.pixels.length; i++) {
			if(((canvas.pixels[i] >> 24) & 0xff) != 0x00) {
				canvas.pixels[i] = 0xffff0000;
			}
		}*/
		if(frameCount % (FRAME_RATE * 3) == 0) {
			fastblur(canvas, 1);
		}
		
		bitsys.paint(canvas);
		canvas.updatePixels();
		image(canvas, 0, 0, width, height);
		
		//fill(255);
		//ellipse(vertexes[0].position().x(), vertexes[0].position().y(), 50, 50);
		
		for(i = 0; i < NUM_VERTEX; i++) {
			float normVertexSum = vertexSums[i] - 10;
			
			//int color = color(255 / NUM_VERTEX * i + 1, 200, 255);
			/* fill(color);
			rect(i * 100, 0, 100, 100); */
				
			bitsys.addBits((int) vertexSums[i], (255 / NUM_VERTEX * i), (255 / NUM_VERTEX * (i + 1)),
					vertexes[i].position().x(), vertexes[i].position().y(), 
					// vertexes[i].velocity().x() + (float)(normVertexSum * Math.random() * 0.5), 
					// vertexes[i].velocity().y() + (float)(normVertexSum * Math.random() * 0.5));
					vertexes[i].velocity().x() + normVertexSum * noise(currentFrame) * 0.5f, 
					vertexes[i].velocity().y() + normVertexSum * noise(currentFrame) * 0.5f);
		}
		
		bitsys.update();
		
		PGraphicsOpenGL pgl = (PGraphicsOpenGL) g;         // processings opengl graphics object
        GL gl = pgl.beginGL();                // JOGL's GL object

        gl.glEnable( GL.GL_BLEND );             // enable blending

        gl.glBlendFunc(GL.GL_SRC_COLOR, GL.GL_ONE);  // additive blending (ignore alpha)
        gl.glEnable(GL.GL_LINE_SMOOTH);        // make points round
        gl.glLineWidth(1);

        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
        gl.glVertexPointer(2, GL.GL_FLOAT, 0, bitsys.posArray);

        gl.glEnableClientState(GL.GL_COLOR_ARRAY);
        gl.glColorPointer(3, GL.GL_FLOAT, 0, bitsys.colArray);

        gl.glDrawArrays(GL.GL_LINES, 0, bitsys.MAX_PARTICLES * 2);
		
        gl.glDisable(GL.GL_BLEND);
        pgl.endGL();
    	
        if(drawCircles) {
			for(i = 1; i < NUM_VERTEX; i++) {
				noFill();
				ellipse(vertexes[i].position().x(), vertexes[i].position().y(), 50, 50);
			}
		}
        
		if(DEBUG && !paused) {
			for(i = 0; i < NUM_VERTEX; i++) {
				vertexTextfields[i].setText("" + vertexSums[i]);
			}			
		}
		
		currentFrame++;
	}
	
	public boolean drawCircles = false;
	
	public void mousePressed() {
		drawCircles = !drawCircles;
	} 

	
	public void handleBoundaryCollisions( Particle p ) {
		if ( p.position().x() < 0 || p.position().x() > width )
			p.velocity().set( BOUNDARY_ATTRITION *p.velocity().x(), p.velocity().y(), 0 );
		if ( p.position().y() < 0 || p.position().y() > height ) 
			p.velocity().set( p.velocity().x(), BOUNDARY_ATTRITION *p.velocity().y(), 0 );
		p.position().set( constrain( p.position().x(), 0, width ), constrain( p.position().y(), 0, height ), 0 ); 
	}
	
	public void Pause() {
		paused = !paused;
	}
	
	public void stop() {
		in.close();
		minim.stop();
		
		super.stop();
	}
	
	private void fastblur(PImage img,int radius){

		  if (radius<1){
		    return;
		  }
		  int w=img.width;
		  int h=img.height;
		  int wm=w-1;
		  int hm=h-1;
		  int wh=w*h;
		  int div=radius+radius+1;
		  int r[]=new int[wh];
		  int g[]=new int[wh];
		  int b[]=new int[wh];
		  int rsum,gsum,bsum,x,y,i,p,p1,p2,yp,yi,yw;
		  int vmin[] = new int[max(w,h)];
		  int vmax[] = new int[max(w,h)];
		  int[] pix=img.pixels;
		  int dv[]=new int[256*div];
		  for (i=0;i<256*div;i++){
		     dv[i]=(i/div); 
		  }
		  
		  yw=yi=0;
		 
		  for (y=0;y<h;y++){
		    rsum=gsum=bsum=0;
		    for(i=-radius;i<=radius;i++){
		      p=pix[yi+min(wm,max(i,0))];
		      rsum+=(p & 0xff0000)>>16;
		      gsum+=(p & 0x00ff00)>>8;
		      bsum+= p & 0x0000ff;
		   }
		    for (x=0;x<w;x++){
		    
		      r[yi]=dv[rsum];
		      g[yi]=dv[gsum];
		      b[yi]=dv[bsum];

		      if(y==0){
		        vmin[x]=min(x+radius+1,wm);
		        vmax[x]=max(x-radius,0);
		       } 
		       p1=pix[yw+vmin[x]];
		       p2=pix[yw+vmax[x]];

		      rsum+=((p1 & 0xff0000)-(p2 & 0xff0000))>>16;
		      gsum+=((p1 & 0x00ff00)-(p2 & 0x00ff00))>>8;
		      bsum+= (p1 & 0x0000ff)-(p2 & 0x0000ff);
		      yi++;
		    }
		    yw+=w;
		  }
		  
		  for (x=0;x<w;x++){
		    rsum=gsum=bsum=0;
		    yp=-radius*w;
		    for(i=-radius;i<=radius;i++){
		      yi=max(0,yp)+x;
		      rsum+=r[yi];
		      gsum+=g[yi];
		      bsum+=b[yi];
		      yp+=w;
		    }
		    yi=x;
		    for (y=0;y<h;y++){
		      pix[yi]=0xff000000 | (dv[rsum]<<16) | (dv[gsum]<<8) | dv[bsum];
		      if(x==0){
		        vmin[y]=min(y+radius+1,hm)*w;
		        vmax[y]=max(y-radius,0)*w;
		      } 
		      p1=x+vmin[y];
		      p2=x+vmax[y];

		      rsum+=r[p1]-r[p2];
		      gsum+=g[p1]-g[p2];
		      bsum+=b[p1]-b[p2];

		      yi+=w;
		    }
		  }

		}
	
	public void init(){
        if(frame!=null){
          frame.removeNotify();//make the frame not displayable
          frame.setResizable(false);
          frame.setUndecorated(true);
          println("frame is at "+frame.getLocation());
          frame.addNotify();
        }
      super.init();
  }
	
	public static void main(String args[]) {
		
		int primary_display = 0; //index into Graphic Devices array...  

        int primary_width;

        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice devices[] = environment.getScreenDevices();
         String location;
         if(devices.length>1 ){ //we have a 2nd display/projector

             primary_width = devices[0].getDisplayMode().getWidth();
             location = "--location="+primary_width+",0";

         }else{//leave on primary display
             location = "--location=0,0";

         }

        String display = "--display="+primary_display+1;  //processing considers the first display to be # 1
        // PApplet.main(new String[] { location , "--hide-stop", display,"RazzleX" });
		
		// PApplet.main(new String[] { "--present", "com.fifthrevision.Vertex" });
        PApplet.main(new String[] { location , "--hide-stop", display,"com.fifthrevision.Vertex" });
	}
}
