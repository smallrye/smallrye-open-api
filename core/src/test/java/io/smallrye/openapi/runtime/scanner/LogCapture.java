package io.smallrye.openapi.runtime.scanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Capture logs from a named logger and make them available to the test
 * <p>
 * <code><pre>
 * &commat;RegisterExtension
 * LogCapture c = new LogCapture(ClassUnderTest.class.getName());
 *
 * &commat;Test
 * public void test() {
 *     // do something to provoke a log message
 *     new ClassUnderTest().logMyMessage();
 *
 *     LogRecord r = c.assertLogContaining("My Special Message");
 *     assertEquals(Level.INFO, r.getLevel());
 * }
 * </pre></code>
 */
public class LogCapture implements BeforeEachCallback, AfterEachCallback {

    private String loggerName;
    private Logger logger;
    private TestHandler handler;
    private Level oldLevel;

    public LogCapture(String loggerName) {
        this.loggerName = loggerName;
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        logger = Logger.getLogger(loggerName);
        handler = new TestHandler();
        logger.addHandler(handler);
        oldLevel = logger.getLevel();
        logger.setLevel(Level.ALL);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        if (handler != null) {
            logger.removeHandler(handler);
        }
        if (logger != null) {
            logger.setLevel(oldLevel);
        }
    }

    public List<LogRecord> getAll() {
        synchronized (handler.records) {
            return new ArrayList<>(handler.records);
        }
    }

    public LogRecord assertLogContaining(String substring) {
        synchronized (handler.records) {
            for (LogRecord r : handler.records) {
                if (r.getMessage().contains(substring)) {
                    return r;
                }

            }

            StringBuilder sb = new StringBuilder();
            sb.append("Log containing \"").append(substring).append("\" was not found.");
            sb.append("\n");
            sb.append("Log records recorded:\n");
            if (handler.records.isEmpty()) {
                sb.append("<no records>\n");
            }
            for (LogRecord r : handler.records) {
                sb.append("[").append(r.getLevel()).append("] ");
                sb.append(r.getMessage()).append("\n");
            }

            throw new AssertionError(sb.toString());
        }
    }

    private static class TestHandler extends Handler {

        private List<LogRecord> records = Collections.synchronizedList(new ArrayList<>());

        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

    }

}
