package benchmarks;

import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
public class QueryTest {

    @Param({"African", "History+of+Africa", "African+people+were+slaves"})
    private String query;

    private static final String BASE_URL = "http://localhost:8080";

    @Benchmark
    public String benchmarkSearchHierarchical() throws IOException {
        return sendGetRequest("/search/hierarchical", query);
    }

    @Benchmark
    public String benchmarkSearchTree() throws IOException {
        return sendGetRequest("/search/tree", query);
    }

    @Benchmark
    public String benchmarkSearchUnique() throws IOException {
        return sendGetRequest("/search/unique", query);
    }

    private String sendGetRequest(String endpoint, String queryParam) throws IOException {
        URL url = new URL(BASE_URL + endpoint + "?query=" + queryParam);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("Query error: Response code " + responseCode);
        }

        byte[] response = connection.getInputStream().readAllBytes();
        connection.disconnect();
        return new String(response, StandardCharsets.UTF_8);
    }
}
