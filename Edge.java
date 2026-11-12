import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Edge {
    private final String destination;
    private final String type;
    private final String brand;
    private final double price;
    private final String priceFormatted;

    // Rute terjadwal: waktu berangkat dan tiba dengan zona waktu masing-masing
    private final ZonedDateTime departureTime;
    private final ZonedDateTime arrivalTime;

    // Atribut khusus rute feeder / penghubung
    private final boolean isFeeder;
    private final String firstTrip;
    private final String lastTrip;
    private final double durationHours;
    private final double frequencyHours;

    // Format tanggal + jam yang dipakai dalam CSV (contoh: "29-May 9:46")
    private static final DateTimeFormatter CSV_FORMATTER =
            DateTimeFormatter.ofPattern("d-MMM H:mm");

    // Format untuk output ke layar: "29/05 09:46 WIB"
    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM HH:mm");

    // Konstruktor untuk rute reguler terjadwal (pesawat, kereta, bus, shuttle)
    public Edge(String destination, String type, String brand,
                String startDate, String startTime, int startOffsetHours,
                String endDate, String endTime, int endOffsetHours,
                double price, String priceFormatted) {
        this.destination    = destination;
        this.type           = type;
        this.brand          = brand;
        this.price          = price;
        this.priceFormatted = priceFormatted;
        this.isFeeder       = false;
        this.firstTrip      = "";
        this.lastTrip       = "";
        this.durationHours  = 0;
        this.frequencyHours = 0;

        this.departureTime = parseZonedDateTime(startDate, startTime, startOffsetHours);
        this.arrivalTime   = parseZonedDateTime(endDate,   endTime,   endOffsetHours);
    }

    // Konstruktor untuk rute feeder berbasis frekuensi berkala dan jam operasional
    public Edge(String destination, String type, String brand,
                String firstTrip, String lastTrip,
                double durationHours, double frequencyHours,
                double price, String priceFormatted) {
        this.destination    = destination;
        this.type           = type;
        this.brand          = brand;
        this.price          = price;
        this.priceFormatted = priceFormatted;
        this.isFeeder       = true;
        this.firstTrip      = firstTrip;
        this.lastTrip       = lastTrip;
        this.durationHours  = durationHours;
        this.frequencyHours = frequencyHours;

        // Feeder tidak memiliki jadwal pasti; waktu = null
        this.departureTime = null;
        this.arrivalTime   = null;
    }

    /**
     * Mengurai tanggal + waktu + offset UTC menjadi ZonedDateTime.
     *
     * @param date        string tanggal dari CSV, contoh: "29-May"
     * @param time        string waktu dari CSV, contoh: "9:46"
     * @param offsetHours offset UTC dalam jam (7 = WIB, 8 = WITA, 9 = WIT)
     * @return ZonedDateTime, atau null jika format tidak valid
     */
    private static ZonedDateTime parseZonedDateTime(String date, String time, int offsetHours) {
        if (date == null || time == null) return null;
        String raw = date.trim() + " " + time.trim();
        try {
            LocalDateTime ldt = LocalDateTime.parse(raw, CSV_FORMATTER);
            return ldt.atOffset(ZoneOffset.ofHours(offsetHours)).toZonedDateTime();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Memformat ZonedDateTime untuk tampilan di konsol.
     * Menampilkan waktu sesuai zona aslinya beserta label zona (WIB / WITA / WIT).
     * Contoh output: "29/05 09:46 WIB"
     */
    public static String formatDisplay(ZonedDateTime zdt) {
        if (zdt == null) return "-";
        String label = offsetToZoneLabel(zdt.getOffset().getTotalSeconds() / 3600);
        return zdt.format(DISPLAY_FORMATTER) + " " + label;
    }

    // Konversi offset jam ke label zona waktu Indonesia
    private static String offsetToZoneLabel(int offsetHours) {
        return switch (offsetHours) {
            case 7  -> "WIB";
            case 8  -> "WITA";
            case 9  -> "WIT";
            default -> "UTC" + (offsetHours >= 0 ? "+" : "") + offsetHours;
        };
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

    // ── Getter ──────────────────────────────────────────────────────────────

    public String getDestination() {
        return destination;
    }

    public String getType() {
        return type;
    }

    public String getBrand() {
        return brand;
    }

    public double getPrice() {
        return price;
    }

    public String getPriceFormatted() {
        return priceFormatted;
    }

    /** Waktu keberangkatan beserta zona aslinya (null untuk rute feeder) */
    public ZonedDateTime getDepartureTime() {
        return departureTime;
    }

    /** Waktu kedatangan beserta zona aslinya (null untuk rute feeder) */
    public ZonedDateTime getArrivalTime() {
        return arrivalTime;
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
        return String.format("-> [%s] (Tipe: %s, Moda: %s, Berangkat: %s, Tiba: %s, Harga: %s)",
                destination, type, brand,
                formatDisplay(departureTime), formatDisplay(arrivalTime),
                priceFormatted);
    }
}
