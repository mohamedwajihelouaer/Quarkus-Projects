package com.jyx.batch;

import io.quarkus.test.QuarkusUnitTest;
import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.BatchStatus;
import jakarta.batch.runtime.JobExecution;
import jakarta.batch.runtime.StepExecution;
import jakarta.inject.Inject;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public class FileCheckBatchletTest {
    private static final Logger logger = LoggerFactory.getLogger(FileCheckBatchlet.class);
    private static final Logger log = LoggerFactory.getLogger(FileCheckBatchletTest.class);
    @RegisterExtension
    static QuarkusUnitTest TEST = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(FileCheckBatchlet.class)
                    .addAsManifestResource("fileCheckBatchlet.xml", "batch-jobs/fileCheckBatchlet.xml"))
            .overrideConfigKey("batch.toprocess.dir", System.getProperty("java.io.tmpdir") + "/toprocess")
            .overrideConfigKey("batch.processing.dir", System.getProperty("java.io.tmpdir") + "/processing");
    @Inject
    JobOperator jobOperator;

    @Test
    public void runBatchletJobWithExistingFile() throws Exception {

        String toProcessPath = System.getProperty("java.io.tmpdir");
        String toProcess = "toprocess";
        Path toProcessPathCreated = Paths.get(toProcessPath, toProcess);
        Files.createDirectories(toProcessPathCreated);
        log.info("Created path for test file: {}", toProcessPathCreated);

        Path testFile = toProcessPathCreated.resolve("test.txt");
        Files.writeString(testFile, "Test content");
        log.info("Created test file: {}", testFile);

        Properties jobParameters = new Properties();
        jobParameters.setProperty("fileName", "test.txt");

        long executionId = jobOperator.start("fileCheckBatchlet", jobParameters);

        await().pollInterval(100, TimeUnit.MILLISECONDS)
                .atMost(5, TimeUnit.SECONDS)
                .until(() -> {
                    JobExecution jobExecution = jobOperator.getJobExecution(executionId);
                    List<StepExecution> stepExecutions = jobOperator.getStepExecutions(executionId);
                    for (StepExecution stepExecution : stepExecutions) {
                        log.info("Step name: {}, Status: {}", stepExecution.getStepName(), stepExecution.getBatchStatus());
                    }
                    log.info("Execution ID: {}, Status: {}", executionId, jobExecution.getBatchStatus());

                    return BatchStatus.COMPLETED.equals(jobExecution.getBatchStatus());
                });

        Path movedFile = Paths.get(System.getProperty("java.io.tmpdir") + "/processing/test.txt");
        log.info("Moved file Path: {}", movedFile);
        log.info("Moved file exists: {}", Files.exists(movedFile));

        // Verify file was
        assert Files.exists(movedFile);
        assert !Files.exists(testFile);

    }


//    @Test
//    public void runBatchletJobWithNonExistingFile() {
//        Properties jobParameters = new Properties();
//        jobParameters.setProperty("fileName", "nonexistent.txt");
//
//        long executionId = jobOperator.start("fileCheckBatchlet", jobParameters);
//
//        await().pollInterval(100, TimeUnit.MILLISECONDS)
//                .atMost(5, TimeUnit.SECONDS)
//                .until(() -> {
//                    JobExecution jobExecution = jobOperator.getJobExecution(executionId);
//                    System.out.println("Execution ID: " + executionId + ", Status: " + jobExecution.getBatchStatus());
//                    return BatchStatus.FAILED.equals(jobExecution.getBatchStatus());
//                });
//    }


//    @Test
//    public void restartBatchletJob() {
//        Properties jobParameters = new Properties();
//        jobParameters.setProperty("filePath", "src/test/resources/test.txt");
//
//        long executionId = jobOperator.start("fileCheckBatchlet", jobParameters);
//
//        await().atMost(5, TimeUnit.SECONDS).until(() -> {
//            JobExecution jobExecution = jobOperator.getJobExecution(executionId);
//            return BatchStatus.COMPLETED.equals(jobExecution.getBatchStatus());
//        });
//
//        // Simulate stopping the job
//        jobOperator.stop(executionId);
//        await().atMost(5, TimeUnit.SECONDS).until(() -> {
//            JobExecution jobExecution = jobOperator.getJobExecution(executionId);
//            return BatchStatus.STOPPED.equals(jobExecution.getBatchStatus());
//        });
//
//        long restartId = jobOperator.restart(executionId, jobParameters);
//        await().atMost(5, TimeUnit.SECONDS).until(() -> {
//            JobExecution jobExecution = jobOperator.getJobExecution(restartId);
//            return BatchStatus.COMPLETED.equals(jobExecution.getBatchStatus());
//        });
//    }
}