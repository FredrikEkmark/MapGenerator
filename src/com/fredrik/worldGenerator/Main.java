package com.fredrik.worldGenerator;


import com.fredrik.worldGenerator.tile.TileInturpetation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;


public class Main {

    public static void main(String[] args) throws IOException {

        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < 200; i++) {
            for (int j = 0; j < 200; j++) {
                image.setRGB(i, j, 0xc48937);
            }
        }
        ImageIO.write(image, "png", new File("test.png"));







    }
}
