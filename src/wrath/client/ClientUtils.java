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
import java.awt.Desktop.Action;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;
import wrath.util.Logger;

/**
 * General static utilities library.
 * Contains assorted methods for the Client to use.
 * @author Trent Spears
 */
public class ClientUtils
{
    /**
     * Describes the format of an image.
     */
    public static enum ImageFormat {GIF, JPEG, PNG;}
    
    /**
     * Static libraries, no constructor necessary.
     */
    private ClientUtils(){}
    
    /**
     * Loads a TrueTypeFont from a regular java font.
     * @param javaFont The Font from {@link java.awt.Font}.
     * @param antiAliasing Whether or not to make the edges look smoother.
     * @return Returns the converted font.
     */
    public static TrueTypeFont convertFontFromJavaFont(Font javaFont, boolean antiAliasing)
    {
        return new TrueTypeFont(javaFont, antiAliasing);
    }
    
    /**
     * Loads a Java AWT Font from a file.
     * @param file The file to load the font from.
     * @return Returns the font object.
     */
    public static Font getFontFromFile(File file)
    {
        Font awtFont = null;
        
        try(InputStream inputStream = ResourceLoader.getResourceAsStream("myfont.ttf")) 
        {
            awtFont = Font.createFont(Font.TRUETYPE_FONT, inputStream);
        }
        catch(FontFormatException e)
        {
            Logger.getErrorLogger().log("Could not load font from '" + file.getName() + "'! Not a valid font format!");
        }
        catch(IOException e)
        {
            Logger.getErrorLogger().log("Could not load font from '" + file.getName() + "'! I/O Error has occured!");
        }
        
        return awtFont;
    }
    
    /**
     * Loads a ByteBuffer (Used in OpenGL) from most images.
     * @param file File to load image from.
     * @return Returns the ByteBuffer converting from the original image.
     */
    public static ByteBuffer getImageToByteBuffer(File file) 
    {
        try
        {
            BufferedImage image = ImageIO.read(file);
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
        catch(IOException | NullPointerException e)
        {
            Logger.getErrorLogger().log("Could not convert Image to ByteBuffer! I/O Error has occured!");
        }
        
        return ByteBuffer.allocate(0);
    }
    
    /**
     * Loads a Slick-Utils/LWJGL Texture from an image.
     * @param file The file to load the texture from.
     * @return Returns the texture object, null if could not be read.
     */
    public static Texture getTexture(File file)
    {
        // Format Detection
        ImageFormat format;
        
        if(file.getName().endsWith(".png") || file.getName().endsWith(".PNG")) format = ImageFormat.PNG;
        else if(file.getName().endsWith(".jpg") || file.getName().endsWith(".JPG")) format = ImageFormat.JPEG;
        else if(file.getName().endsWith(".jpeg") || file.getName().endsWith(".JPEG")) format = ImageFormat.JPEG;
        else if(file.getName().endsWith(".gif") || file.getName().endsWith(".GIF")) format = ImageFormat.GIF;
        else
        {
            Logger.getErrorLogger().log("Could not load texture from '" + file.getName() + "'! Texture is in an unsupported format!");
            return null;
        }
        
        // Load the texture
        Texture t;
        try (FileInputStream fis = new FileInputStream(file)) 
        {
            t = TextureLoader.getTexture(format.name(), fis);
            fis.close();
            return t;
        }
        catch(IOException e)
        {
            Logger.getErrorLogger().log("Could not load texture from '" + file.getName() + "'! I/O Error occured or file not found!");
        }
        return null;
    }
    
    /**
     * Opens the URL in the System's default web browser.
     * @param url The URL in the form of a String.
     */
    public static void openUrlInBrowser(String url)
    {   
        if(Desktop.isDesktopSupported())
        {
            Desktop desktop = Desktop.getDesktop();
            if(desktop.isSupported(Action.BROWSE))
            {
                try 
                {
                    desktop.browse(URI.create(url));
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
