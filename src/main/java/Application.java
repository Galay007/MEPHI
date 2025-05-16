import com.sun.net.httpserver.HttpServer;
import otp.api.RouteApi;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Application {
    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

            RouteApi routes = new RouteApi();
            routes.registerRoutes(server);
            server.start();

            System.out.println("Server started on http://localhost:" + 8080);
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            System.exit(1);
        }
    }
}