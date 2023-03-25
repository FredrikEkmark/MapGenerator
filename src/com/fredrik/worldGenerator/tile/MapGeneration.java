package com.fredrik.worldGenerator.tile;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;

public class MapGeneration
{
    private static final int WIDTH = 3000;
    private static final int HEIGHT = 1500;

    public static void main(String[] args)
            throws IOException {

        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

        int HEIGHT_PART = HEIGHT/10;

        for (int y = 0; y < HEIGHT; y++)
        {
            for (int x = 0; x < WIDTH; x++) {
                int value;
                if (y == HEIGHT_PART/2 || y == HEIGHT_PART*2 || y == (HEIGHT_PART/2)*19 || y == HEIGHT_PART*8 || y == HEIGHT_PART*3.5 || y == HEIGHT_PART*6.5 || y == HEIGHT_PART*4.5 || y == HEIGHT_PART*5.5) {
                    value = 0x990101;
                    image.setRGB(x, y, value);
                } else {
                    value = TileInturpetation.tileValue(x, y);
                    image.setRGB(x, y, TileInturpetation.TEMP_PAINTER(value));
                }
            }
        }
        ImageIO.write(image, "png", new File("worldMap.png"));
    }
}
