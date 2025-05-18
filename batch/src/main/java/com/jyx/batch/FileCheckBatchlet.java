package com.jyx.batch;

import jakarta.batch.api.Batchlet;
import jakarta.batch.runtime.BatchStatus;
import jakarta.batch.runtime.context.JobContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Named
@ApplicationScoped
public class FileCheckBatchlet implements Batchlet {

    private static final Logger logger = LoggerFactory.getLogger(FileCheckBatchlet.class);


    @ConfigProperty(name = "batch.toprocess.dir")
    String toProcessDir;

    @ConfigProperty(name = "batch.processing.dir")
    String processingDir;

    @Inject
    JobContext jobContext;


    @Override
    public String process() throws Exception {
        // Get the first file in the toprocess directory
        File toProcessDir = new File(Paths.get(this.toProcessDir).toUri());
        File[] files = toProcessDir.listFiles(File::isFile);

        if (files == null || files.length == 0) {
            logger.info("No files found in directory: {}", toProcessDir.getAbsolutePath());
            // throw new IllegalStateException("File does not exist");
            // jobContext.setExitStatus("FAILED");
            return "FAILED";
        }

        // Process the first file
        File sourceFile = files[0];

        // Check if file exists and is readable
        if (!sourceFile.exists()) {
            throw new IllegalStateException("File does not exist: " + sourceFile.getAbsolutePath());
        }

        if (!sourceFile.canRead()) {
            throw new IllegalStateException("File is not readable: " + sourceFile.getAbsolutePath());
        }

        // Prepare destination path
        Path sourcePath = sourceFile.toPath();
        Path destPath = Paths.get(this.processingDir, sourceFile.getName());

        // Ensure processing directory exists
        Files.createDirectories(destPath.getParent());

        // Copy file with attributes
        Files.move(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);

        // Store file name in job context
        jobContext.setTransientUserData(sourceFile.getName());

        return BatchStatus.COMPLETED.toString();
    }

    @Override
    public void stop() throws Exception {
        // No specific stop logic needed
    }
}
