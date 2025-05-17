package com.jyx;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class AppStartUp {

    void onStart(@Observes StartupEvent ev) {
        Book.createIndexes();
    }
}