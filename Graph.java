import java.util.*;

public class Graph {
    // Adjacency List: lokasi asal dipetakan ke daftar rute tujuannya
    private Map<String, List<Edge>> adjList;
    // Menyimpan kategori peran simpul (start, end, atau transit)
    private Map<String, String> nodeFlags;

    public Graph() {
        this.adjList = new HashMap<>();
        this.nodeFlags = new HashMap<>();
    }

    // Menyimpan peran simpul (misal "start" untuk asal, "end" untuk tujuan akhir)
    public void setNodeFlag(String location, String flag) {
        adjList.putIfAbsent(location, new ArrayList<>());
        nodeFlags.put(location, flag != null ? flag.trim().toLowerCase() : "");
    }

    public boolean isStartNode(String location) {
        return "start".equalsIgnoreCase(nodeFlags.get(location));
    }

    public boolean isEndNode(String location) {
        return "end".equalsIgnoreCase(nodeFlags.get(location));
    }

    // Menambahkan rute terjadwal (pesawat, kereta, bus, shuttle)
    public void addEdge(String source, String destination, String type, String brand,
            String startDate, String startTime, String endDate, String endTime,
            double price, String priceFormatted) {
        adjList.putIfAbsent(source, new ArrayList<>());
        adjList.putIfAbsent(destination, new ArrayList<>());

        Edge edge = new Edge(destination, type, brand, startDate, startTime, endDate, endTime, price, priceFormatted);
        adjList.get(source).add(edge);
    }

    // Menambahkan rute feeder / penghubung (shuttle, taksi)
    public void addFeederEdge(String source, String destination, String type, String brand,
            String firstTrip, String lastTrip, double durationHours,
            double frequencyHours, double price, String priceFormatted) {
        adjList.putIfAbsent(source, new ArrayList<>());
        adjList.putIfAbsent(destination, new ArrayList<>());

        Edge edge = new Edge(destination, type, brand, firstTrip, lastTrip, durationHours, frequencyHours, price,
                priceFormatted);
        adjList.get(source).add(edge);
    }

    // Mendapatkan seluruh daftar simpul lokasi dalam graf
    public Set<String> getVertices() {
        return adjList.keySet();
    }

    // Menelusuri seluruh kemungkinan rute (langsung dan transit) menggunakan
    // algoritma DFS
    public List<RoutePath> findAlternativeRoutes(String origin, String destination) {
        List<RoutePath> allRoutes = new ArrayList<>();
        if (!adjList.containsKey(origin) || !adjList.containsKey(destination)) {
            return allRoutes;
        }

        Set<String> visited = new HashSet<>();
        List<Edge> currentPath = new ArrayList<>();

        visited.add(origin);
        dfsRoutes(origin, origin, destination, visited, currentPath, allRoutes);

        // Urutkan rute hasil pencarian berdasarkan total biaya terendah
        allRoutes.sort(Comparator.comparingDouble(RoutePath::getTotalPrice));

        return allRoutes;
    }

    // Fungsi rekursif penelusuran jalur graf dengan backtracking
    private void dfsRoutes(String origin, String current, String target, Set<String> visited,
            List<Edge> currentPath, List<RoutePath> allRoutes) {
        // Jika telah sampai di lokasi tujuan, catat jalur yang terbentuk
        if (current.equalsIgnoreCase(target)) {
            allRoutes.add(new RoutePath(origin, currentPath));
            return;
        }

        List<Edge> neighbors = adjList.getOrDefault(current, new ArrayList<>());
        for (Edge edge : neighbors) {
            String nextNode = edge.getDestination();
            // utk menghindari looping ke lokasi yang sudah dikunjungi di jalur saat ini
            if (!visited.contains(nextNode)) {
                visited.add(nextNode);
                currentPath.add(edge);

                dfsRoutes(origin, nextNode, target, visited, currentPath, allRoutes);

                // Kembalikan status simpul (backtracking) untuk mencari cabang jalur lain
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
            System.out.printf("Tidak ditemukan rute perjalanan yang menghubungkan '%s' ke '%s'.%n", origin,
                    destination);
        } else {
            int directCount = 0;
            int transitCount = 0;
            for (RoutePath r : routes) {
                if (r.isDirectRoute())
                    directCount++;
                else
                    transitCount++;
            }

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
