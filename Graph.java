import java.util.*;

public class Graph {
    // Adjacency List: simpul asal dipetakan ke daftar rute tujuannya
    private final Map<String, List<Edge>> adjList;
    // Menyimpan metadata tiap simpul/lokasi (id, role)
    private final Map<String, Node> nodes;

    public Graph() {
        this.adjList = new HashMap<>();
        this.nodes   = new HashMap<>();
    }

    // Menyimpan atau memperbarui metadata simpul (flag dari nodes.csv)
    public void setNode(String id, String flag) {
        adjList.putIfAbsent(id, new ArrayList<>());
        nodes.put(id, new Node(id, flag));
    }

    /** @deprecated Gunakan {@link #setNode(String, String)} */
    @Deprecated
    public void setNodeFlag(String location, String flag) {
        setNode(location, flag);
    }

    // Mengambil Node berdasarkan ID; membuat Node TRANSIT baru jika belum ada
    public Node getNode(String id) {
        return nodes.computeIfAbsent(id, k -> new Node(k, ""));
    }

    public boolean isStartNode(String location) {
        return getNode(location).isStart();
    }

    public boolean isEndNode(String location) {
        return getNode(location).isEnd();
    }

    // Memastikan simpul terdaftar dalam adjList dan nodes
    private void ensureNode(String id) {
        adjList.putIfAbsent(id, new ArrayList<>());
        nodes.computeIfAbsent(id, k -> new Node(k, ""));
    }

    // Menambahkan rute terjadwal (pesawat, kereta, bus, shuttle)
    public void addEdge(String source, String destination, String type, String brand,
            String startDate, String startTime, int startOffsetHours,
            String endDate, String endTime, int endOffsetHours,
            double price, String priceFormatted) {
        ensureNode(source);
        ensureNode(destination);

        Edge edge = new Edge(destination, type, brand,
                startDate, startTime, startOffsetHours,
                endDate, endTime, endOffsetHours,
                price, priceFormatted);
        adjList.get(source).add(edge);
    }

    // Menambahkan rute feeder / penghubung (shuttle, taksi)
    public void addFeederEdge(String source, String destination, String type, String brand,
            String firstTrip, String lastTrip, double durationHours,
            double frequencyHours, double price, String priceFormatted) {
        ensureNode(source);
        ensureNode(destination);

        Edge edge = new Edge(destination, type, brand,
                firstTrip, lastTrip, durationHours, frequencyHours,
                price, priceFormatted);
        adjList.get(source).add(edge);
    }

    // Mendapatkan seluruh daftar simpul/lokasi dalam graf
    public Set<String> getVertices() {
        return adjList.keySet();
    }

    // Menelusuri seluruh kemungkinan rute (langsung dan transit) menggunakan DFS
    public List<RoutePath> findAlternativeRoutes(String origin, String destination) {
        List<RoutePath> allRoutes = new ArrayList<>();
        if (!adjList.containsKey(origin) || !adjList.containsKey(destination)) {
            return allRoutes;
        }

        Set<String> visited    = new HashSet<>();
        List<Edge>  currentPath = new ArrayList<>();

        visited.add(origin);
        dfsRoutes(origin, origin, destination, visited, currentPath, allRoutes);

        // Urutkan rute hasil pencarian berdasarkan total biaya terendah
        allRoutes.sort(Comparator.comparingDouble(RoutePath::getTotalPrice));

        return allRoutes;
    }

    // Fungsi rekursif penelusuran jalur graf dengan backtracking
    private void dfsRoutes(String origin, String current, String target,
            Set<String> visited, List<Edge> currentPath, List<RoutePath> allRoutes) {
        if (current.equalsIgnoreCase(target)) {
            allRoutes.add(new RoutePath(origin, currentPath));
            return;
        }

        List<Edge> neighbors = adjList.getOrDefault(current, new ArrayList<>());
        for (Edge edge : neighbors) {
            String nextNode = edge.getDestination();
            if (!visited.contains(nextNode)) {
                visited.add(nextNode);
                currentPath.add(edge);

                dfsRoutes(origin, nextNode, target, visited, currentPath, allRoutes);

                // Backtracking: kembalikan status simpul untuk mencari cabang lain
                currentPath.remove(currentPath.size() - 1);
                visited.remove(nextNode);
            }
        }
    }

    // Menampilkan hasil pencarian rute alternatif ke konsol
    public void printAlternativeRoutes(String origin, String destination) {
        System.out.println("\n=======================================================================");
        System.out.printf("   PENCARIAN ALTERNATIF RUTE PERJALANAN: %s -> %s%n", origin, destination);
        System.out.println("=======================================================================");

        List<RoutePath> routes = findAlternativeRoutes(origin, destination);

        if (routes.isEmpty()) {
            System.out.printf("Tidak ditemukan rute perjalanan yang menghubungkan '%s' ke '%s'.%n",
                    origin, destination);
        } else {
            long directCount  = routes.stream().filter(RoutePath::isDirectRoute).count();
            long transitCount = routes.size() - directCount;

            System.out.printf("Ditemukan %d alternatif rute perjalanan (Rute Langsung: %d, Rute Transit: %d):%n",
                    routes.size(), directCount, transitCount);
            System.out.println("Semua alternatif diurutkan dari total biaya termurah:");

            for (int i = 0; i < routes.size(); i++) {
                routes.get(i).printRoute(i + 1);
            }
        }
        System.out.println("=======================================================================\n");
    }
}
