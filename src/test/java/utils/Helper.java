package utils;

import java.util.Random;

public class Helper {

    public static Double[] uniform_random_vector(int n, double scale, double shift) {
        Random rand = new Random();
        Double[] res = new Double[n];
        for(int j=0; j < n; j++)
            res[j] =  (scale*rand.nextDouble())+shift;
        return res;
    }
}
