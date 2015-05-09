/**
 *  Wrath Engine
 *  Copyright (C) 2015 Trent Spears
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package wrath.client;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL20;
import wrath.client.enums.PopupMessageType;
import wrath.util.Logger;

/**
 * General static utilities library.
 * Contains assorted methods for the Client to use.
 * @author Trent Spears
 */
public class ClientUtils
{
    /**
     * Static libraries, no constructor necessary.
     */
    private ClientUtils(){}
    
    /**
     * Converts an array of primitive bytes to a {@link java.nio.ByteBuffer}.
     * @param data The byte array to convert.
     * @return Returns the {@link java.nio.ByteBuffer} form of the byte array.
     */
    public static ByteBuffer byteArrayToByteBuffer(byte[] data)
    {
        ByteBuffer ret = ByteBuffer.allocateDirect(data.length).order(ByteOrder.nativeOrder());
        ret.put(data).flip();
        return ret;
    }
    
    /**
     * Displays a pop-up message.
     * @param popupTitle The title of the pop-up message.
     * @param message The message displayed on the pop-up.
     * @param type The type of message to display. Changes the icon.
     */
    public static void displayPopupMessage(String popupTitle, String message, PopupMessageType type)
    {
        int opt = JOptionPane.INFORMATION_MESSAGE;
        
        if(type == null) opt = JOptionPane.PLAIN_MESSAGE;    
        else if(type == PopupMessageType.ERROR) opt = JOptionPane.ERROR_MESSAGE;
        else if(type == PopupMessageType.INFO) opt = JOptionPane.INFORMATION_MESSAGE;
        else if(type == PopupMessageType.PLAIN) opt = JOptionPane.PLAIN_MESSAGE;
        else if(type == PopupMessageType.QUESTION) opt = JOptionPane.QUESTION_MESSAGE;
        else if(type == PopupMessageType.WARNING) opt = JOptionPane.WARNING_MESSAGE;
        
        JOptionPane.showMessageDialog(null, message, popupTitle, opt);
    }
    
    /**
     * Converts an array of primitive floats to a {@link java.nio.FloatBuffer}.
     * @param data The float array to convert.
     * @return Returns the {@link java.nio.FloatBuffer} form of the float array.
     */
    public static FloatBuffer floatArrayToFloatBuffer(float[] data)
    {
        FloatBuffer ret = ByteBuffer.allocateDirect(data.length << 2).order(ByteOrder.nativeOrder()).asFloatBuffer();
        ret.put(data).flip();
        return ret;
    }
    
    /**
     * Converts a ByteBuffer (Used in OpenGL) to a BufferedImage.
     * @param buffer The ByteBuffer that contains the image data.
     * @param width The Width (in pixels) of the image.
     * @param height The Height (in pixels) of the image.
     * @return Returns the BufferedImage that contains the data from the ByteBuffer.
     */
    public static BufferedImage getByteBufferToImage(ByteBuffer buffer, int width, int height)
    {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        for(int x = 0; x < width; x++) 
        {
            for(int y = 0; y < height; y++) 
            {
                int i = (x + (width * y)) * 4;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                img.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
            }
        }
        
        return img;
    }
    
    /**
     * Loads a ByteBuffer (Used in OpenGL) from most images.
     * @param image Image to convert.
     * @return Returns the ByteBuffer converting from the original image.
     */
    public static ByteBuffer getImageToByteBuffer(BufferedImage image) 
    {
        try
        {
            byte[] imageBuffer = new byte[image.getWidth()*image.getHeight()*4];
            int counter = 0;
            for(int i = 0; i < image.getWidth(); i++)
            {
                for(int j = 0; j < image.getHeight(); j++)
		{
                    int colorSpace = image.getRGB(j, i);
                    imageBuffer[counter + 0] =(byte)((colorSpace << 8) >> 24 );
                    imageBuffer[counter + 1] =(byte)((colorSpace << 16) >> 24 );
                    imageBuffer[counter + 2] =(byte)((colorSpace << 24) >> 24 );
                    imageBuffer[counter + 3] =(byte)(colorSpace >> 24 );
                    counter += 4;
		}
            }
            return ByteBuffer.wrap(imageBuffer);
        }
        catch(NullPointerException e)
        {
            Logger.getErrorLogger().log("Could not convert Image to ByteBuffer! Operations Error has occured!");
        }
        
        return ByteBuffer.allocate(0);
    }
    
    /**
     * Converts a primitive Java boolean to the LWJGL version of the boolean.
     * Mostly a convenience method.
     * @param bool The primitive Java boolean value.
     * @return Returns the LWJGL version of the boolean.
     */
    public static int getLWJGLBoolean(boolean bool)
    {
        if(bool) return GL11.GL_TRUE;
        else return GL11.GL_FALSE;
    }
    
    /**
     * Loads a LWJGL Texture from an image.
     * @param image The {@link java.awt.image.BufferedImage} version of the Texture.
     * @return Returns the LWJGL texture id.
     */
    public static int get2DTexture(BufferedImage image)
    {
        if(image == null) return 0;
        
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);

        for(int y = 0; y < image.getHeight(); y++)
        {
            for(int x = 0; x < image.getWidth(); x++)
            {
                int pixel = pixels[y * image.getWidth() + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
        }
        buffer.flip();
        
        int id = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        return id;
    }
    
    /**
     * Converts an array of primitive ints to a {@link java.nio.IntBuffer}.
     * @param data The int array to convert.
     * @return Returns the {@link java.nio.IntBuffer} form of the int array.
     */
    public static IntBuffer intArrayToIntBuffer(int[] data)
    {
        IntBuffer ret = ByteBuffer.allocateDirect(data.length << 2).order(ByteOrder.nativeOrder()).asIntBuffer();
        ret.put(data).flip();
        return ret;
    }
    
    /**
     * Loads a {@link java.awt.image.BufferedImage} from a {@link java.io.File}.
     * @param file The {@link java.io.File} to load the {@link java.awt.image.BufferedImage} from.
     * @return Returns the {@link java.awt.image.BufferedImage} located in the {@link java.io.File}.
     */
    public static BufferedImage loadImageFromFile(File file)
    {
        if(file == null) return null;
        
        BufferedImage ret = null;
        
        try
        {
            ret = ImageIO.read(file);
        }
        catch(IOException e)
        {
            Logger.getErrorLogger().log("Could not load image from file! I/O Error!");
        }
        
        return ret;
    }
    
    /**
     * Reads the specified shader file and retrieves it's source in {@link java.lang.String} format.
     * @param shaderFile The {@link java.io.File} to read the shader from.
     * @return Returns the String source of the shader.
     */
    public static String loadShaderSource(File shaderFile)
    {
        if(!shaderFile.exists())
        {
            Logger.getErrorLogger().log("Could not load shader from file '" + shaderFile.getAbsolutePath() + "'! File not found!");
            return null;
        }
        
        String src = "";
        
        try
        {
            String inp;
            try(BufferedReader read = new BufferedReader(new FileReader(shaderFile))) 
            {
                while((inp = read.readLine()) != null) src = src + inp + '\n';
            }
        }
        catch(IOException e)
        {
            Logger.getErrorLogger().log("Could not load shader from file '" + shaderFile.getAbsolutePath() + "'! I/O Error!");
            return null;
        }
        
        return src;
    }
    
    /**
     * Reads the two specified shader files and compiles the shaders into an OpenGL program format.
     * @param vertFile The {@link java.io.File} to read the vert shader from.
     * @param fragFile The {@link java.io.File} to read the frag shader from.
     * @return Returns the integer id of the shader program (as OpenGL uses).
     */
    public static int loadShaderProgram(File vertFile, File fragFile)
    {
        if(!vertFile.exists())
        {
            Logger.getErrorLogger().log("Could not load shader from file '" + vertFile.getAbsolutePath() + "'! File not found!");
            return -1;
        }
        
        if(!fragFile.exists())
        {
            Logger.getErrorLogger().log("Could not load shader from file '" + fragFile.getAbsolutePath() + "'! File not found!");
            return -1;
        }
        
        String vsrc = "";
        
        try
        {
            String inp;
            try(BufferedReader read = new BufferedReader(new FileReader(vertFile))) 
            {
                while((inp = read.readLine()) != null) vsrc = vsrc + inp + '\n';
            }
        }
        catch(IOException e)
        {
            Logger.getErrorLogger().log("Could not load shader from file '" + vertFile.getAbsolutePath() + "'! I/O Error!");
            return -1;
        }
        
        String fsrc = "";
        
        try
        {
            String inp;
            try(BufferedReader read = new BufferedReader(new FileReader(fragFile))) 
            {
                while((inp = read.readLine()) != null) fsrc = fsrc + inp + '\n';
            }
        }
        catch(IOException e)
        {
            Logger.getErrorLogger().log("Could not load shader from file '" + fragFile.getAbsolutePath() + "'! I/O Error!");
            return -1;
        }
        
        int prog = GL20.glCreateProgram();
        int vert = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        int frag = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        
        GL20.glShaderSource(vert, vsrc);
        GL20.glShaderSource(frag, fsrc);
        
        GL20.glCompileShader(vert);
        if(GL20.glGetShaderi(vert, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE)
        {
            Logger.getErrorLogger().log("Could not load shader from file '" + vertFile.getAbsolutePath() + "'! Compile Error:");
            Logger.getErrorLogger().log(GL20.glGetShaderInfoLog(vert));
            return -1;
        }
        
        GL20.glCompileShader(frag);
        if(GL20.glGetShaderi(frag, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE)
        {
            Logger.getErrorLogger().log("Could not load shader from file '" + fragFile.getAbsolutePath() + "'! Compile Error:");
            Logger.getErrorLogger().log(GL20.glGetShaderInfoLog(frag));
            return -1;
        }
        
        GL20.glAttachShader(prog, vert);
        GL20.glAttachShader(prog, frag);
        GL20.glLinkProgram(prog);
        GL20.glValidateProgram(prog);
        
        return prog;
    }
    
    /**
     * Opens the URL in the System's default web browser.
     * @param url The URL in the form of a String.
     */
    public static void openUrlInBrowser(String url)
    {   
        if(Desktop.isDesktopSupported())
        {
            if(Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
            {
                try 
                {
                    Desktop.getDesktop().browse(URI.create(url));
                }
                catch(IOException e)
                {
                    Logger.getErrorLogger().log("Could not open URL '" + url + "' in browser! URL may not be valid!");
                }
            }
            else
            {
                Logger.getErrorLogger().log("Could not open URL '" + url + "' in browser! Action not supported!");
            }
        }
    }
    
    /**
     * Displays an error message in modal form, and closes the program if fatal.
     * @param message The message to display to the user.
     * @param fatal Determines whether or not the error is fatal, if fatal the program will close.
     */
    public static void throwInternalError(String message, boolean fatal)
    {
        JOptionPane.showMessageDialog(null, message, "!! INTERNAL ERROR !!", JOptionPane.ERROR_MESSAGE);
        if(fatal) System.exit(0);
    }
}
