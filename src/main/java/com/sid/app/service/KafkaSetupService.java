package com.sid.app.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

/**
 * Service class responsible for setting up Kafka dynamically based on request parameters.
 */
@Getter
@Service
@Slf4j
public class KafkaSetupService {

    private static final String KAFKA_FOLDER_NAME = "kafka_setup";
    private static final String KAFKA_DOWNLOAD_URL = "https://downloads.apache.org/kafka/3.9.0/kafka_2.13-3.9.0.tgz";
    private static final String KAFKA_ARCHIVE_NAME = "kafka.tgz";
    private static final String EXTRACTED_FOLDER_PREFIX = "kafka_2.13-3.9.0";
    private static final String RENAMED_FOLDER_NAME = "kafka";

    // Reactive sink to emit Kafka setup status updates
    private final Sinks.Many<String> kafkaSetupSink = Sinks.many().multicast().onBackpressureBuffer();

    /**
     * Kafka setup runner method to execute Kafka installation asynchronously.
     *
     * @param kafkaAutoSetupRequired       Flag indicating if automatic Kafka setup is required.
     * @param kafkaUserDefinedPathRequired Flag indicating if a user-defined path should be used.
     * @param kafkaUserDefinedPath         The user-defined installation path for Kafka (if applicable).
     * @return ApplicationRunner to execute the setup process.
     */
    public ApplicationRunner kafkaSetupRunner(boolean kafkaAutoSetupRequired, boolean kafkaUserDefinedPathRequired, String kafkaUserDefinedPath) {
        return args -> {
            if (!kafkaAutoSetupRequired) {
                log.info("kafkaSetupRunner() : Kafka auto setup is disabled. Skipping setup.");
                kafkaSetupSink.tryEmitNext("Kafka setup skipped.");
                return;
            }

            log.info("kafkaSetupRunner() : === Kafka Setup Initialization ===");

            // Determine the setup directory
            String setupBasePath = (kafkaUserDefinedPathRequired && kafkaUserDefinedPath != null && !kafkaUserDefinedPath.isEmpty())
                    ? kafkaUserDefinedPath
                    : System.getProperty("user.home") + File.separator + "Downloads";

            Path kafkaSetupPath = Paths.get(setupBasePath, KAFKA_FOLDER_NAME);
            Path kafkaArchivePath = kafkaSetupPath.resolve(KAFKA_ARCHIVE_NAME);
            Path finalKafkaFolder = kafkaSetupPath.resolve(RENAMED_FOLDER_NAME);

            log.info("kafkaSetupRunner() : Kafka setup directory: {}", kafkaSetupPath);

            try {
                Files.createDirectories(kafkaSetupPath);

                // Download Kafka if not already present
                if (!Files.exists(kafkaArchivePath)) {
                    log.info("kafkaSetupRunner() : Kafka archive not found. Downloading...");
                    downloadKafka(kafkaArchivePath.toString());
                }

                // Extract Kafka if not already set up
                if (!Files.exists(finalKafkaFolder)) {
                    log.info("kafkaSetupRunner() : Extracting Kafka archive...");
                    extractKafkaArchive(kafkaArchivePath.toString(), kafkaSetupPath.toString());
                    TimeUnit.SECONDS.sleep(2);
                    renameKafkaFolder(kafkaSetupPath);
                    deleteKafkaTarFile(kafkaSetupPath);
                }
                log.info("kafkaSetupRunner() : === Kafka Setup Completed Successfully ===");
                kafkaSetupSink.tryEmitNext("Kafka setup completed successfully.");
            } catch (Exception e) {
                log.error("kafkaSetupRunner() : Error during Kafka setup: {}", e.getMessage(), e);
                kafkaSetupSink.tryEmitNext("Error during Kafka setup: " + e.getMessage());
            }
        };
    }

    /**
     * Downloads the Kafka archive from the official URL.
     *
     * @param filePath The destination path for the downloaded Kafka archive.
     */
    private void downloadKafka(String filePath) {
        try {
            log.info("downloadKafka() : Downloading Kafka from {} to {}", KAFKA_DOWNLOAD_URL, filePath);
            FileUtils.copyURLToFile(new URL(KAFKA_DOWNLOAD_URL), new File(filePath));
            log.info("downloadKafka() : Kafka downloaded successfully.");
        } catch (IOException e) {
            log.error("downloadKafka() : Failed to download Kafka: {}", e.getMessage(), e);
        }
    }

    /**
     * Extracts the Kafka .tgz archive by first decompressing the GZIP and then extracting the TAR file.
     *
     * @param archivePath    Path to the .tgz Kafka archive.
     * @param destinationDir Directory where Kafka should be extracted.
     */
    private void extractKafkaArchive(String archivePath, String destinationDir) {
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(archivePath));
             FileOutputStream fileOutputStream = new FileOutputStream(destinationDir + File.separator + "kafka.tar")) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = gzipInputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }

            extractTarFile(destinationDir + File.separator + "kafka.tar", destinationDir);
        } catch (IOException e) {
            log.error("extractKafkaArchive() : Failed to extract Kafka archive: {}", e.getMessage(), e);
        }
    }

    /**
     * Extracts the TAR file containing Kafka binaries into the specified directory.
     *
     * @param tarFilePath    Path to the extracted TAR file.
     * @param destinationDir Directory where Kafka should be extracted.
     */
    private void extractTarFile(String tarFilePath, String destinationDir) {
        try (TarArchiveInputStream tis = new TarArchiveInputStream(new BufferedInputStream(new FileInputStream(tarFilePath)))) {
            TarArchiveEntry entry;
            while ((entry = tis.getNextTarEntry()) != null) {
                File outputFile = new File(destinationDir, entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(outputFile.toPath());
                } else {
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile))) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = tis.read(buffer)) != -1) {
                            bos.write(buffer, 0, bytesRead);
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("extractTarFile() : Failed to extract Kafka TAR file: {}", e.getMessage(), e);
        }
    }

    /**
     * Renames the extracted Kafka folder to a standardized name.
     *
     * @param kafkaSetupPath Path where Kafka is extracted.
     */
    private void renameKafkaFolder(Path kafkaSetupPath) {
        try (Stream<Path> paths = Files.list(kafkaSetupPath)) {
            paths.filter(Files::isDirectory)
                    .filter(path -> path.getFileName().toString().startsWith(EXTRACTED_FOLDER_PREFIX))
                    .findFirst()
                    .ifPresent(extractedFolder -> {
                        Path renamedPath = kafkaSetupPath.resolve(RENAMED_FOLDER_NAME);
                        try {
                            Files.move(extractedFolder, renamedPath, StandardCopyOption.REPLACE_EXISTING);
                            log.info("renameKafkaFolder() : Successfully renamed Kafka folder to {}", renamedPath);
                        } catch (IOException e) {
                            log.error("renameKafkaFolder() : Failed to rename Kafka folder: {}", e.getMessage(), e);
                        }
                    });
        } catch (IOException e) {
            log.error("renameKafkaFolder() : Error renaming Kafka folder: {}", e.getMessage(), e);
        }
    }

    /**
     * Deletes the temporary TAR file after extraction to free up space.
     *
     * @param kafkaSetupPath Path where Kafka setup files are located.
     */
    private void deleteKafkaTarFile(Path kafkaSetupPath) {
        Path kafkaTarPath = kafkaSetupPath.resolve("kafka.tar");
        try {
            Files.deleteIfExists(kafkaTarPath);
            log.info("deleteKafkaTarFile() : Deleted temporary kafka.tar file.");
        } catch (IOException e) {
            log.error("deleteKafkaTarFile() : Failed to delete kafka.tar: {}", e.getMessage(), e);
        }
    }

}