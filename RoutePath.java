import java.util.ArrayList;
import java.util.List;

// Merepresentasikan satu jalur perjalanan utuh (terdiri dari 1 atau lebih tahap/transit)
public class RoutePath {
    private String origin;
    private List<Edge> legs;

    public RoutePath(String origin, List<Edge> legs) {
        this.origin = origin;
        this.legs = new ArrayList<>(legs);
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        if (legs.isEmpty()) return origin;
        return legs.get(legs.size() - 1).getDestination();
    }

    public List<Edge> getLegs() {
        return legs;
    }

    public int getLegCount() {
        return legs.size();
    }

    // Mengecek apakah rute langsung (hanya 1 tahap perjalanan)
    public boolean isDirectRoute() {
        return legs.size() == 1;
    }

    // Mengambil daftar nama simpul/lokasi transit perantara
    public List<String> getTransitLocations() {
        List<String> transits = new ArrayList<>();
        for (int i = 0; i < legs.size() - 1; i++) {
            transits.add(legs.get(i).getDestination());
        }
        return transits;
    }

    // Menghasilkan label jenis rute (langsung vs transit dengan rincian titik transit)
    public String getRouteTypeLabel() {
        if (isDirectRoute()) {
            return "Rute Langsung";
        } else {
            List<String> transits = getTransitLocations();
            return "Rute Transit (via " + String.join(" -> ", transits) + ")";
        }
    }

    // Menghitung akumulasi total biaya dari seluruh tahapan perjalanan
    public double getTotalPrice() {
        double total = 0;
        for (Edge edge : legs) {
            total += edge.getPrice();
        }
        return total;
    }

    // Mencetak rincian rute langkah demi langkah ke layar
    public void printRoute(int routeIndex) {
        String typeLabel = getRouteTypeLabel();
        System.out.printf("%n--- Alternatif Rute #%d [%s] (%d Leg/Tahap, Total Biaya: Rp%,.2f) ---%n", 
                routeIndex, typeLabel, getLegCount(), getTotalPrice());
        
        String currentLoc = origin;
        for (int i = 0; i < legs.size(); i++) {
            Edge leg = legs.get(i);
            System.out.printf("   [Tahap %d] %s -> %s%n", i + 1, currentLoc, leg.getDestination());
            System.out.printf("            Moda    : %s (%s)%n", leg.getBrand(), leg.getType());
            if (leg.isFeeder()) {
                System.out.printf("            Layanan : Feeder / Penghubung (Operasional: %s - %s)%n", 
                        leg.getFirstTrip(), leg.getLastTrip());
                System.out.printf("            Durasi  : %s | Frekuensi: Tiap %s%n", 
                        Edge.formatHours(leg.getDurationHours()), Edge.formatHours(leg.getFrequencyHours()));
            } else {
                System.out.printf("            Jadwal  : Berangkat %s jam %s | Tiba %s jam %s%n", 
                        leg.getStartDate(), leg.getStartTime(), leg.getEndDate(), leg.getEndTime());
            }
            System.out.printf("            Harga   : %s%n", leg.getPriceFormatted());
            currentLoc = leg.getDestination();
        }
    }
}
