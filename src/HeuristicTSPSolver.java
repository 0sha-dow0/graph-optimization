import java.util.Random;

public class HeuristicTSPSolver {

    private final TSPInstance instance;
    private final long timeLimitNanos;

    private long startTimeNanos;
    private long cycleEvaluations;
    private double bestTourCost = Double.POSITIVE_INFINITY;
    private int[] bestTour;

    public HeuristicTSPSolver(TSPInstance instance, long timeLimitMillis) {
        this.instance = instance;
        this.timeLimitNanos = timeLimitMillis * 1_000_000L;
    }

    public double getBestTourCost() {
        return bestTourCost;
    }

    public long getCycleEvaluations() {
        return cycleEvaluations;
    }


    public int[] getBestTour() {
        return (bestTour == null) ? null : bestTour.clone();
    }

    private boolean timeLimitExceeded() {
        return System.nanoTime() - startTimeNanos >= timeLimitNanos;
    }


    public void solve() {
        startTimeNanos = System.nanoTime();
        int nodeCount = instance.getNodeCount();
        Random random = new Random(42);

        boolean firstStart = true;

        while (!timeLimitExceeded()) {
            int startNode;
            if (firstStart) {
                startNode = 1;
                firstStart = false;
            } else {
                startNode = 1 + random.nextInt(nodeCount);
            }

            int[] tour = buildNearestNeighborTour(startNode);
            if (timeLimitExceeded()) {
                break;
            }

            double nnCost = computeTourCost(tour);
            recordTour(nnCost, tour);

            twoOptImprove(tour);

        }
    }



    private int[] buildNearestNeighborTour(int startNode) {
        int nodeCount = instance.getNodeCount();
        int[] tour = new int[nodeCount];
        boolean[] used = new boolean[nodeCount + 1];

        int currentNode = startNode;
        used[currentNode] = true;
        tour[0] = currentNode;

        for (int pos = 1; pos < nodeCount; pos++) {
            if (timeLimitExceeded()) {

                for (; pos < nodeCount; pos++) {
                    for (int v = 1; v <= nodeCount; v++) {
                        if (!used[v]) {
                            tour[pos] = v;
                            used[v] = true;
                            break;
                        }
                    }
                }
                break;
            }

            int nextNode = -1;
            double bestDistance = Double.POSITIVE_INFINITY;

            for (int v = 1; v <= nodeCount; v++) {
                if (!used[v]) {
                    double d = instance.getDistance(currentNode, v);
                    if (d < bestDistance) {
                        bestDistance = d;
                        nextNode = v;
                    }
                }
            }

            tour[pos] = nextNode;
            used[nextNode] = true;
            currentNode = nextNode;
        }

        return tour;
    }



    private double computeTourCost(int[] tour) {
        int n = tour.length;
        double sum = 0.0;
        for (int i = 0; i < n - 1; i++) {
            sum += instance.getDistance(tour[i], tour[i + 1]);
        }
        sum += instance.getDistance(tour[n - 1], tour[0]);
        return sum;
    }

    private void recordTour(double cost, int[] tour) {
        cycleEvaluations++;
        if (cost < bestTourCost) {
            bestTourCost = cost;
            bestTour = tour.clone();
        }
    }


    private void twoOptImprove(int[] tour) {
        int n = tour.length;
        double currentCost = computeTourCost(tour);
        boolean improved = true;

        while (improved && !timeLimitExceeded()) {
            improved = false;

            for (int i = 0; i < n - 1 && !timeLimitExceeded(); i++) {
                for (int j = i + 2; j < n && !timeLimitExceeded(); j++) {
                    if (i == 0 && j == n - 1) {
                        continue;
                    }

                    int a = tour[i];
                    int b = tour[i + 1];
                    int c = tour[j];
                    int d = tour[(j + 1) % n];

                    double currentEdges =
                            instance.getDistance(a, b) + instance.getDistance(c, d);
                    double swappedEdges =
                            instance.getDistance(a, c) + instance.getDistance(b, d);

                    double delta = swappedEdges - currentEdges;
                    if (delta < -1e-9) {
                        reverseSegment(tour, i + 1, j);
                        currentCost += delta;
                        recordTour(currentCost, tour);
                        improved = true;
                    }
                }
            }
        }
    }

    private void reverseSegment(int[] tour, int i, int j) {
        while (i < j) {
            int tmp = tour[i];
            tour[i] = tour[j];
            tour[j] = tmp;
            i++;
            j--;
        }
    }
}
