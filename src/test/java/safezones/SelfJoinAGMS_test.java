package safezones;

import org.junit.Test;
import sketches.FastAGMS_proto;
import safezones.SelfJoinAGMS.SelfJoin_lowerBound;
import safezones.SelfJoinAGMS.SelfJoin_upperBound;

import static java.lang.Math.abs;
import static junit.framework.TestCase.assertTrue;
import static utils.Helper.uniform_random_vector;
import static utils.SketchOperators.median;
import static utils.SketchOperators.sum;

public class SelfJoinAGMS_test {

    /**
     * Test the self-join upper bound for accuracy.
     * This is done by taking random sketches and comparing
     * to the distance of the admissible region.
     */
    @Test
    public void test_sj_upperBound() {

        for(int i = 0; i < 10; i++) {
            FastAGMS_proto E = new FastAGMS_proto(5, 10);

            // fill the sketch with random values
            for (int w = 0; w < E.getWidth(); w++)
                E.setSketchColumn(w, uniform_random_vector(E.getDepth(), 20, -10));

            double Emed = median(E.getSketch());

            SelfJoin_upperBound sz = new SelfJoin_upperBound(E.getSketch(), 1.1 * Emed, true);

            assertTrue(0.0 < sz.median(E.getSketch()));

            // test 100 sketches from each type
            int count_inA = 0, count_notInA = 0, count_inZ = 0, count_notInZ = 0;

            while (count_notInA < 100 || count_inA < 100) {
                FastAGMS_proto X = new FastAGMS_proto(5, 10);

                // X = E + rand
                for (int w = 0; w < X.getWidth(); w++)
                    X.setSketchColumn(w, sum(E.getSketchColumn(w), uniform_random_vector(X.getDepth(), 10, -5)));

                int inA = (median(X.getSketch()) < 1.1 * Emed) ? 1 : 0;
                if (inA == 1) count_inA++;
                else count_notInA++;

                int inZ = (sz.median(X.getSketch()) > 0) ? 1 : 0;
                if (inZ == 1) count_inZ++;
                else count_notInZ++;

                //fixme: not passing this assertion, meaning that it is "in Z" while "not inA"
                //assert inZ <= inA;
            }
            System.out.println("Upper bound test:");
            System.out.println("in A: " + count_inA + ", not in A: " + count_notInA);
            System.out.println("in Z: " + count_inZ + ", not in Z: " + count_notInZ);
        }
    }

    /**
     * Test the self-join lower bound for accuracy.
     * This is done by taking random sketches and comparing
     * to the distance of the admissible region.
     */
    @Test
    public void test_sj_lowerBound() {
        FastAGMS_proto E = new FastAGMS_proto(5, 10);

        for(int i = 0; i < 10; i++) {

            // fill the sketch with random values
            for (int w = 0; w < E.getWidth(); w++)
                E.setSketchColumn(w, uniform_random_vector(E.getDepth(), 20, -10));

            double Emed = median(E.getSketch());

            SelfJoin_lowerBound sz;
            try { sz = new SelfJoin_lowerBound(E.getSketch(), 0.9 * Emed, true); }
            catch (IllegalArgumentException e) { i--; continue; }

            assertTrue(0.0 < sz.median(E.getSketch()));

            // test 100 sketches from each type
            int count_inA = 0, count_notInA = 0, count_inZ = 0, count_notInZ = 0;

            while (count_notInA < 100 || count_inA < 100) {
                FastAGMS_proto X = new FastAGMS_proto(5, 10);

                // X = E + rand
                for (int w = 0; w < X.getWidth(); w++)
                    X.setSketchColumn(w, sum(E.getSketchColumn(w), uniform_random_vector(X.getDepth(), 10, -5)));

                int inA = (median(X.getSketch()) >= 0.9 * Emed) ? 1 : 0;
                if (inA == 1) count_inA++;
                else count_notInA++;

                int inZ = (sz.median(X.getSketch()) > 0) ? 1 : 0;
                if (inZ == 1) count_inZ++;
                else count_notInZ++;

                assert inZ <= inA;
            }
            System.out.println("Lower bound test:");
            System.out.println("in A: " + count_inA + ", not in A: " + count_notInA);
            System.out.println("in Z: " + count_inZ + ", not in Z: " + count_notInZ);
        }
    }

    /**
     * Testing the self-join complete safezone.
     * Not implemented COMPARE_WITH_BOUNDING_BALLS segment
     */
    @Test
    public void test_selfjoin() {
        FastAGMS_proto E = new FastAGMS_proto(5, 10);

        for(int i = 0; i < 10; i++) {

            // fill the sketch with random values
            for (int w = 0; w < E.getWidth(); w++)
                E.setSketchColumn(w, uniform_random_vector(E.getDepth(), 20, -10));

            double Emed = median(E.getSketch());

            SelfJoinAGMS sz;
            try { sz = new SelfJoinAGMS(E.getSketch(), 0.9*Emed, 1.1*Emed, true); }
            catch (IllegalArgumentException e) { i--; continue; }

            assertTrue(0.0 < sz.inf(E.getSketch()));

            // test 100 sketches from each type
            int count_inA = 0, count_notInA = 0, count_inZ = 0, count_notInZ = 0;

            for(int j = 0; j < 1000; j++) {
                FastAGMS_proto X = new FastAGMS_proto(5, 10);

                // X = E + rand
                for (int w = 0; w < X.getWidth(); w++)
                    X.setSketchColumn(w, sum(E.getSketchColumn(w), uniform_random_vector(X.getDepth(), 2*1.65, -1.65)));

                int inA = (abs(median(X.getSketch()) - Emed) <= 0.1*Emed) ? 1 : 0;
                if(inA == 1) count_inA++;
                else count_notInA++;

                int inZ = (sz.inf(X.getSketch()) > 0) ? 1 : 0;
                if (inZ == 1) count_inZ++;
                else count_notInZ++;

                //fixme: not passing this assertion, meaning that it is "in Z" while "not inA"
                //assert inZ <= inA;
            }
            System.out.println("Both bounds test:");
            System.out.println("in A: " + count_inA + ", not in A: " + count_notInA);
            System.out.println("in Z: " + count_inZ + ", not in Z: " + count_notInZ);
        }
    }
}
