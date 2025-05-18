package com.jyx.batch;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.batch.runtime.BatchRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

@QuarkusMain
public class Runner implements QuarkusApplication {

    private static final Logger logger = LoggerFactory.getLogger(Runner.class);

    @Override
    public int run(String... args) {

        var jobName = "";
        if (args.length == 0) {
            jobName = "pricesJob";
        }

        var jobOperator = BatchRuntime.getJobOperator();
        var jobProperties = new Properties();

        var executionId = jobOperator.start(jobName, jobProperties);
        logger.info("Batch job started with execution ID: {}", executionId);
        return 0;
    }
}
