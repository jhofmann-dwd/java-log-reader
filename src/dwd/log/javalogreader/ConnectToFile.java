package dwd.log.javalogreader;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Timer;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectToFile {
    private static final int BUFFER_SIZE = 8192;
    private static final int BATCH_SIZE = 1000;
    private static final String CACHE_DIR = "cache";
    private Map<String, Path> activeFiles = new ConcurrentHashMap<>();
    private Timer cleanupTimer = new Timer(true);

    String url;
    HttpClient con;
    LocalTime minTime;
    LocalTime maxTime;
    JTextArea result;
    boolean inputBox;
    String input;
    String fileText;
    String username, password;
    StringBuilder output = new StringBuilder();
    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    // Constructor remains the same
    public ConnectToFile(String url, HttpClient con, String input, JTextArea result, String fileText, String username, String password, boolean inputBox, LocalTime minTime, LocalTime maxTime) throws MalformedURLException, URISyntaxException {
        this.url = url;
        this.con = con;
        this.input = input;
        this.result = result;
        this.fileText = fileText;
        this.username = username;
        this.password = password;
        this.minTime = minTime;
        this.maxTime = maxTime;
    }

    public ConnectToFile() {
    }

    private String generateCacheKey() {
        return String.format("%s_%s_%s_%s",
                url.hashCode(),
                minTime.toString().replace(":", ""),  // Remove colons from time
                maxTime.toString().replace(":", ""),  // Remove colons from time
                input == null ? "null" : input.hashCode()
        );
    }

    private Path getCacheFile() throws IOException {
        String cacheKey = generateCacheKey();
        Path cacheDir = Paths.get(CACHE_DIR);
        if (!Files.exists(cacheDir)) {
            Files.createDirectories(cacheDir);
        }
        return cacheDir.resolve(cacheKey + ".txt");
    }


    public CompletableFuture<String> outputString() {
        try {
            Path cacheFile = getCacheFile();
            String cacheKey = generateCacheKey();

            // Check if cache exists and is valid
            if (Files.exists(cacheFile)) {
                activeFiles.put(cacheKey, cacheFile);
                scheduleFileDeletion(cacheFile, cacheKey);
                return CompletableFuture.supplyAsync(() -> {
                    try {
                        List<String> lines = Files.readAllLines(cacheFile);
                        String content = String.join("\n", lines);
                        SwingUtilities.invokeLater(() -> {
                            if (result.isDisplayable()) {
                                result.setText(content);
                            }
                        });
                        return "Loaded from cache";
                    } catch (IOException e) {
                        throw new CompletionException(e);
                    }
                }, executorService);
            }

            HttpClient client = HttpClient.newBuilder()
                    .authenticator(new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password.toCharArray());
                        }
                    })
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                    .thenApplyAsync(response -> {
                        if (response.statusCode() == 401) {
                            throw new CompletionException(
                                    new IOException("Unauthorized: Bitte überprüfen Sie Ihre Anmeldedaten"));
                        }

                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(response.body(), StandardCharsets.UTF_8), BUFFER_SIZE);
                             BufferedWriter writer = Files.newBufferedWriter(cacheFile, StandardCharsets.UTF_8)) {

                            AtomicInteger lineCounter = new AtomicInteger();
                            AtomicInteger matchesCount = new AtomicInteger();
                            List<String> currentBatch = new ArrayList<>(BATCH_SIZE);

                            String line;
                            while ((line = reader.readLine()) != null) {
                                final int currentLineNum = lineCounter.incrementAndGet();

                                if (line.length() > 8) {
                                    try {
                                        long time = Integer.parseInt(line.substring(0, 8), 16) * 1000L;
                                        LocalDateTime date = LocalDateTime.ofInstant(
                                                java.time.Instant.ofEpochMilli(time),
                                                java.time.ZoneId.systemDefault());

                                        if (date.isAfter(minTime.atDate(date.toLocalDate())) &&
                                                date.isBefore(maxTime.atDate(date.toLocalDate()))) {

                                            if (input != null && (input.isEmpty() && inputBox ||
                                                    line.contains(input))) {

                                                DateTimeFormatter dateFormat =
                                                        DateTimeFormatter.ofPattern("HH:mm:ss");
                                                String formattedLine = String.format("[Line: %d | Time: %s]%s",
                                                        currentLineNum,
                                                        date.format(dateFormat),
                                                        line.substring(8));

                                                currentBatch.add(formattedLine);
                                                writer.write(formattedLine + "\n");
                                                matchesCount.incrementAndGet();

                                                if (currentBatch.size() >= BATCH_SIZE) {
                                                    final String batchText = String.join("\n", currentBatch) + "\n";
                                                    updateTextArea(batchText);
                                                    currentBatch.clear();
                                                }
                                            }
                                        }
                                    } catch (NumberFormatException ignored) {
                                        // Skip malformed lines
                                    }
                                }
                            }

                            if (!currentBatch.isEmpty()) {
                                final String batchText = String.join("\n", currentBatch) + "\n";
                                updateTextArea(batchText);
                                writer.write(batchText);
                            }

                            final String totalCount = "\nTOTAL: " + matchesCount.get() + " Lines.";
                            updateTextArea(totalCount);
                            writer.write(totalCount);

                            activeFiles.put(cacheKey, cacheFile);
                            scheduleFileDeletion(cacheFile, cacheKey);

                            return "Processing complete";

                        } catch (IOException e) {
                            throw new CompletionException(e);
                        }
                    }, executorService);

        } catch (Exception e) {
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    private void scheduleFileDeletion(Path file, String cacheKey) {
        cleanupTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    activeFiles.remove(cacheKey);
                    Files.deleteIfExists(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 3 * 60 * 60 * 1000); // 3 hours
    }

    public void cleanup() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }

        cleanupTimer.cancel();
        // Clean up any remaining cache files
        for (Map.Entry<String, Path> entry : activeFiles.entrySet()) {
            try {
                Files.deleteIfExists(entry.getValue());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        activeFiles.clear();
    }


    private void updateTextArea(final String text) {
        SwingUtilities.invokeLater(() -> {
            if (result.isDisplayable()) {
                // Clear the text area if it's getting too large
                if (result.getText().length() > 1000000) {
                    result.setText("");
                }

                // Ensure text ends with a newline
                String textToAppend = text.endsWith("\n") ? text : text + "\n";
                result.append(textToAppend);

                // Scroll to the bottom
                result.setCaretPosition(result.getDocument().getLength());
            }
        });
    }


    public CompletableFuture<Boolean> checkAuth(String username, String password, String host) {
        HttpClient client = HttpClient.newBuilder()
                .authenticator(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password.toCharArray());
                    }
                })
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + host))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .thenApply(response -> {
                    return response.statusCode() != 401;
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return false;
                });
    }
}