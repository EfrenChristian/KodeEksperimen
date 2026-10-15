import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class main {
    public static void main(String[] args) {
        String datasetPath = "PAN61-dataset.csv";
        String feederPath = "feeder_routes.csv";
        String nodesPath = "nodes.csv";

        Graph transportGraph = new Graph();
        // Membaca peran tiap lokasi (titik start, end, atau transit)
        loadNodesCSV(transportGraph, nodesPath);

        // Membaca rute transportasi utama terjadwal (pesawat, kereta, bus, shuttle)
        loadScheduledRoutesCSV(transportGraph, datasetPath);

        // Membaca rute penghubung / feeder (shuttle stasiun-bandara, taksi)
        loadFeederRoutesCSV(transportGraph, feederPath);

        // Menjalankan antarmuka terminal untuk memilih asal dan tujuan
        startInteractiveMenu(transportGraph);
    }

    // Membaca metadata simpul dari nodes.csv
    private static void loadNodesCSV(Graph graph, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine(); // Lewati baris header
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;
                String[] parts = line.split(",", -1);
                String id = parts[0].trim();
                String flag = parts.length > 1 ? parts[1].trim() : "";
                graph.setNodeFlag(id, flag);
            }
        } catch (IOException e) {
            System.err.println("Gagal membaca " + filePath + ": " + e.getMessage());
        }
    }

    // Membaca data jadwal rute transportasi dari PAN61-dataset.csv
    private static void loadScheduledRoutesCSV(Graph graph, String filePath) {
        File file = new File(filePath);
        // Regex agar pemisahan kolom CSV tidak memecah tanda koma di dalam tanda petik
        String csvSplitRegex = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String headerLine = br.readLine();
            if (headerLine == null)
                return;

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;

                String[] row = line.split(csvSplitRegex, -1);
                if (row.length < 11)
                    continue;

                String type = row[0].trim();
                String brand = row[1].trim();
                String startLocation = row[2].trim();
                String startDate = row[3].trim();
                String startTime = row[4].trim();
                String endLocation = row[6].trim();
                String endDate = row[7].trim();
                String endTime = row[8].trim();

                // Bersihkan format string mata uang dan konversi ke angka numerik
                String priceFormatted = row[10].trim().replaceAll("^\"|\"$", "").trim();
                double priceValue = 0.0;
                try {
                    String cleanNumeric = priceFormatted.replaceAll("[^0-9.]", "");
                    if (!cleanNumeric.isEmpty()) {
                        priceValue = Double.parseDouble(cleanNumeric);
                    }
                } catch (NumberFormatException e) {
                    priceValue = 0.0;
                }

                graph.addEdge(startLocation, endLocation, type, brand,
                        startDate, startTime, endDate, endTime,
                        priceValue, priceFormatted);
            }
        } catch (IOException e) {
            System.err.println("Gagal membaca " + filePath + ": " + e.getMessage());
        }
    }

    // Membaca rute feeder berbasis frekuensi dan jam operasional dari
    // feeder_routes.csv
    private static void loadFeederRoutesCSV(Graph graph, String filePath) {
        File file = new File(filePath);
        String csvSplitRegex = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String headerLine = br.readLine();
            if (headerLine == null)
                return;

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;

                String[] row = line.split(csvSplitRegex, -1);
                if (row.length < 9)
                    continue;

                String type = row[0].trim();
                String brand = row[1].trim();
                String startLocation = row[2].trim();
                String endLocation = row[3].trim();
                String firstTrip = row[4].trim();
                String lastTrip = row[5].trim();

                double durationHours = 0.0;
                double frequencyHours = 0.0;
                try {
                    durationHours = Double.parseDouble(row[6].trim());
                    frequencyHours = Double.parseDouble(row[7].trim());
                } catch (NumberFormatException ignored) {
                }

                String priceFormatted = row[8].trim().replaceAll("^\"|\"$", "").trim();
                double priceValue = 0.0;
                try {
                    String cleanNumeric = priceFormatted.replaceAll("[^0-9.]", "");
                    if (!cleanNumeric.isEmpty()) {
                        priceValue = Double.parseDouble(cleanNumeric);
                    }
                } catch (NumberFormatException ignored) {
                }

                graph.addFeederEdge(startLocation, endLocation, type, brand,
                        firstTrip, lastTrip, durationHours, frequencyHours,
                        priceValue, priceFormatted);
            }
        } catch (IOException e) {
            System.err.println("Gagal membaca " + filePath + ": " + e.getMessage());
        }
    }

    // Antarmuka pemilihan lokasi asal dan tujuan
    private static void startInteractiveMenu(Graph transportGraph) {
        // Kelompokkan lokasi ke dalam kategori asal, tujuan, dan perantara
        List<String> startLocations = new ArrayList<>();
        List<String> endLocations = new ArrayList<>();
        List<String> transitLocations = new ArrayList<>();

        for (String loc : transportGraph.getVertices()) {
            if (transportGraph.isStartNode(loc)) {
                startLocations.add(loc);
            } else if (transportGraph.isEndNode(loc)) {
                endLocations.add(loc);
            } else {
                transitLocations.add(loc);
            }
        }

        Collections.sort(startLocations);
        Collections.sort(endLocations);
        Collections.sort(transitLocations);

        // Gabungkan seluruh lokasi ke dalam satu daftar bernomor urut
        List<String> allLocations = new ArrayList<>();
        allLocations.addAll(startLocations);
        allLocations.addAll(endLocations);
        allLocations.addAll(transitLocations);

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n=======================================================================");
            System.out.println("                 PILIH LOKASI ASAL DAN TUJUAN PERJALANAN               ");
            System.out.println("=======================================================================");
            System.out.println("Daftar Lokasi yang Tersedia:");

            int idx = 1;
            if (!startLocations.isEmpty()) {
                System.out.println("\n[ Titik Keberangkatan Awal / START ]");
                for (String loc : startLocations) {
                    System.out.printf("  [%2d] %s%n", idx++, loc);
                }
            }

            if (!endLocations.isEmpty()) {
                System.out.println("\n[ Titik Tujuan Akhir / END ]");
                for (String loc : endLocations) {
                    System.out.printf("  [%2d] %s%n", idx++, loc);
                }
            }

            if (!transitLocations.isEmpty()) {
                System.out.println("\n[ Titik Perantara / Transit ]");
                for (String loc : transitLocations) {
                    System.out.printf("  [%2d] %s%n", idx++, loc);
                }
            }

            System.out.println("-----------------------------------------------------------------------");
            System.out.println(
                    "(Tips: Anda dapat mengetik nomor urut [1-" + allLocations.size() + "] atau nama lokasi langsung)");

            String origin = getValidLocation(scanner, "Pilih/Ketik nomor atau nama Lokasi ASAL   : ", allLocations);
            if (origin == null)
                break;

            String destination = getValidLocation(scanner, "Pilih/Ketik nomor atau nama Lokasi TUJUAN : ",
                    allLocations);
            if (destination == null)
                break;

            // Hitung dan tampilkan alternatif rute perjalanan
            transportGraph.printAlternativeRoutes(origin, destination);

            System.out.print("Apakah ingin mencari rute perjalanan lain? (y/n): ");
            String answer = scanner.nextLine().trim().toLowerCase();
            if (!answer.equals("y") && !answer.equals("ya")) {
                System.out.println("\nSelesai");
                break;
            }
        }
    }

    // Validasi input pengguna agar lokasi yang dipilih valid
    private static String getValidLocation(Scanner scanner, String prompt, List<String> locations) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("Input tidak boleh kosong. Silakan coba lagi.");
                continue;
            }

            // Cek jika pengguna memilih berdasarkan nomor urut
            try {
                int index = Integer.parseInt(input);
                if (index >= 1 && index <= locations.size()) {
                    return locations.get(index - 1);
                } else {
                    System.out.printf("Nomor tidak valid. Masukkan angka antara 1 sampai %d.%n", locations.size());
                    continue;
                }
            } catch (NumberFormatException ignored) {
                // Input bukan angka, lanjut ke pengecekan nama lokasi
            }

            // Cek jika pengguna mengetik nama lokasi langsung (case-insensitive)
            for (String loc : locations) {
                if (loc.equalsIgnoreCase(input)) {
                    return loc;
                }
            }

            System.out.println("Lokasi '" + input + "' tidak ditemukan dalam daftar. Silakan coba lagi.");
        }
    }
}