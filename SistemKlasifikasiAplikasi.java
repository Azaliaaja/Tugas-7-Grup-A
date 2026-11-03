import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * PJBL-2 Sesi 2
 * Sistem Klasifikasi Aplikasi Berdasarkan Kegunaan Menggunakan Operasi Himpunan
 *
 * Cara menjalankan:
 * 1. Simpan file ini dengan nama SistemKlasifikasiAplikasi.java
 * 2. Buka terminal di folder file tersebut.
 * 3. Jalankan: javac SistemKlasifikasiAplikasi.java
 * 4. Jalankan: java SistemKlasifikasiAplikasi
 *
 * Hanya menggunakan library bawaan Java (Swing/Java2D).
 */

public class SistemKlasifikasiAplikasi extends JFrame {
    
    private void uploadCustomDataset() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Pilih Berkas Dataset CSV");
    int userSelection = fileChooser.showOpenDialog(this);

    if (userSelection == JFileChooser.APPROVE_OPTION) {
        File fileToOpen = fileChooser.getSelectedFile();
        try {
            Dataset customDataset = parseDatasetCSV(fileToOpen);
            if (customDataset != null) {
                current = customDataset;
                // Tambahkan pilihan baru ke ComboBox dataset
                datasetBox.addItem(customDataset.name);
                datasetBox.setSelectedItem(customDataset.name);
                vennPanel.setDataset(current);
                calculate();
                JOptionPane.showMessageDialog(this, 
                    "Dataset '" + customDataset.name + "' berhasil diunggah!", 
                    "Berhasil", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Gagal membaca file CSV: " + ex.getMessage(), 
                "Kesalahan Format", JOptionPane.ERROR_MESSAGE);
        }
    }
}

private Dataset parseDatasetCSV(File file) throws Exception {
    List<String> apps = new ArrayList<>();
    Map<String, List<String>> rawCategories = new LinkedHashMap<>();
    
    // Inisialisasi kategori default
    rawCategories.put("Pendidikan", new ArrayList<>());
    rawCategories.put("Komunikasi", new ArrayList<>());
    rawCategories.put("Hiburan", new ArrayList<>());
    rawCategories.put("Produktivitas", new ArrayList<>());

    try (BufferedReader br = new BufferedReader(new java.io.FileReader(file))) {
        String line;
        boolean isHeader = true;
        while ((line = br.readLine()) != null) {
            if (isHeader) { 
                isHeader = false;
                if (line.toLowerCase().contains("aplikasi") || line.toLowerCase().contains("nama")) {
                    continue;
                }
            }
            String[] parts = line.split(",");
            if (parts.length >= 2) {
                String appName = parts[0].trim();
                String category = parts[1].trim();
                
                if (!apps.contains(appName)) {
                    apps.add(appName);
                }
                
                if (rawCategories.containsKey(category)) {
                    rawCategories.get(category).add(appName);
                }
            }
        }
    }
    String datasetName = "Custom - " + file.getName();
    return new Dataset(datasetName, apps, rawCategories);
}
    private final JComboBox<String> datasetBox = new JComboBox<>(
            new String[]{"Dataset 1 - Aplikasi umum", "Dataset 2 - Aplikasi belajar & kerja"});
    private final JComboBox<String> operationBox = new JComboBox<>(
            new String[]{"Union (A ∪ B)", "Intersection (A ∩ B)", "Difference (A − B)",
                    "Complement (Aᶜ)", "PIE: A ∪ B ∪ C"});
    private final JComboBox<String> setABox = new JComboBox<>(new String[]{"Pendidikan", "Komunikasi", "Hiburan", "Produktivitas"});
    private final JComboBox<String> setBBox = new JComboBox<>(new String[]{"Komunikasi", "Pendidikan", "Hiburan", "Produktivitas"});
    private final JTextArea resultArea = new JTextArea();
    private final VennPanel vennPanel = new VennPanel();
    private Dataset current;
   
    public SistemKlasifikasiAplikasi() {
        super("Sistem Klasifikasi Aplikasi - PJBL-2");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 720);
        setMinimumSize(new Dimension(850, 620));
        setLocationRelativeTo(null);
        // Di dalam konstruktor SistemKlasifikasiAplikasi(), tambahkan tombol Unggah:
        
        // Di bagian event listener:
        
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        top.add(new JLabel("Pilih dataset:"));
        top.add(datasetBox);
        JButton uploadButton = new JButton("Unggah CSV");
        top.add(uploadButton);
        uploadButton.addActionListener(e -> uploadCustomDataset());
        JButton loadButton = new JButton("Muat Dataset");
        top.add(loadButton);
        top.add(new JLabel("Himpunan A:"));
        top.add(setABox);
        top.add(new JLabel("Himpunan B:"));
        top.add(setBBox);
        top.add(new JLabel("Operasi:"));
        top.add(operationBox);
        JButton calculateButton = new JButton("Hitung");
        top.add(calculateButton);

        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setMargin(new Insets(10, 10, 10, 10));

        JPanel right = new JPanel(new BorderLayout(5, 5));
        right.add(new JLabel("Hasil operasi dan perhitungan PIE", SwingConstants.CENTER), BorderLayout.NORTH);
        right.add(new JScrollPane(resultArea), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, vennPanel, right);
        split.setResizeWeight(0.57);
        split.setDividerLocation(560);

        add(top, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);

        loadButton.addActionListener(e -> loadSelectedDataset());
        calculateButton.addActionListener(e -> calculate());
        datasetBox.addActionListener(e -> loadSelectedDataset());
        loadSelectedDataset();
    }

    private void loadSelectedDataset() {
        current = datasetBox.getSelectedIndex() == 0 ? Dataset.dataset1() : Dataset.dataset2();
        vennPanel.setDataset(current);
        calculate();
    }

    private void calculate() {
        if (current == null) return;
        String aName = (String) setABox.getSelectedItem();
        String bName = (String) setBBox.getSelectedItem();
        Set<String> a = current.categories.get(aName);
        Set<String> b = current.categories.get(bName);
        String operation = (String) operationBox.getSelectedItem();

        StringBuilder out = new StringBuilder();
        out.append("DATASET: ").append(current.name).append("\n");
        out.append("Semesta U (").append(current.universe.size()).append(" aplikasi):\n");
        out.append(current.universe).append("\n\n");
        out.append(aName).append(" (A), jumlah ").append(a.size()).append(":\n").append(a).append("\n\n");
        out.append(bName).append(" (B), jumlah ").append(b.size()).append(":\n").append(b).append("\n\n");

        Set<String> result = new LinkedHashSet<>();
        switch (operationBox.getSelectedIndex()) {
            case 0:
                result.addAll(a);
                result.addAll(b);
                out.append("UNION A ∪ B\n");
                break;
            case 1:
                result.addAll(a);
                result.retainAll(b);
                out.append("INTERSECTION A ∩ B\n");
                break;
            case 2:
                result.addAll(a);
                result.removeAll(b);
                out.append("DIFFERENCE A − B\n");
                break;
            case 3:
                result.addAll(current.universe);
                result.removeAll(a);
                out.append("COMPLEMENT Aᶜ = U − A\n");
                break;
            default:
                result.addAll(current.categories.get("Pendidikan"));
                result.addAll(current.categories.get("Komunikasi"));
                result.addAll(current.categories.get("Hiburan"));
                out.append("PIE untuk A = Pendidikan, B = Komunikasi, C = Hiburan\n");
                appendPIE(out, current);
                out.append("\n");
                break;
        }
        out.append("Hasil (").append(result.size()).append(" aplikasi):\n").append(result).append("\n\n");
        out.append("CATATAN: Diagram Venn menampilkan tiga kategori Pendidikan, Komunikasi, dan Hiburan.");
        resultArea.setText(out.toString());
        resultArea.setCaretPosition(0);
        vennPanel.repaint();
    }

    private static void appendPIE(StringBuilder out, Dataset d) {
        Set<String> a = d.categories.get("Pendidikan");
        Set<String> b = d.categories.get("Komunikasi");
        Set<String> c = d.categories.get("Hiburan");
        int ab = intersection(a, b).size();
        int ac = intersection(a, c).size();
        int bc = intersection(b, c).size();
        int abc = intersection(intersection(a, b), c).size();
        int unionCount = a.size() + b.size() + c.size() - ab - ac - bc + abc;
        int none = d.universe.size() - unionCount;
        out.append("|A ∪ B ∪ C| = |A| + |B| + |C| − |A∩B| − |A∩C| − |B∩C| + |A∩B∩C|\n");
        out.append("= ").append(a.size()).append(" + ").append(b.size()).append(" + ").append(c.size())
                .append(" − ").append(ab).append(" − ").append(ac).append(" − ").append(bc)
                .append(" + ").append(abc).append(" = ").append(unionCount).append("\n");
        out.append("Jumlah aplikasi pada minimal satu dari 3 kategori: ").append(unionCount).append("\n");
        out.append("Jumlah aplikasi di luar ketiga kategori: ").append(d.universe.size())
                .append(" − ").append(unionCount).append(" = ").append(none).append("\n");
        out.append("Irisan tiga kategori: ").append(abc).append(" aplikasi\n");
    }

    private static Set<String> intersection(Set<String> x, Set<String> y) {
        Set<String> result = new LinkedHashSet<>(x);
        result.retainAll(y);
        return result;
    }

    private static Set<String> intersection(Set<String> x, Set<String> y, Set<String> z) {
        Set<String> result = intersection(x, y);
        result.retainAll(z);
        return result;
    }

    private static class Dataset {
        String name;
        Set<String> universe;
        Map<String, Set<String>> categories;

        Dataset(String name, List<String> apps, Map<String, List<String>> rawCategories) {
            this.name = name;
            this.universe = new LinkedHashSet<>(apps);
            this.categories = new LinkedHashMap<>();
            for (Map.Entry<String, List<String>> entry : rawCategories.entrySet()) {
                Set<String> set = new LinkedHashSet<>(entry.getValue());
                set.retainAll(universe);
                categories.put(entry.getKey(), set);
            }
        }

        static Dataset dataset1() {
            List<String> apps = Arrays.asList(
                    "Google Classroom", "WhatsApp", "YouTube", "Microsoft Word",
                    "Zoom", "Discord", "Canva", "Google Drive", "Spotify", "Google Meet",
                    "Telegram", "Khan Academy", "Netflix", "Notion", "Slack"
            );
            Map<String, List<String>> cats = new LinkedHashMap<>();
            cats.put("Pendidikan", Arrays.asList("Google Classroom", "Zoom", "Canva", "Google Meet", "Khan Academy"));
            cats.put("Komunikasi", Arrays.asList("WhatsApp", "Zoom", "Discord", "Google Meet", "Telegram", "Slack"));
            cats.put("Hiburan", Arrays.asList("YouTube", "Discord", "Canva", "Spotify", "Netflix"));
            cats.put("Produktivitas", Arrays.asList("Microsoft Word", "Canva", "Google Drive", "Notion", "Slack"));
            return new Dataset("Dataset 1 - Aplikasi umum", apps, cats);
        }

        static Dataset dataset2() {
            List<String> apps = Arrays.asList(
                    "Moodle", "Google Meet", "Microsoft Teams", "Zoom", "Duolingo",
                    "Coursera", "YouTube", "Trello", "Notion", "GitHub",
                    "Slack", "Google Drive", "Kahoot", "Discord", "Figma",
                    "LibreOffice", "Spotify", "Telegram"
            );
            Map<String, List<String>> cats = new LinkedHashMap<>();
            cats.put("Pendidikan", Arrays.asList("Moodle", "Google Meet", "Zoom", "Duolingo", "Coursera", "YouTube", "Kahoot"));
            cats.put("Komunikasi", Arrays.asList("Google Meet", "Microsoft Teams", "Zoom", "Slack", "Discord", "Telegram"));
            cats.put("Hiburan", Arrays.asList("YouTube", "Kahoot", "Spotify", "Discord"));
            cats.put("Produktivitas", Arrays.asList("Trello", "Notion", "GitHub", "Slack", "Google Drive", "Figma", "LibreOffice"));
            return new Dataset("Dataset 2 - Aplikasi belajar & kerja", apps, cats);
        }
    }

    private static class VennPanel extends JPanel {
        private Dataset dataset;

        VennPanel() {
            setPreferredSize(new Dimension(550, 500));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createTitledBorder("Diagram Venn otomatis"));
        }

        void setDataset(Dataset d) {
            dataset = d;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (dataset == null) return;
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int diameter = Math.min(230, Math.min((w - 100) / 2, (h - 150) / 2));
            diameter = Math.max(145, diameter);
            int x1 = w / 2 - diameter / 2 - 55;
            int x2 = w / 2 - diameter / 2 + 55;
            int y1 = 95;
            int y2 = 95 + diameter / 2;

            g.setColor(new Color(80, 150, 245, 95));
            g.fillOval(x1, y1, diameter, diameter);
            g.setColor(new Color(245, 150, 70, 95));
            g.fillOval(x2, y1, diameter, diameter);
            g.setColor(new Color(80, 190, 120, 95));
            g.fillOval(w / 2 - diameter / 2, y2, diameter, diameter);

            g.setStroke(new BasicStroke(2f));
            g.setColor(new Color(45, 95, 160));
            g.drawOval(x1, y1, diameter, diameter);
            g.setColor(new Color(180, 100, 35));
            g.drawOval(x2, y1, diameter, diameter);
            g.setColor(new Color(35, 125, 75));
            g.drawOval(w / 2 - diameter / 2, y2, diameter, diameter);

            g.setColor(Color.DARK_GRAY);
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            g.drawString("Pendidikan (A): " + dataset.categories.get("Pendidikan").size(), x1 + 8, y1 - 12);
            g.drawString("Komunikasi (B): " + dataset.categories.get("Komunikasi").size(), x2 + 8, y1 - 12);
            g.drawString("Hiburan (C): " + dataset.categories.get("Hiburan").size(), w / 2 - 45, y2 + diameter + 20);

            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            Set<String> ab = intersection(dataset.categories.get("Pendidikan"), dataset.categories.get("Komunikasi"));
            Set<String> ac = intersection(dataset.categories.get("Pendidikan"), dataset.categories.get("Hiburan"));
            Set<String> bc = intersection(dataset.categories.get("Komunikasi"), dataset.categories.get("Hiburan"));
            Set<String> abc = intersection(dataset.categories.get("Pendidikan"),
                    dataset.categories.get("Komunikasi"), dataset.categories.get("Hiburan"));

            g.drawString("A∩B: " + ab.size(), w / 2 - 25, y1 + diameter / 2 - 15);
            g.drawString("A∩C: " + ac.size(), w / 2 - 90, y2 + 25);
            g.drawString("B∩C: " + bc.size(), w / 2 + 45, y2 + 25);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.drawString("A∩B∩C: " + abc.size(), w / 2 - 38, y2 + diameter / 2 + 10);

            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g.drawString("Semesta U: " + dataset.universe.size() + " aplikasi", 15, h - 28);
            g.drawString("Catatan: lingkaran menunjukkan kategori; angka menunjukkan ukuran irisan.", 15, h - 12);
            g.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SistemKlasifikasiAplikasi().setVisible(true));
    }
}
