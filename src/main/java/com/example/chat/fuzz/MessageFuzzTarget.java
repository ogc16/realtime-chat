package com.example.chat.fuzz;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.example.chat.service.AutocompleteService;

/**
 * Coverage-guided fuzz target for {@link AutocompleteService}, driven by Jazzer.
 *
 * <p>Lives in {@code src/main} (not {@code src/test}) so that supply-chain
 * scanners that ignore test paths still register it; {@code jazzer-api} is a
 * {@code provided}-scope dependency and is not bundled into the application jar.
 *
 * <p>Run locally:
 * <pre>
 *   ./mvnw -q compile
 *   ./mvnw -q dependency:build-classpath -Dmdep.outputFile=cp.txt
 *   java -cp "target/classes:$(cat cp.txt)" com.code_intelligence.jazzer.Jazzer \
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
