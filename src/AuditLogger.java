import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditLogger {

    private static final Path LOG_DIRECTORY =
            Paths.get("logs");

    private static final Path LOG_FILE =
            LOG_DIRECTORY.resolve("audit.log");

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AuditLogger() {
        createLogDirectory();
    }

    private void createLogDirectory() {

        try {

            Files.createDirectories(
                    LOG_DIRECTORY
            );

        } catch (IOException | SecurityException e) {

            System.out.println(
                    "Erreur : impossible de préparer les logs."
            );
        }
    }

    public void log(
            String event,
            String username,
            String details
    ) {

        if (event == null || event.isBlank()) {
            return;
        }

        if (username == null || username.isBlank()) {
            username = "UNKNOWN";
        }

        if (details == null) {
            details = "";
        }

        String timestamp =
                LocalDateTime.now()
                        .format(FORMATTER);

        String line =
                "["
                        + timestamp
                        + "] "
                        + event
                        + " | user="
                        + username
                        + " | "
                        + details
                        + System.lineSeparator();

        try {

            Files.writeString(
                    LOG_FILE,
                    line,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

        } catch (IOException | SecurityException e) {

            System.out.println(
                    "Erreur : impossible d'écrire dans le journal."
            );
        }
    }

    public Path getLogFile() {
        return LOG_FILE;
    }
}