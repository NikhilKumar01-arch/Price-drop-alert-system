
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.net.InetSocketAddress;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PriceDropServer {

    static List<Product> products = new ArrayList<>();

    public static void main(String[] args) throws Exception {

        products.add(new Product("Smartphone", 15000, 12000));
        products.add(new Product("Laptop", 55000, 45000));
        products.add(new Product("Headphones", 3000, 2000));

        HttpServer server = HttpServer.create(
            new InetSocketAddress(8080), 0
        );

        server.createContext("/", exchange -> {
            String html = createPage();
            sendResponse(exchange, html);
        });

        server.createContext("/add", exchange -> {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String data = new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8
                );

                Map<String, String> form = parseForm(data);

                try {
                    String name = form.get("name");
                    double price = Double.parseDouble(form.get("price"));
                    double target = Double.parseDouble(form.get("target"));

                    if (name == null || name.isBlank()
                            || price < 0 || target < 0) {
                        throw new IllegalArgumentException();
                    }

                    products.add(new Product(name, price, target));

                } catch (Exception e) {
                    // Invalid form data is ignored.
                }
            }

            exchange.getResponseHeaders().set(
                "Location", "/"
            );
            exchange.sendResponseHeaders(303, -1);
            exchange.close();
        });

        server.createContext("/update", exchange -> {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String data = new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8
                );

                Map<String, String> form = parseForm(data);

                try {
                    int id = Integer.parseInt(form.get("id"));
                    double price = Double.parseDouble(form.get("price"));

                    for (Product p : products) {
                        if (p.getId() == id) {
                            p.updatePrice(price);
                            break;
                        }
                    }
                } catch (Exception e) {
                    // Invalid update is ignored.
                }
            }

            exchange.getResponseHeaders().set("Location", "/");
            exchange.sendResponseHeaders(303, -1);
            exchange.close();
        });

        server.setExecutor(null);
        server.start();

        System.out.println("Price Drop Alert System Started!");
        System.out.println("Open http://localhost:8080");
    }

    static Map<String, String> parseForm(String data) {
        Map<String, String> map = new HashMap<>();

        for (String pair : data.split("&")) {
            String[] parts = pair.split("=", 2);

            if (parts.length == 2) {
                map.put(
                    java.net.URLDecoder.decode(
                        parts[0], StandardCharsets.UTF_8
                    ),
                    java.net.URLDecoder.decode(
                        parts[1], StandardCharsets.UTF_8
                    )
                );
            }
        }
        return map;
    }

    static String createPage() {
        StringBuilder rows = new StringBuilder();

        for (Product p : products) {
            rows.append("<tr>")
                .append("<td>").append(p.getId()).append("</td>")
                .append("<td>").append(escape(p.getName())).append("</td>")
                .append("<td>₹").append(p.getCurrentPrice()).append("</td>")
                .append("<td>₹").append(p.getTargetPrice()).append("</td>")
                .append("<td>").append(p.getStatus()).append("</td>")
                .append("<td>");

            if (!p.isAlertSent()) {
                rows.append("<form action='/update' method='post'>")
                    .append("<input type='hidden' name='id' value='")
                    .append(p.getId()).append("'>")
                    .append("<input type='number' step='0.01' min='0' ")
                    .append("name='price' placeholder='New price' required>")
                    .append("<button>Update</button></form>");
            } else {
                rows.append("Alert activated!");
            }

            rows.append("</td></tr>");
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
            <title>Price Drop Alert System</title>
            <meta charset="UTF-8">
            <style>
            body {
                font-family: Arial;
                background: #eef2f7;
                text-align: center;
                padding: 25px;
            }
            h1 { color: #183153; }
            .container {
                background: white;
                padding: 25px;
                border-radius: 12px;
                max-width: 1000px;
                margin: auto;
            }
            input {
                padding: 10px;
                margin: 5px;
            }
            button {
                padding: 10px 15px;
                background: #1769aa;
                color: white;
                border: none;
                cursor: pointer;
                border-radius: 5px;
            }
            table {
                width: 100%;
                border-collapse: collapse;
                margin-top: 25px;
            }
            th, td {
                padding: 12px;
                border: 1px solid #ddd;
            }
            th {
                background: #183153;
                color: white;
            }
            </style>
            </head>
            <body>
            <div class="container">
            <h1>Price Drop Alert System</h1>
            <h3>Add New Product</h3>

            <form action="/add" method="post">
            <input name="name" placeholder="Product Name" required>
            <input type="number" name="price" placeholder="Current Price"
            min="0" step="0.01" required>
            <input type="number" name="target" placeholder="Target Price"
            min="0" step="0.01" required>
            <button type="submit">Add Product</button>
            </form>

            <h2>Product List</h2>
            <table>
            <tr>
            <th>ID</th>
            <th>Product</th>
            <th>Current Price</th>
            <th>Target Price</th>
            <th>Status</th>
            <th>Action</th>
            </tr>
            """ + rows + """
            </table>
            </div>
            </body>
            </html>
            """;
    }

    static String escape(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    static void sendResponse(HttpExchange exchange, String html)
            throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
            "Content-Type", "text/html; charset=UTF-8"
        );

        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
