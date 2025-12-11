
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import Texture.TextureReader;
import java.awt.event.*;
import java.io.IOException;
import javax.media.opengl.*;

import java.util.ArrayList;
import java.util.BitSet;
import javax.media.opengl.glu.GLU;
import com.sun.opengl.util.j2d.TextRenderer;
import java.awt.Font;

public class AnimGLEventListener4 extends AnimListener {

    int animationIndex = 0;
    int maxWidth = 100;
    int maxHeight = 100;
    double x = maxWidth/2, y = maxHeight/2;

    int score = 0;
    TextRenderer scoreRenderer = new TextRenderer(new Font("Arial", Font.BOLD, 22));


    double[] eggStartX = { 12, 32, 52, 72, 92 };
    double eggStartY = 95;

    ArrayList<Egg> eggs = new ArrayList<>();

    long lastEggTime = 0;

    class Egg {
        double x, y;
        boolean active = true;

        Egg(double x, double y) {
            this.x = x;
            this.y = y;
        }

        void update() {
            y -= 0.7; //  speed
            if (y < -5) active = false;
        }
    }


    String textureNames[] = {"basket.png","egg.png","back.png"};
    TextureReader.Texture texture[] = new TextureReader.Texture[textureNames.length];
    int textures[] = new int[textureNames.length];


    public void init(GLAutoDrawable gld) {

        GL gl = gld.getGL();
        gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);    //This Will Clear The Background Color To Black

        gl.glEnable(GL.GL_TEXTURE_2D);  // Enable Texture Mapping
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glGenTextures(textureNames.length, textures, 0);

        for(int i = 0; i < textureNames.length; i++){
            try {
                texture[i] = TextureReader.readTexture(assetsFolderName + "//" + textureNames[i] , true);
                gl.glBindTexture(GL.GL_TEXTURE_2D, textures[i]);

//                mipmapsFromPNG(gl, new GLU(), texture[i]);
                new GLU().gluBuild2DMipmaps(
                        GL.GL_TEXTURE_2D,
                        GL.GL_RGBA, // Internal Texel Format,
                        texture[i].getWidth(), texture[i].getHeight(),
                        GL.GL_RGBA, // External format from image,
                        GL.GL_UNSIGNED_BYTE,
                        texture[i].getPixels() // Imagedata
                );
            } catch( IOException e ) {
                System.out.println(e);
                e.printStackTrace();
            }
        }
    }

    public void display(GLAutoDrawable gld) {

        GL gl = gld.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);       //Clear The Screen And The Depth Buffer
        gl.glLoadIdentity();

        DrawBackground(gl);
        handleKeyPress();

        DrawSprite(gl, x, 1, 0, 2.25f);

        long now = System.currentTimeMillis();
        if (now - lastEggTime >= 1000) {
            int r = (int)(Math.random() * 5);
            eggs.add(new Egg(eggStartX[r], eggStartY));
            lastEggTime = now;
        }


        for (int i = 0; i < eggs.size(); i++) {
            Egg e = eggs.get(i);

            if (!e.active) {
                eggs.remove(i);
                i--;
                continue;
            }

            e.update();
            DrawSprite(gl, (e.x)-10, (e.y)-40, 1, 0.5f); // egg = index 1
        }

        drawScore(gl, gld, score);
        checkEggCaught();
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    }

    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
    }

    public void DrawSprite(GL gl,double x, double y, int index, float scale){
        gl.glEnable(GL.GL_BLEND);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[index]);	// Turn Blending On

        gl.glPushMatrix();
        gl.glTranslated( x/(maxWidth/2.0) - 0.9, y/(maxHeight/2.0) - 0.9, 0);
        gl.glScaled(0.1*scale, 0.1*scale, 1);
//        gl.glRotated(angle,0,0,1);
        //System.out.println(x +" " + y);
        gl.glBegin(GL.GL_QUADS);
        // Front Face
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f);
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f);
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(1.0f, 1.0f, -1.0f);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);
        gl.glEnd();
        gl.glPopMatrix();

        gl.glDisable(GL.GL_BLEND);
    }

    public void DrawBackground(GL gl){
        gl.glEnable(GL.GL_BLEND);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[2]);	// Turn Blending On

        gl.glPushMatrix();
        gl.glBegin(GL.GL_QUADS);
        // Front Face
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f);
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f);
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(1.0f, 1.0f, -1.0f);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);
        gl.glEnd();
        gl.glPopMatrix();

        gl.glDisable(GL.GL_BLEND);
    }


    public void drawScore(GL gl, GLAutoDrawable gld, int score) {

        GLCanvas glc = (GLCanvas) gld; // cast drawable to canvas

        // Save previous OpenGL states
        gl.glPushAttrib(GL.GL_ENABLE_BIT | GL.GL_CURRENT_BIT);

        // Disable states that conflict with TextRenderer
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glDisable(GL.GL_LIGHTING);
        gl.glDisable(GL.GL_BLEND);

        // Draw text
        scoreRenderer.beginRendering(glc.getWidth(), glc.getHeight());
        scoreRenderer.setColor(1f, 1f, 1f, 1f);  // white text
        scoreRenderer.draw("Score : " + score, 20, glc.getHeight() - 40);
        scoreRenderer.endRendering();

        // Restore GL state
        gl.glPopAttrib();

        gl.glEnable(GL.GL_TEXTURE_2D);
    }

    public void checkEggCaught() {
        double basketLeft = x;                    // basket X
        double basketRight = x + 10;             // basket width
        double basketY = 1;                       // basket Y

        for (int i = 0; i < eggs.size(); i++) {
            Egg e = eggs.get(i);
            if (!e.active) continue;

            // Check if egg reaches basket level and is inside basket horizontally
            if (e.y <= basketY + 1 && e.x >= basketLeft && e.x <= basketRight) {
                score++;        // increase score
                e.active = false; // remove egg
            }
        }
    }



    /*
     * KeyListener
     */

    public void handleKeyPress() {

        if (isKeyPressed(KeyEvent.VK_LEFT)) {
            if (x > 0) {
                x--;
            }
            animationIndex++;
        }
        if (isKeyPressed(KeyEvent.VK_RIGHT)) {
            if (x < maxWidth-10) {
                x++;
            }
            animationIndex++;
        }

    }

    public BitSet keyBits = new BitSet(256);

    @Override
    public void keyPressed(final KeyEvent event) {
        int keyCode = event.getKeyCode();
        keyBits.set(keyCode);
    }

    @Override
    public void keyReleased(final KeyEvent event) {
        int keyCode = event.getKeyCode();
        keyBits.clear(keyCode);
    }

    @Override
    public void keyTyped(final KeyEvent event) {
        // don't care
    }

    public boolean isKeyPressed(final int keyCode) {
        return keyBits.get(keyCode);
    }
}
