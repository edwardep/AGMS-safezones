package sketches;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;

public class FastAGMS_proto {

    private int depth;
    private int width;

    private Long seedVector[][];
    private Double sketchVector[][];

    private UniformRandomProvider rng;

    public FastAGMS_proto(int depth, int width){
        this.depth = depth;
        this.width = width;
        seedVector = new Long[6][depth];
        sketchVector = new Double[depth][width];
        rng = RandomSource.create(RandomSource.MT);
        initSeedVector();
    }

    private void initSeedVector() {
        for(int i = 0; i < 6; i++){
            for(int j = 0; j < depth; j++){
                long rand = rng.nextLong();
                seedVector[i][j] = (rand < 0) ? -rand : rand;
            }
        }
    }

    public long hash31(long a, long b, long x) {
        long result = (a * x) + b;
        return ((result >> 31) ^ result) & 2147483647;
    }

    public int hash(int d, long x) {
        assert d < depth;
        return (int) hash31(seedVector[0][d], seedVector[1][d], x) % width;
    }


    public long fourwise(long s1, long s2, long s3, long s4, long x) {
        return hash31(hash31(hash31(x, s1, s2), x, s3), x, s4) & (1 << 15);
    }

    // return a 4-wise independent number {-1,+1}
    public int fourwise(int d, long x) {
        return (fourwise(seedVector[2][d], seedVector[3][d], seedVector[4][d], seedVector[5][d], x) > 0) ? 1 : -1;
    }

    public void update(long key, double value) {
        for(int d = 0; d < depth; d++){
            int hash = hash(d, key);
            int xi = fourwise(d, key);

            sketchVector[d][hash] += value*xi;
        }
    }

    public int sumSquares(int d) {
        int sum = 0;
        for(int i = 0; i < width; i++)
            sum += sketchVector[d][i]*sketchVector[d][i];
        return sum;
    }

    public Double getSketchElement(int i, int j) {
        return sketchVector[i][j];
    }

    public int getDepth() {
        return depth;
    }
    public int getWidth() {
        return width;
    }
    public int getSize() {
        return depth*width;
    }

    public Long[][] getSeedVector() {
        return seedVector;
    }

    public Double[][] getSketch() {
        return sketchVector;
    }

    public Double[] getSketchColumn(int w) {
        Double[] ret = new Double[depth];
        for(int d = 0; d < depth; d++)
            ret[d] = sketchVector[d][w];
        return ret;
    }

    public void setSketchColumn(int w, Double[] column) {
        for(int d = 0; d < depth; d++)
            this.sketchVector[d][w] = column[d];
    }

    public String toString() {
        String res = "";
        int d;
        for(d = 0; d < depth; d++) {
            res += "[";
            for (int w = 0; w < width; w++)
                res += sketchVector[d][w] + ((w < width-1)?",":"");
            res += "]\n";
        }
        return res;
    }

}
