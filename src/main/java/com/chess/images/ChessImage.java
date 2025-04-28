/*
 * Copyright (c) 2024 
 * George Miller
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * ----------------------------------------------------------------------------
 *
 * Class: ChessImage
 *
 * This class manages the creation, manipulation, and saving of images for chess components.
 * It allows images to be loaded from files or from resources within a JAR file, and provides
 * methods to manipulate pixel data (RGB values) and save the image in various formats.
 *
 * Key functionalities include:
 * - Loading images from file paths or resources (like chess pieces and boards).
 * - Saving images in multiple formats (PNG, JPG, BMP).
 * - Setting and retrieving individual pixel values (RGB and Alpha components).
 */

package com.chess.images;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

public class ChessImage {

    /**
     * The BufferedImage that holds the image data for the chess image.
     */
    private static BufferedImage image;

    /**
     * Creates a new ChessImage based on the file path to an existing image.
     * This image must be in a supported format (e.g., PNG, JPG).
     *
     * @param path The path to the image file on disk.
     * @throws IOException If there is an error reading the file.
     */
    public ChessImage(String path) throws IOException {
        File file = new File(path);
        image = ImageIO.read(file);
    }

    /**
     * Creates a new blank image with an optional colored background.
     * If the color is null, the image will be opaque with no specific color.
     *
     * @param width The width of the image.
     * @param height The height of the image.
     * @param color The background color of the image (nullable).
     */
    public ChessImage(int width, int height, Color color) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        if (color != null) {
            Graphics g = image.createGraphics();
            g.setColor(color);
            g.fillRect(0, 0, width, height);  // Fill the background with the specified color
        }
    }

    // Path to the image directory in the JAR file after compiled.
    private static final String IMAGE_DIRECTORY = "/images/";

    /**
     * Loads a chess piece image from the resources stored in the JAR file.
     * This can be used for displaying chess pieces and boards.
     *
     * @param piece The chess piece whose image is being loaded.
     * @throws IOException If the image file cannot be found or loaded.
     */
    public ChessImage(ChessPiece piece) throws IOException {
        // Construct the file path for the chess piece image
        String filePath = IMAGE_DIRECTORY + piece.getFileName();
        //System.out.println("Opening the file called: " + filePath);

        // Load the image as a resource stream from the classpath
        try (InputStream resourceStream = ChessImage.class.getResourceAsStream(filePath)) {
            if (resourceStream != null) {
                image = ImageIO.read(resourceStream);
            } else {
                throw new IOException("File not found: " + filePath);
            }
        }
    }

    /**
     * Saves the current BufferedImage to the specified file path.
     * The format is determined by the file extension (.png, .jpg/.jpeg, .bmp).
     *
     * @param path The file path where the image will be saved.
     * @throws IOException If an error occurs during the save process or if the file format is unsupported.
     */
    public void save(String path) throws IOException {
        String lowerCasePath = path.toLowerCase();
        if (lowerCasePath.endsWith(".png")) {
            ImageIO.write(image, "png", new File(path));
        } else if (lowerCasePath.endsWith(".jpg") || lowerCasePath.endsWith(".jpeg")) {
            ImageIO.write(image, "jpg", new File(path));
        } else if (lowerCasePath.endsWith(".bmp")) {
            ImageIO.write(image, "bmp", new File(path));
        } else {
            throw new IOException("Unsupported image file type in save path!");
        }
    }

    /**
     * Sets the RGB values of a pixel at the specified (x, y) coordinates.
     *
     * @param x The x-coordinate of the pixel.
     * @param y The y-coordinate of the pixel.
     * @param red The red component (0-255).
     * @param green The green component (0-255).
     * @param blue The blue component (0-255).
     */
    public void setRGB(int x, int y, int red, int green, int blue) {
        Color color = new Color(red, green, blue);
        image.setRGB(x, y, color.getRGB());
    }

    /**
     * Sets the RGBA (red, green, blue, alpha) values of a pixel at the specified (x, y) coordinates.
     *
     * @param x The x-coordinate of the pixel.
     * @param y The y-coordinate of the pixel.
     * @param red The red component (0-255).
     * @param green The green component (0-255).
     * @param blue The blue component (0-255).
     * @param alpha The alpha (transparency) component (0-255).
     */
    public void setRGB(int x, int y, int red, int green, int blue, int alpha) {
        Color color = new Color(red, green, blue, alpha);
        image.setRGB(x, y, color.getRGB());
    }

    /**
     * Gets the RGB value of the pixel at the specified (x, y) coordinates.
     *
     * @param x The x-coordinate of the pixel.
     * @param y The y-coordinate of the pixel.
     * @return The RGB value as an integer.
     */
    public int getRGB(int x, int y) {
        return image.getRGB(x, y);
    }

    /**
     * Gets the red component of the pixel at the specified (x, y) coordinates.
     *
     * @param x The x-coordinate of the pixel.
     * @param y The y-coordinate of the pixel.
     * @return The red component (0-255).
     */
    public int getRed(int x, int y) {
        Color color = new Color(image.getRGB(x, y), true);
        return color.getRed();
    }

    /**
     * Gets the green component of the pixel at the specified (x, y) coordinates.
     *
     * @param x The x-coordinate of the pixel.
     * @param y The y-coordinate of the pixel.
     * @return The green component (0-255).
     */
    public int getGreen(int x, int y) {
        Color color = new Color(image.getRGB(x, y), true);
        return color.getGreen();
    }

    /**
     * Gets the blue component of the pixel at the specified (x, y) coordinates.
     *
     * @param x The x-coordinate of the pixel.
     * @param y The y-coordinate of the pixel.
     * @return The blue component (0-255).
     */
    public int getBlue(int x, int y) {
        Color color = new Color(image.getRGB(x, y), true);
        return color.getBlue();
    }

    /**
     * Gets the alpha (transparency) component of the pixel at the specified (x, y) coordinates.
     *
     * @param x The x-coordinate of the pixel.
     * @param y The y-coordinate of the pixel.
     * @return The alpha component (0-255).
     */
    public int getAlpha(int x, int y) {
        Color color = new Color(image.getRGB(x, y), true);
        return color.getAlpha();
    }
}
