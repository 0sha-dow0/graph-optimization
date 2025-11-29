import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

public class Main {

    public static void main(String[] args) throws Exception {
        long perInstanceMillis = 59_000L; // forcing to be under 60 secs

        String randomPath    = "src/random/TSP_1000_randomDistance (1).txt";
        String euclideanPath = "src/Euclidean/TSP_1000_euclidianDistance (1).txt";

        runInstance(randomPath, "Random graph", perInstanceMillis);
        runInstance(euclideanPath, "Euclidean graph", perInstanceMillis);
    }

    private static void runInstance(String filePath,
                                    String label,
                                    long timeLimitMillis) throws Exception {

        System.out.println("========== " + label + " ==========");

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

        Instant startInstant = Instant.now();
        String startStr = formatter.format(
                LocalDateTime.ofInstant(startInstant, ZoneId.systemDefault()));
        System.out.printf("Started computing %s at %s%n", label, startStr);

        TSPInstance instance = TSPInstance.loadFromFile(filePath);
        HeuristicTSPSolver solver = new HeuristicTSPSolver(instance, timeLimitMillis);

        solver.solve();

        Instant endInstant = Instant.now();
        String endStr = formatter.format(
                LocalDateTime.ofInstant(endInstant, ZoneId.systemDefault()));
        long elapsedMs = Duration.between(startInstant, endInstant).toMillis();
        double elapsedSec = elapsedMs / 1000.0;

        System.out.printf("Finished computing %s at %s%n", label, endStr);
        System.out.printf("Elapsed time for %s: %.3f seconds%n", label, elapsedSec);

        double bestCost = solver.getBestTourCost();
        long cycles = solver.getCycleEvaluations();
        int[] bestTour = solver.getBestTour();

        System.out.printf("Best tour cost: %.2f%n", bestCost);
        System.out.printf("Cycles evaluated: %s%n", toScientific(cycles));

        // printing the nodes for the best cost tour
        if (bestTour != null) {
            System.out.print("Best tour nodes: ");
            for (int i = 0; i < bestTour.length; i++) {
                System.out.print(bestTour[i]);
                if (i < bestTour.length - 1) {
                    System.out.print(", ");
                }
            }

            System.out.print(", " + bestTour[0]);
            System.out.println();
        } else {
            System.out.println("Best tour nodes: (no tour found within time limit)");
        }

        System.out.println();
    }

    private static String toScientific(long value) {
        if (value == 0L) {
            return "0e0";
        }
        int exponent = (int) Math.floor(Math.log10((double) value));
        double mantissa = value / Math.pow(10.0, exponent);
        return String.format("%.1fe%d", mantissa, exponent);
    }
}
