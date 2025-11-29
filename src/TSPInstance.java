import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TSPInstance {

    private final int nodeCount;
    private final double[][] nodeDistances;

    public TSPInstance(int nodeCount, double[][] nodeDistances) {
        this.nodeCount = nodeCount;
        this.nodeDistances = nodeDistances;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public double getDistance(int i, int j) {
        return nodeDistances[i][j];
    }


    public static TSPInstance loadFromFile(String path) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line = reader.readLine();
            if (line == null) {
                throw new IllegalArgumentException("Empty file: " + path);
            }

            int n = Integer.parseInt(line.trim());
            double[][] dist = new double[n + 1][n + 1];


            reader.readLine();

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\\s+");
                if (parts.length < 3) {
                    continue;
                }

                int i = Integer.parseInt(parts[0]);
                int j = Integer.parseInt(parts[1]);
                double d = Double.parseDouble(parts[2]);

                dist[i][j] = d;
                dist[j][i] = d;
            }

            return new TSPInstance(n, dist);
        }
    }
}
