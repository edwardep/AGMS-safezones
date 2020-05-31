package safezones;

import java.util.Arrays;
import static utils.SketchOperators.multiply;

public class SafezoneQuorum {

    private int n;              // the number of inputs
    private int k;              // the lower bound on true inputs
    private int[] L;            // the legal inputs index from n to zetaE
    private Double[] zetaE;     // the reference vector's zetas, where zE >= 0
    private Double[] zCached;   // caching coefficients for faster execution
    private boolean eikonal = true;

    public SafezoneQuorum() {}
    public SafezoneQuorum(Double[] zE, int k, boolean eik) {
        prepare(zE, k);
        setEikonal(eik);
    }

    public void setEikonal(boolean eikonal) { this.eikonal = eikonal; }

    public void prepare(Double[] zE, int k) {
        this.n = zE.length;
        this.k = k;

        // count legal inputs
        int[] Legal = new int[n];
        int pos = 0;
        for(int i=0; i < n; i++)
            if (zE[i] > 0) Legal[pos++] = i;

        // create the index and the other matrices
        L = Arrays.copyOfRange(Legal,0, pos);
        zetaE = new Double[pos];

        int iter = 0;
        for(int index : L) zetaE[iter++] = zE[index];


        assert 1 <= k && k <= n;
        if(L.length < k)
            throw new IllegalArgumentException("The reference vector is non-admissible:"+ Arrays.toString(zE));
    }

    private double find_min(int m, int l, int b, Double[] zEzX, Double[] zE2, double SzEzX, double SzE2) {
        if(m==0) return SzEzX/Math.sqrt(SzE2);

        double zinf = find_min(m-1, l, b+1, zEzX, zE2, SzEzX + zEzX[b], SzE2 + zE2[b]);

        int c = l-m+1;
        for(int i = b+1; i < c; i++){
            double zi = find_min(m-1, l, i+1, zEzX, zE2, SzEzX + zEzX[i], SzE2 + zE2[i]);
            zinf = Math.min(zinf, zi);
        }
        return zinf;
    }

    private double zetaEikonal(Double[] zX) {
        // pre-compute zeta_i(E)*zeta_i(X) for all i in L
        Double[] zEzX = zetaE.clone();

        Double[] zXL = new Double[L.length];
        int iter = 0;
        for(int index : L) zXL[iter++] = zX[index];

        zEzX = multiply(zEzX, zXL);

        if(zCached == null)
            prepareZCache();

        int l = L.length;
        int m = l - k + 1;

        return find_min(m, l, 0, zEzX, zCached, 0d,0d);
    }

    private double zetaNonEikonal(Double[] zX) {
        int iter = 0;
        Double[] zEzX = zetaE.clone();
        Double[] zXL = new Double[L.length];
        for(int index : L) zXL[iter++] = zX[index];
        zEzX = multiply(zEzX, zXL);

        Arrays.sort(zEzX);
        double res = 0d;
        for(int i=0; i < L.length-k+1; i++)
            res += zEzX[i];

        return res;
    }

    public void prepareZCache() {
        this.zCached = new Double[zetaE.length];
        this.zCached = Arrays.copyOf(multiply(zetaE, zetaE), zetaE.length);
    }

    public double median(Double[] zX) {
        return eikonal ? zetaEikonal(zX) : zetaNonEikonal(zX);
    }

    public int getN() {
        return n;
    }

    public int getK() {
        return k;
    }

    public int[] getL() {
        return L;
    }

    public Double[] getZetaE() {
        return zetaE;
    }

    public Double[] getzCached() {
        return zCached;
    }
}
