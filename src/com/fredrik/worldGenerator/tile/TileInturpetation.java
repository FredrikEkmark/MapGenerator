package com.fredrik.worldGenerator.tile;

import com.fredrik.worldGenerator.PerlinNoise.OpenSimplex;

import java.util.LinkedList;
import java.util.Random;

public class TileInturpetation {

    private static final int WIDTH = 3000;
    private static final int WIDTH_PART = WIDTH/10;
    private static final int HEIGHT = 1500;
    private static final int HEIGHT_PART = HEIGHT/10;
    private static final double FREQUENCY1 = 1 / 36.0;
    private static final double FREQUENCY2 = 1 / 120.0;
    private static final double FREQUENCY3 = 1 / 340.0;
    private static final double FREQUENCY4 = 1 / 24.0;
    private static final double FREQUENCY5 = 1 / 6.0;

    private static final int SEED =  new Random().nextInt(1, 10000);

    private static final int WATER_PATH = spinSeed(SEED);

    private static LinkedList<Integer> eastList;
    private static LinkedList<Integer> westList;

    private static int farWest = 0;
    private static int farEast = 0;
    private static int middleWest = 0;
    private static int middleEast = 0;
    private static int closeWest = 0;
    private static int closeEast = 0;

    public static int tileValue(int x, int y) {

        if (x == 0) {
            farWest = 0;
            farEast = 0;
            middleWest = 0;
            middleEast = 0;
            closeWest = 0;
            closeEast = 0;
            eastList = new LinkedList<>();
            westList = new LinkedList<>();
            for (int i = 0; i < WIDTH_PART/3; i++) {
                eastList.add(terrain(terrainNoise(x +i, y), difNoise(x + i ,y), i, y));
                westList.add(terrain(terrainNoise(x + WIDTH - i, y), difNoise(x + WIDTH - i, y), i, y));
                if (i > WIDTH_PART/6) {
                    farWest += westList.getLast();
                    farEast += eastList.getLast();
                } else if (i > WIDTH_PART/30) {
                    middleWest += westList.getLast();
                    middleEast += eastList.getLast();
                } else {
                    closeWest += westList.getLast();
                    closeEast += eastList.getLast();
                }
            }
        }

        int terrainValue = moveList(x, y);

        Double terrainNoiseValue = terrainNoise(x,y);
        double difNoiseValue = difNoise(x,y);

        int temperatureValue = temperature(y, (terrainNoiseValue + difNoiseValue - 0.8)/3);
        int precipitationValue = precipitation(x, y, terrainValue, difNoiseValue);

        return terrainValue + temperatureValue + precipitationValue;
    };

    private static int moveList(int x, int y) {

        middleWest += westList.get(WIDTH_PART/30);
        farWest += westList.get(WIDTH_PART/6); //
        farWest -= westList.getLast(); //
        westList.removeLast();
        closeEast -= eastList.getFirst();
        closeEast += eastList.get(WIDTH_PART/30);
        middleEast += eastList.get(WIDTH_PART/6);
        westList.addFirst(eastList.removeFirst());
        middleEast -= eastList.get(WIDTH_PART/30);
        farEast -= eastList.get(WIDTH_PART/6); //
        closeWest += westList.getFirst();
        closeWest -= westList.get((WIDTH_PART/30));
        middleWest -= westList.get((WIDTH_PART/6));

        int value = westList.getFirst();

        eastList.add(terrain(terrainNoise(x + WIDTH_PART/3,y), difNoise(x,y), xSpin(x + WIDTH_PART/3), y));
        farEast += eastList.getLast(); //

        return value;
    }

    private static Double terrainNoise(int x, int y) {
        double value1 = OpenSimplex.noise3_ImproveXY(SEED, x * FREQUENCY1, y * FREQUENCY1, 0.0);
        double value2 = OpenSimplex.noise3_ImproveXY(SEED, x * FREQUENCY2, y * FREQUENCY2, 0.0);
        double value3 = OpenSimplex.noise3_ImproveXY(SEED, x * FREQUENCY3, y * FREQUENCY3, 0.0);

        return ((value1 + value2 + value3 + value3) / 4);
    }

    private static double difNoise(int x, int y) {
        double value = OpenSimplex.noise3_ImproveXY(SEED/2, x * FREQUENCY4, y * FREQUENCY4, 0.0);
        value += OpenSimplex.noise3_ImproveXY(SEED/2, x * FREQUENCY5, y * FREQUENCY5, 0.0);

        return value;
    }

    private static int terrain(Double valuesCombined, double difValue, int x, int y) {

        if (WATER_PATH <= y + 100 && WATER_PATH >= y - 100) {
            int gradiant = y - WATER_PATH;
            if (gradiant < 0) {
                gradiant = gradiant * -1;
            }
            Double gradiant100 = gradiant * 0.01;
            Double gradiant0 = 1 - gradiant100;
            if (valuesCombined * gradiant100 < 0.5 * gradiant0) {
                valuesCombined = ((valuesCombined * gradiant100) + (0.3 * gradiant0)/2);
            }
        }

        if (y < 100) {
            Double gradiant100 = y * 0.01;
            Double gradiant0 = 1 - gradiant100;
            valuesCombined = ((valuesCombined * gradiant100) + (0.01 * gradiant0)/2);
        } else if (y > HEIGHT - 100) {
            int gradiant = HEIGHT - y;
            Double gradiant100 = gradiant * 0.01;
            Double gradiant0 = 1 - gradiant100;
            valuesCombined = ((valuesCombined * gradiant100) + (0.01 * gradiant0)/2);
        }

        int terrainResult;

        if (x <= WIDTH_PART) {
            float test = ((1.f/WIDTH_PART)* x);
            double test2 = 1 - test;
            Double value = ((valuesCombined * test) + (test2 * 1/2));
            terrainResult =  TileInturpetation.terrainMapping(value, x, y, difValue);
        } else if (x >= WIDTH - WIDTH_PART) {
            float test = ((1.f/WIDTH_PART) * (WIDTH - x));
            double test2 = 1 - test;
            Double value = ((valuesCombined * test) + (test2 * 1/2)) ;
            terrainResult = TileInturpetation.terrainMapping(value, x, y, difValue);
        } else {
            terrainResult = TileInturpetation.terrainMapping(valuesCombined, x, y, difValue);
        }

        return terrainResult;
    }

    private static int terrainMapping(Double terrain, int x, int y, double difValue) {

        terrain = terrain * -1 + (difValue*0.05);

        if (terrain < -0.1) {
            return 100;
        } else if (terrain < 0) {
            if (difValue > 1.1) {
                return 300;}
            return 200;
        } else if (terrain < 0.15) {
            if (difValue > -1.4) {
                return 300;}
            return 300;
        } else if (terrain < 0.4) {
            return 400;
        } else {
            return 500;
        }
    }

    private static int temperature(int latitude, double difValue) {

        int value;
        int latitudeLine = -1;

        int waterTemp = waterTempDif();

        if (latitude < HEIGHT_PART/2) {
            latitudeLine = HEIGHT_PART/2;
            value = 10;
        } else if (latitude > (HEIGHT_PART/2)*19) {
            latitudeLine = (HEIGHT_PART/2)*19;
            value = 10;
        } else if (latitude < HEIGHT_PART*2 ) {
            latitudeLine = (HEIGHT_PART*2);
            value = 20;
        } else if (latitude > HEIGHT_PART*8) {
            latitudeLine = (HEIGHT_PART*8);
            value = 20;
        } else if (latitude < HEIGHT_PART*3.5) {
            latitudeLine = (int) (HEIGHT_PART*3.5);
            value = 30;
        } else if (latitude > HEIGHT_PART*6.5) {
            latitudeLine = (int) (HEIGHT_PART*6.5);
            value = 30; //
        } else if (latitude < HEIGHT_PART*4.5) {
            latitudeLine = (int) (HEIGHT_PART*4.5);
            value = 40;
        } else if (latitude > HEIGHT_PART*5.5) {
            latitudeLine = (int) (HEIGHT_PART*5.5);
            value = 40;
        } else {
            value = 50;
        }

        double difPos;

        if (latitudeLine == -1) {
            difPos = 100.0;
        } else if (latitude < HEIGHT/2) {
            difPos = (latitudeLine - latitude);
        } else {
            difPos = (latitude - latitudeLine);
        }

        difPos = (difPos + waterTemp) * 0.1;

        if (difValue + difPos < 0 )
            value += 10;

        return value;
    }

    private static int waterTempDif() {

        int close = closeEast + closeWest;
        int middle = middleEast + middleWest;
        int far = farEast + farWest;

        int dif = 0;

        if (closeEast + middleEast + farEast < 10000 || closeWest + middleWest + farWest < 10000) {
            dif -= 5;
        }
        if (middleEast + closeEast < 7500 || closeWest + middleWest < 7500) {
            dif -= 10;
        } else if (middleEast + closeEast < 9000 || closeWest + middleWest < 9000) {
            dif -= 5;
        }

        if (closeWest < 1500 || closeEast < 1500) {
            dif -= 10;
        } else if (closeWest < 2000 || closeEast < 2000) {
            dif -= 5;
        }

        return dif;
    }

    public static int precipitation(int x, int latitude, int terrain, double difValue) {

        if (terrain == 500) {
            return 1;
        } else if (terrain < 201) {
            return 3;
        }

        int value;

        double closeWindSide;
        double middleWindSide;
        double closeLeWindSide;
        double middleLeWindSide;


        if (latitude > HEIGHT_PART * 3.5 && latitude < HEIGHT_PART * 6.5 ||
                latitude < HEIGHT_PART * 2 && latitude > HEIGHT_PART / 2 ||
                latitude > HEIGHT_PART * 8 && latitude < HEIGHT_PART * 9.5) {
            closeWindSide = closeWest;
            middleWindSide  = middleWest;
            closeLeWindSide = closeEast;
            middleLeWindSide = middleEast;
        } else {
            closeWindSide = closeEast;
            middleWindSide  = middleEast;
            closeLeWindSide = closeWest;
            middleLeWindSide = middleWest;
        }

        double latitudeLine;

        double fadeOut = 10;

        if (latitude <= HEIGHT_PART/2) {
            latitudeLine = HEIGHT_PART*0.5;
            fadeOut = latitude - latitudeLine;
        } else if (latitude >= (HEIGHT_PART/2)*19) {
            latitudeLine = (HEIGHT_PART*0.5)*19;
            fadeOut = latitude - latitudeLine;
        } else if (latitude < HEIGHT_PART*2 ) {
            latitudeLine = (HEIGHT_PART*2);
            fadeOut = latitude - latitudeLine;
        } else if (latitude > HEIGHT_PART*8) {
            latitudeLine = (HEIGHT_PART*8);
            fadeOut = latitude - latitudeLine;
        } else if (latitude < HEIGHT_PART*3.5) {
            latitudeLine = (int) (HEIGHT_PART*3.5);
            fadeOut = latitude - latitudeLine;
        } else if (latitude > HEIGHT_PART*6.5) {
            latitudeLine = (int) (HEIGHT_PART*6.5);
            fadeOut = latitude - latitudeLine;
        } else if (latitude < HEIGHT_PART*4.5) {
            latitudeLine = (int) (HEIGHT_PART*4.5);
            fadeOut = latitude - latitudeLine;
        } else if (latitude > HEIGHT_PART*5.5) {
            latitudeLine = (int) (HEIGHT_PART*5.5);
            fadeOut = latitude - latitudeLine;
        }


        if (fadeOut < 0) {fadeOut *= -1;}

        if (fadeOut > 10) {fadeOut = 10;}

        fadeOut *= 0.05; // 0.5

        double fadeIn = 0.5 - fadeOut;

        fadeOut += 0.5;

        closeWindSide = (closeWindSide * fadeOut) + (closeLeWindSide * fadeIn);
        middleWindSide  = (middleWindSide * fadeOut) + (middleLeWindSide * fadeIn);
        closeLeWindSide = (closeWindSide * fadeIn) + (closeLeWindSide * fadeOut);
        middleLeWindSide = (middleWindSide * fadeIn) + (middleLeWindSide * fadeOut);

        if (closeWindSide < 2501) {
            value = 3;
        } else if (closeLeWindSide < 2301) {
            value = 3;
        } else if (closeWindSide > 4199) {
            value = 1;
        }   else if (closeWindSide < 3001 && closeWindSide + middleWindSide < 13001) {
            value = 3;
        }   else if (closeLeWindSide > 3500 && middleLeWindSide + closeLeWindSide >  16000) {
            value = 3;
        }  else if (closeWindSide > 2999 && closeWindSide + middleWindSide > 18999) {
            value = 1;
        } else if (closeWindSide > 2999 && closeWindSide + middleWindSide > 15000 &&
                closeLeWindSide > 2999 && closeLeWindSide + middleLeWindSide > 15000) {
            value = 1;
        }else {
            value = 2;
        }

        return value;
    }

    private static int spinSeed(int latitude) {

        while (latitude > HEIGHT - HEIGHT_PART) {
            latitude -= (HEIGHT -HEIGHT_PART*2);
        }

        if (latitude < HEIGHT_PART) {
            latitude += (HEIGHT - HEIGHT_PART*2);
        }

        return latitude;
    }

    public static int xSpin(int x) {

        if (x >= WIDTH) {
            x -= WIDTH;
        }
        return x;
    }

    public static int TEMP_PAINTER(int value) {
        switch (value) {
            case 111, 112, 113, 211, 212, 213, 311, 312, 313 -> {return 0xc2d7f2;}  // Glacier
            case 123,  133, 143, 153 -> {return 0x1433a6;}  // Deep Water
            case 223, 233, 243, 253 -> {return 0x3c5cfa;}  // Coastal Water
            case 322, 323 -> {return 0x528d9c;} // Lowland Tundra
            case 422, 423 -> {return 0x2e5059;} // Highlands Tundra
            case 321 -> {return 0x768c91;} // Cold Desert
            case 421 -> {return 0x3c474a;} // Cold Desert Hills
            case 331 -> {return 0x41b03e;}  // Temperate Lowlands Plains
            case 332 -> {return 0x2a7328;}  // Temperate Forest
            case 431 -> {return 0x4c782f;}  // Highland Hills
            case 432 -> {return 0x284019;}  // Temperate Highland Forest
            case 333 -> {return 0x0ccc66;}  // Temperate Rainforest
            case 433 -> {return 0x07783c;}  // Temperate Highland Rainforest
            case 341 -> {return 0xe84827;} // Hot Desert
            case 441 -> {return 0x9c2e17;} // Hot Desert Hills
            case 342 -> {return 0xc98806;} // Hot Steppe
            case 442 -> {return 0x825804;} // Hot Steppe hills
            case 351, 352, 343 -> {return 0xbf8e32;} // Tropical Savanna
            case 451, 452, 443 -> {return 0x6e511d;} // Tropical Savanna Hills
            case 353 -> {return 0x046126;} // Tropical Rainforest
            case 453 -> {return 0x023314;} // Tropical Rainforest Hills
            case 411, 412, 413 -> {return 0x9dc6fc;} // Glacial Heights
            case 511, 521, 531 -> {return 0xebf0f7;} // Frozen Mountains
            case 541, 551 -> {return 0x260f02;}  // Mountains
        }
        System.out.println("Error not valid value: " + value);
        return 0x010101;
    }


}
