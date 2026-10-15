public class Edge {
    private String destination;
    private String type;
    private String brand;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
    private double price;
    private String priceFormatted;

    // Atribut khusus rute feeder / penghubung
    private boolean isFeeder;
    private String firstTrip;
    private String lastTrip;
    private double durationHours;
    private double frequencyHours;

    // Konstruktor untuk rute reguler terjadwal (misal: pesawat, kereta, bus)
    public Edge(String destination, String type, String brand, String startDate, String startTime, 
                String endDate, String endTime, double price, String priceFormatted) {
        this.destination = destination;
        this.type = type;
        this.brand = brand;
        this.startDate = startDate;
        this.startTime = startTime;
        this.endDate = endDate;
        this.endTime = endTime;
        this.price = price;
        this.priceFormatted = priceFormatted;
        this.isFeeder = false;
        this.firstTrip = "";
        this.lastTrip = "";
        this.durationHours = 0;
        this.frequencyHours = 0;
    }

    // Konstruktor untuk rute feeder berbasis frekuensi berkala dan jam operasional
    public Edge(String destination, String type, String brand, String firstTrip, String lastTrip,
                double durationHours, double frequencyHours, double price, String priceFormatted) {
        this.destination = destination;
        this.type = type;
        this.brand = brand;
        this.startDate = "Setiap Hari";
        this.startTime = firstTrip + " - " + lastTrip;
        this.endDate = "-";
        this.endTime = "Durasi " + formatHours(durationHours);
        this.price = price;
        this.priceFormatted = priceFormatted;
        this.isFeeder = true;
        this.firstTrip = firstTrip;
        this.lastTrip = lastTrip;
        this.durationHours = durationHours;
        this.frequencyHours = frequencyHours;
    }

    // Konversi nilai jam desimal (contoh: 0.5 atau 1.5) menjadi teks yang mudah dibaca
    public static String formatHours(double hours) {
        if (hours == 0.5) return "30 menit";
        if (hours == 1.0) return "1 jam";
        if (hours == (long) hours) return String.format("%d jam", (long) hours);
        long wholeHours = (long) hours;
        long minutes = Math.round((hours - wholeHours) * 60);
        if (wholeHours == 0) return String.format("%d menit", minutes);
        return String.format("%d jam %d menit", wholeHours, minutes);
    }

    public String getDestination() {
        return destination;
    }

    public String getType() {
        return type;
    }

    public String getBrand() {
        return brand;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getEndTime() {
        return endTime;
    }

    public double getPrice() {
        return price;
    }

    public String getPriceFormatted() {
        return priceFormatted;
    }

    public boolean isFeeder() {
        return isFeeder;
    }

    public String getFirstTrip() {
        return firstTrip;
    }

    public String getLastTrip() {
        return lastTrip;
    }

    public double getDurationHours() {
        return durationHours;
    }

    public double getFrequencyHours() {
        return frequencyHours;
    }

    @Override
    public String toString() {
        if (isFeeder) {
            return String.format("-> [%s] (Tipe: %s, Moda: %s, Operasional: %s-%s, Durasi: %s, Frekuensi: tiap %s, Biaya: %s)",
                    destination, type, brand, firstTrip, lastTrip, 
                    formatHours(durationHours), formatHours(frequencyHours), priceFormatted);
        }
        return String.format("-> [%s] (Tipe: %s, Moda: %s, Berangkat: %s %s, Tiba: %s %s, Harga: %s)",
                destination, type, brand, startDate, startTime, endDate, endTime, priceFormatted);
    }
}
