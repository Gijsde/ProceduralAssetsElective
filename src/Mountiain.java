package src;

import java.awt.Point;
import java.io.File;


public class Mountiain {
    public static void removeOutside(boolean[][] mask) {
        
    }

    public static double findDistance(Point from, Point to) {
        return .0;
    }

    public static byte[][] fillOutline(boolean[][] mask) {

        return null;
    }


    public static void main(String[] args) {
        File path = new File("images/photo.png");
        boolean[][] mask = Outline.normalizeImage(path);
        Outline.findCenter(mask);
        /**
         * TODO: have the only values in the mask be of the mountain: remove the corners.
         * IDEA: use bfs again but dont add any black pixels to the queue
         */

        /**
         * TODO: add a way to force the lines mountains have
         * this will also create valleys.
         * IDEA: user inputs a number this will be the amount of lines the mountain will have
         * split the mountain up in x amount of parts. look for the farthest point in those areas
         * from the center to those farthest points will be the lines
         */

        /**
         * TODO: find the distance of a pixel to the closest pixel on the outline and center.
         * this distance should be used by the function that determines the height of the pixel 
         * and in this case the brightness ranging from 0 to 255
         * 
         * the lines that the mountain has should be seen as a peak but with the respective height as the max height.
         */

        /**
         * TODO: allow for multiple different kind of functions to be used to determine the height of the mountain.
         * f.e. be able to choose between a Sigmoid function or a linear function
         */

        /**
         * TODO: add perlin noise to the height of the mountain
         * this will create some roughness to the mountain meaning it wont be as smooth.
         */


    }
}
