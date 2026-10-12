package com.example.chat.fuzz;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.example.chat.service.AutocompleteService;

/**
 * Coverage-guided fuzz target for {@link AutocompleteService}, driven by Jazzer.
 *
 * <p>Run locally:
 * <pre>
 *   ./mvnw -q test-compile
 *   ./mvnw -q dependency:build-classpath -Dmdep.outputFile=cp.txt
 *   java -cp "target/test-classes:$(cat cp.txt)" com.code_intelligence.jazzer.Jazzer \
 *       --target_class=com.example.chat.fuzz.MessageFuzzTarget
 * </pre>
 */
public final class MessageFuzzTarget {

    private MessageFuzzTarget() {
    }

    public static void fuzzerTestOneInput(FuzzedDataProvider data) {
        int limit = data.consumeInt(0, 25);
        String message = data.consumeRemainingAsString();

        AutocompleteService service = new AutocompleteService();
        service.record(message);
        service.suggest(message, limit);
    }
}
