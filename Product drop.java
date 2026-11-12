
import java.util.ArrayList;
import java.util.List;

public class Product {
    private static final List<Product> products = new ArrayList<>();
    private static int nextId = 1;

    private final int id;
    private final String name;
    private double currentPrice;
    private final double targetPrice;
    private boolean alertSent = false;

    public Product(String name, double currentPrice, double targetPrice) {
        this.id = nextId++;
        this.name = name;
        this.currentPrice = currentPrice;
        this.targetPrice = targetPrice;
    }

    public static synchronized void addProduct(
            String name, double currentPrice, double targetPrice) {
        products.add(new Product(name, currentPrice, targetPrice));
    }

    public static synchronized List<Product> getAllProducts() {
        return new ArrayList<>(products);
    }

    public static synchronized void updateAllPrices() {
        for (Product p : products) {
            if (p.currentPrice > p.targetPrice) {
                p.currentPrice = Math.max(
                    p.targetPrice, p.currentPrice * 0.99
                );
            }

            if (!p.alertSent && p.currentPrice <= p.targetPrice) {
                p.alertSent = true;
                System.out.println("PRICE DROP ALERT: " + p.name
                        + " reached Rs. " + p.currentPrice);
            }
        }
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getCurrentPrice() { return currentPrice; }
    public double getTargetPrice() { return targetPrice; }
    public boolean isAlertSent() { return alertSent; }

    public String toJson() {
        return "{\"id\":" + id
                + ",\"name\":\"" + escape(name)
                + "\",\"currentPrice\":" + currentPrice
                + ",\"targetPrice\":" + targetPrice
                + ",\"alertSent\":" + alertSent + "}";
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
