package io.smallrye.openapi.tck;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newBufferedWriter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.collections.Lists;
import org.testng.internal.Utils;
import org.testng.log4testng.Logger;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;

public class SmallRyeTckReporter implements IReporter {

    private static final Logger LOG = Logger.getLogger(SmallRyeTckReporter.class);
    private static final String SPEC_TEMPLATE = "https://download.eclipse.org/microprofile/microprofile-open-api-%1$s/microprofile-openapi-spec-%1$s.html";

    protected PrintWriter writer;

    protected final List<SuiteResult> suiteResults = Lists.newArrayList();

    // Reusable buffer
    private final StringBuilder buffer = new StringBuilder();

    private String fileName = "microprofile-openapi-tck-report.html";

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        try {
            writer = createWriter(outputDirectory);
        } catch (IOException e) {
            LOG.error("Unable to create output file", e);
            return;
        }

        for (ISuite suite : suites) {
            suiteResults.add(new SuiteResult(suite));
        }

        writeDocumentStart();
        writeHead();
        writeBody();
        writeDocumentEnd();

        writer.close();
    }

    protected PrintWriter createWriter(String outdir) throws IOException {
        new File(outdir).mkdirs();
        String fileNameProperty = System.getProperty("smallrye.tck-report.file-name", "").trim();
        if (!fileNameProperty.isEmpty()) {
            fileName = fileNameProperty;
        }
        return new PrintWriter(newBufferedWriter(new File(outdir, fileName).toPath(), UTF_8));
    }

    protected void writeDocumentStart() {
        writer.println(
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"https://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">");
        writer.println("<html xmlns=\"https://www.w3.org/1999/xhtml\">");
    }

    protected void writeHead() {
        writer.println("<head>");
        writer.println("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"/>");
        writer.println("<title>SmallRye OpenAPI TCK Report</title>");
        writeStylesheet();
        writer.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
        writer.println("<link rel=\"shortcut icon\" type=\"image/png\" href=\"https://smallrye.io/favicon.ico\" >");
        writer.println("</head>");
    }

    protected void writeStylesheet() {
        writer.println("<style type=\"text/css\">");
        writer.println("  html, body { font-family: 'Open Sans', Arial, sans-serif;font-weight: normal; }");
        writer.println("  table {width:100%;margin-bottom:10px;border-collapse:collapse;empty-cells:show}");
        writer.println("  th,td {border:1px solid black;padding:0.5em}");
        writer.println("  th {vertical-align:bottom}");
        writer.println("  td {vertical-align:top}");
        writer.println("  .stripe td {background-color: #E6EBF9}");
        writer.println("  .num {text-align:right}");
        writer.println("  .passedodd td {background-color: #3F3}");
        writer.println("  .passedeven td {background-color: #57d057}");
        writer.println("  .skippedodd td {background-color: #DDD}");
        writer.println("  .skippedeven td {background-color: #CCC}");
        writer.println("  .failedodd td,.attn {background-color: #F33}");
        writer.println("  .failedeven td,.stripe .attn {background-color: #D00}");
        writer.println("  .stacktrace {white-space:pre;font-family:monospace}");
        writer.println("  .totop {font-size:85%;text-align:center;border-bottom:2px solid #000; padding-bottom:1em;}");
        writer.println("  .invisible {display:none}");
        writer.println("</style>");
    }

    protected void writeBody() {
        final String mpVersion = System.getProperty("microprofile.version");
        final String srVersion = System.getProperty("smallrye.version");
        final String srCommit = System.getProperty("smallrye.commit");

        writer.println("<body>");
        writer.println("<div class=\"content\">");
        writer.printf("<img style=\"height: 4em;\" alt=\"SmallRye\" src=\"%s\">\n", SMALLRYE_LOGO);
        writer.printf("<h1>SmallRye OpenAPI %s</h1>\n", srVersion);
        writer.printf("<h2>MicroProfile OpenAPI %s TCK Test Report</h2>\n", mpVersion);

        writer.println("<hr/>");
        writer.println("<h3>Certification Summary</h3>");
        writer.println("<ul>");

        writer.println("<li>Product Name and Version:");
        writer.printf("<p>SmallRye OpenAPI %s (commit %s)</p>\n", srVersion, srCommit);
        writer.println("</li>");

        writer.println("<li>Specification Name, Version and download URL:");
        writer.printf("<p><a href=\"%s\">MicroProfile OpenAPI %s</a></p>\n", String.format(SPEC_TEMPLATE, mpVersion),
                mpVersion);
        writer.println("</li>");

        writer.println("<li>Java runtime used to run the implementation:");
        writer.printf("<p>Java %s: %s</p>\n", System.getProperty("java.version"), System.getProperty("java.vendor"));
        writer.println("</li>");

        writer.println("<li>Summary of the information for the certification environment:");
        writer.printf("<p>%s %s %s</p>\n", System.getProperty("os.name"), System.getProperty("os.arch"),
                System.getProperty("os.version"));
        writer.println("</li>");

        writer.println("</ul>");

        writer.println("<h3>Suite Summary</h3>");
        writeSuiteSummary();

        writer.println("<h3>Test Summary</h3>");
        writeScenarioSummary();

        writer.println("<h3>Test Details</h3>");
        writeScenarioDetails();

        writer.println("</div>");
        writer.println("</body>");
    }

    protected void writeDocumentEnd() {
        writer.println("</html>");
    }

    protected void writeSuiteSummary() {
        NumberFormat integerFormat = NumberFormat.getIntegerInstance();
        NumberFormat decimalFormat = NumberFormat.getNumberInstance();

        int totalPassedTests = 0;
        int totalSkippedTests = 0;
        int totalFailedTests = 0;
        long totalDuration = 0;

        writer.println("<table>");
        writer.print("<tr>");
        writer.print("<th>Test</th>");
        writer.print("<th># Passed</th>");
        writer.print("<th># Skipped</th>");
        writer.print("<th># Failed</th>");
        writer.print("<th>Duration</th>");
        writer.print("<th>Included Groups</th>");
        writer.print("<th>Excluded Groups</th>");
        writer.println("</tr>");

        int testIndex = 0;
        for (SuiteResult suiteResult : suiteResults) {
            writer.print("<tr><th colspan=\"7\">");
            writer.print(Utils.escapeHtml(suiteResult.getSuiteName()));
            writer.println("</th></tr>");

            for (TestResult testResult : suiteResult.getTestResults()) {
                int passedTests = testResult.getPassedTestCount();
                int skippedTests = testResult.getSkippedTestCount();
                int failedTests = testResult.getFailedTestCount();
                long duration = testResult.getDuration();

                writer.print("<tr");
                if ((testIndex % 2) == 1) {
                    writer.print(" class=\"stripe\"");
                }
                writer.print(">");

                buffer.setLength(0);
                writeTableData(
                        buffer
                                .append("<a href=\"#t")
                                .append(testIndex)
                                .append("\">")
                                .append(Utils.escapeHtml(testResult.getTestName()))
                                .append("</a>")
                                .toString());
                writeTableData(integerFormat.format(passedTests), "num");
                writeTableData(integerFormat.format(skippedTests), (skippedTests > 0 ? "num attn" : "num"));
                writeTableData(integerFormat.format(failedTests), (failedTests > 0 ? "num attn" : "num"));
                writeTableData(Duration.ofMillis(duration).toString(), "num");
                writeTableData(testResult.getIncludedGroups());
                writeTableData(testResult.getExcludedGroups());

                writer.println("</tr>");

                totalPassedTests += passedTests;
                totalSkippedTests += skippedTests;
                totalFailedTests += failedTests;
                totalDuration += duration;

                testIndex++;
            }
            boolean testsInParallel = XmlSuite.ParallelMode.TESTS.equals(suiteResult.getParallelMode());
            if (testsInParallel) {
                Optional<TestResult> maxValue = suiteResult.testResults.stream()
                        .max(Comparator.comparing(TestResult::getDuration));
                if (maxValue.isPresent()) {
                    totalDuration = Math.max(totalDuration, maxValue.get().duration);
                }
            }
        }

        // Print totals if there was more than one test
        if (testIndex > 1) {
            writer.print("<tr>");
            writer.print("<th>Total</th>");
            writeTableHeader(integerFormat.format(totalPassedTests), "num");
            writeTableHeader(
                    integerFormat.format(totalSkippedTests),
                    (totalSkippedTests > 0 ? "num attn" : "num"));
            writeTableHeader(
                    integerFormat.format(totalFailedTests),
                    (totalFailedTests > 0 ? "num attn" : "num"));
            writeTableHeader(decimalFormat.format(totalDuration), "num");
            writer.print("<th colspan=\"2\"></th>");
            writer.println("</tr>");
        }

        writer.println("</table>");
    }

    /** Writes a summary of all the test scenarios. */
    protected void writeScenarioSummary() {
        writer.print("<table id='summary'>");
        writer.print("<thead>");
        writer.print("<tr>");
        writer.print("<th>Class</th>");
        writer.print("<th>Method</th>");
        writer.print("<th>Parameters</th>");
        writer.print("<th>Start</th>");
        writer.print("<th>Time (ms)</th>");
        writer.print("</tr>");
        writer.print("</thead>");

        int testIndex = 0;
        int scenarioIndex = 0;
        for (SuiteResult suiteResult : suiteResults) {
            writer.print("<tbody><tr><th colspan=\"5\">");
            writer.print(Utils.escapeHtml(suiteResult.getSuiteName()));
            writer.print("</th></tr></tbody>");

            for (TestResult testResult : suiteResult.getTestResults()) {
                writer.printf("<tbody id=\"t%d\">", testIndex);

                String testName = Utils.escapeHtml(testResult.getTestName());
                int startIndex = scenarioIndex;

                scenarioIndex += writeScenarioSummary(
                        testName + " &#8212; failed (configuration methods)",
                        testResult.getFailedConfigurationResults(),
                        "failed",
                        scenarioIndex);
                scenarioIndex += writeScenarioSummary(
                        testName + " &#8212; failed",
                        testResult.getFailedTestResults(),
                        "failed",
                        scenarioIndex);
                scenarioIndex += writeScenarioSummary(
                        testName + " &#8212; skipped (configuration methods)",
                        testResult.getSkippedConfigurationResults(),
                        "skipped",
                        scenarioIndex);
                scenarioIndex += writeScenarioSummary(
                        testName + " &#8212; skipped",
                        testResult.getSkippedTestResults(),
                        "skipped",
                        scenarioIndex);
                scenarioIndex += writeScenarioSummary(
                        testName + " &#8212; passed",
                        testResult.getPassedTestResults(),
                        "passed",
                        scenarioIndex);

                if (scenarioIndex == startIndex) {
                    writer.print("<tr><th colspan=\"4\" class=\"invisible\"/></tr>");
                }

                writer.println("</tbody>");

                testIndex++;
            }
        }

        writer.println("</table>");
    }

    /**
     * Writes the scenario summary for the results of a given state for a single
     * test.
     */
    private int writeScenarioSummary(
            String description,
            List<ClassResult> classResults,
            String cssClassPrefix,
            int startingScenarioIndex) {
        int scenarioCount = 0;
        if (!classResults.isEmpty()) {
            writer.print("<tr><th colspan=\"5\">");
            writer.print(description);
            writer.print("</th></tr>");

            int scenarioIndex = startingScenarioIndex;
            int classIndex = 0;
            for (ClassResult classResult : classResults) {
                String cssClass = cssClassPrefix + ((classIndex % 2) == 0 ? "even" : "odd");

                buffer.setLength(0);

                int scenariosPerClass = 0;
                int methodIndex = 0;
                for (MethodResult methodResult : classResult.getMethodResults()
                        .stream()
                        .sorted(Comparator.comparing(m -> m.getResults().get(0).getMethod().getMethodName()))
                        .collect(Collectors.toList())) {

                    List<ITestResult> results = methodResult.getResults()
                            .stream()
                            .sorted(Comparator.comparing(
                                    (ITestResult result) -> Arrays.stream(result.getParameters()).map(Object::toString)
                                            .collect(Collectors.joining(", "))))
                            .collect(Collectors.toList());
                    int resultsCount = results.size();
                    assert resultsCount > 0;

                    ITestResult firstResult = results.iterator().next();
                    String methodName = Utils.escapeHtml(firstResult.getMethod().getMethodName());
                    long start = firstResult.getStartMillis();
                    long duration = firstResult.getEndMillis() - start;

                    // The first method per class shares a row with the class
                    // header
                    if (methodIndex > 0) {
                        buffer.append("<tr class=\"").append(cssClass).append("\">");
                    }

                    // Write the timing information with the first scenario per
                    // method
                    buffer
                            .append("<td><a href=\"#m")
                            .append(scenarioIndex)
                            .append("\">")
                            .append(methodName)
                            .append("</a></td>")
                            .append("<td>")
                            .append(Arrays.stream(firstResult.getParameters()).map(Object::toString)
                                    .collect(Collectors.joining(", ")))
                            .append("</td>")
                            .append("<td>")
                            .append(getFormattedStartTime(start))
                            .append("</td>")
                            .append("<td class=\"num\">")
                            .append(duration)
                            .append("</td></tr>");
                    scenarioIndex++;

                    // Write the remaining scenarios for the method
                    for (int i = 1; i < resultsCount; i++) {
                        buffer
                                .append("<tr class=\"")
                                .append(cssClass)
                                .append("\">")
                                .append("<td><a href=\"#m")
                                .append(scenarioIndex)
                                .append("\">")
                                .append(methodName)
                                .append("</a></td>")
                                .append("<td>")
                                .append(Arrays.stream(results.get(i).getParameters()).map(Object::toString)
                                        .collect(Collectors.joining(", ")))
                                .append("</td>")
                                .append("<td>")
                                .append(getFormattedStartTime(results.get(i).getStartMillis()))
                                .append("</td>")
                                .append("<td class=\"num\">")
                                .append(results.get(i).getEndMillis() - results.get(i).getStartMillis())
                                .append("</td>")
                                .append("</tr>");
                        scenarioIndex++;
                    }

                    scenariosPerClass += resultsCount;
                    methodIndex++;
                }

                // Write the test results for the class
                writer.print("<tr class=\"");
                writer.print(cssClass);
                writer.print("\">");
                writer.print("<td rowspan=\"");
                writer.print(scenariosPerClass);
                writer.print("\">");
                writer.print(Utils.escapeHtml(classResult.getClassName()));
                writer.print("</td>");
                writer.print(buffer);

                classIndex++;
            }
            scenarioCount = scenarioIndex - startingScenarioIndex;
        }
        return scenarioCount;
    }

    protected String getFormattedStartTime(long startTimeInMillisFromEpoch) {
        return Instant.ofEpochMilli(startTimeInMillisFromEpoch).toString();
    }

    /** Writes the details for all test scenarios. */
    protected void writeScenarioDetails() {
        int scenarioIndex = 0;
        for (SuiteResult suiteResult : suiteResults) {
            for (TestResult testResult : suiteResult.getTestResults()) {
                writer.print("<h2>");
                writer.print(Utils.escapeHtml(testResult.getTestName()));
                writer.print("</h2>");

                scenarioIndex += writeScenarioDetails(testResult.getFailedConfigurationResults(), scenarioIndex);
                scenarioIndex += writeScenarioDetails(testResult.getFailedTestResults(), scenarioIndex);
                scenarioIndex += writeScenarioDetails(testResult.getSkippedConfigurationResults(), scenarioIndex);
                scenarioIndex += writeScenarioDetails(testResult.getSkippedTestResults(), scenarioIndex);
                scenarioIndex += writeScenarioDetails(testResult.getPassedTestResults(), scenarioIndex);
            }
        }
    }

    /**
     * Writes the scenario details for the results of a given state for a single
     * test.
     */
    private int writeScenarioDetails(List<ClassResult> classResults, int startingScenarioIndex) {
        int scenarioIndex = startingScenarioIndex;
        for (ClassResult classResult : classResults) {
            String className = classResult.getClassName();
            for (MethodResult methodResult : classResult.getMethodResults()) {
                List<ITestResult> results = methodResult.getResults();
                assert !results.isEmpty();

                String label = Utils.escapeHtml(
                        className + "#" + results.iterator().next().getMethod().getMethodName());
                for (ITestResult result : results) {
                    writeScenario(scenarioIndex, label, result);
                    scenarioIndex++;
                }
            }
        }

        return scenarioIndex - startingScenarioIndex;
    }

    /** Writes the details for an individual test scenario. */
    private void writeScenario(int scenarioIndex, String label, ITestResult result) {
        writer.print("<h3 id=\"m");
        writer.print(scenarioIndex);
        writer.print("\">");
        writer.print(label);
        writer.print("</h3>");

        writer.print("<table class=\"result\">");

        boolean hasRows = false;

        // Write test parameters (if any)
        Object[] parameters = result.getParameters();
        int parameterCount = (parameters == null ? 0 : parameters.length);
        if (parameterCount > 0) {
            writer.print("<tr class=\"param\">");
            for (int i = 1; i <= parameterCount; i++) {
                writer.print("<th>Parameter #");
                writer.print(i);
                writer.print("</th>");
            }
            writer.print("</tr><tr class=\"param stripe\">");
            for (Object parameter : parameters) {
                writer.print("<td>");
                writer.print(Utils.escapeHtml(Utils.toString(parameter)));
                writer.print("</td>");
            }
            writer.print("</tr>");
            hasRows = true;
        }

        // Write reporter messages (if any)
        List<String> reporterMessages = Reporter.getOutput(result);
        if (!reporterMessages.isEmpty()) {
            writer.print("<tr><th");
            if (parameterCount > 1) {
                writer.printf(" colspan=\"%d\"", parameterCount);
            }
            writer.print(">Messages</th></tr>");

            writer.print("<tr><td");
            if (parameterCount > 1) {
                writer.printf(" colspan=\"%d\"", parameterCount);
            }
            writer.print(">");
            writeReporterMessages(reporterMessages);
            writer.print("</td></tr>");
            hasRows = true;
        }

        // Write exception (if any)
        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            writer.print("<tr><th");
            if (parameterCount > 1) {
                writer.printf(" colspan=\"%d\"", parameterCount);
            }
            writer.print(">");
            writer.print(result.getStatus() == ITestResult.SUCCESS ? "Expected Exception" : "Exception");
            writer.print("</th></tr>");

            writer.print("<tr><td");
            if (parameterCount > 1) {
                writer.printf(" colspan=\"%d\"", parameterCount);
            }
            writer.print(">");
            writeStackTrace(throwable);
            writer.print("</td></tr>");
            hasRows = true;
        }

        if (!hasRows) {
            writer.print("<tr><th");
            if (parameterCount > 1) {
                writer.printf(" colspan=\"%d\"", parameterCount);
            }
            writer.print(" class=\"invisible\"/></tr>");
        }

        writer.print("</table>");
        writer.println("<p class=\"totop\"><a href=\"#summary\">back to summary</a></p>");
    }

    protected void writeReporterMessages(List<String> reporterMessages) {
        writer.print("<div class=\"messages\">");
        Iterator<String> iterator = reporterMessages.iterator();
        assert iterator.hasNext();
        if (Reporter.getEscapeHtml()) {
            writer.print(Utils.escapeHtml(iterator.next()));
        } else {
            writer.print(iterator.next());
        }
        while (iterator.hasNext()) {
            writer.print("<br/>");
            if (Reporter.getEscapeHtml()) {
                writer.print(Utils.escapeHtml(iterator.next()));
            } else {
                writer.print(iterator.next());
            }
        }
        writer.print("</div>");
    }

    protected void writeStackTrace(Throwable throwable) {
        writer.print("<div class=\"stacktrace\">");
        writer.print(Utils.shortStackTrace(throwable, true));
        writer.print("</div>");
    }

    /**
     * Writes a TH element with the specified contents and CSS class names.
     *
     * @param html
     *        the HTML contents
     * @param cssClasses
     *        the space-delimited CSS classes or null if there are no
     *        classes to apply
     */
    protected void writeTableHeader(String html, String cssClasses) {
        writeTag("th", html, cssClasses);
    }

    /**
     * Writes a TD element with the specified contents.
     *
     * @param html
     *        the HTML contents
     */
    protected void writeTableData(String html) {
        writeTableData(html, null);
    }

    /**
     * Writes a TD element with the specified contents and CSS class names.
     *
     * @param html
     *        the HTML contents
     * @param cssClasses
     *        the space-delimited CSS classes or null if there are no
     *        classes to apply
     */
    protected void writeTableData(String html, String cssClasses) {
        writeTag("td", html, cssClasses);
    }

    /**
     * Writes an arbitrary HTML element with the specified contents and CSS
     * class names.
     *
     * @param tag
     *        the tag name
     * @param html
     *        the HTML contents
     * @param cssClasses
     *        the space-delimited CSS classes or null if there are no
     *        classes to apply
     */
    protected void writeTag(String tag, String html, String cssClasses) {
        writer.print("<");
        writer.print(tag);
        if (cssClasses != null) {
            writer.print(" class=\"");
            writer.print(cssClasses);
            writer.print("\"");
        }
        writer.print(">");
        writer.print(html);
        writer.print("</");
        writer.print(tag);
        writer.print(">");
    }

    /** Groups {@link TestResult}s by suite. */
    protected static class SuiteResult {
        private final String suiteName;
        private final List<TestResult> testResults = Lists.newArrayList();
        private final ParallelMode mode;

        public SuiteResult(ISuite suite) {
            suiteName = suite.getName();
            mode = suite.getXmlSuite().getParallel();
            for (ISuiteResult suiteResult : suite.getResults().values()) {
                testResults.add(new TestResult(suiteResult.getTestContext()));
            }
        }

        public String getSuiteName() {
            return suiteName;
        }

        /** @return the test results (possibly empty) */
        public List<TestResult> getTestResults() {
            return testResults;
        }

        public ParallelMode getParallelMode() {
            return mode;
        }
    }

    /**
     * Groups {@link ClassResult}s by test, type (configuration or test), and
     * status.
     */
    protected static class TestResult {
        /**
         * Orders test results by class name and then by method name (in
         * lexicographic order).
         */
        protected static final Comparator<ITestResult> RESULT_COMPARATOR = Comparator.comparing(
                (ITestResult o) -> o.getTestClass().getName())
                .thenComparing(o -> o.getMethod().getMethodName());

        private final String testName;
        private final List<ClassResult> failedConfigurationResults;
        private final List<ClassResult> failedTestResults;
        private final List<ClassResult> skippedConfigurationResults;
        private final List<ClassResult> skippedTestResults;
        private final List<ClassResult> passedTestResults;
        private final int failedTestCount;
        private final int skippedTestCount;
        private final int passedTestCount;
        private final long duration;
        private final String includedGroups;
        private final String excludedGroups;

        public TestResult(ITestContext context) {
            testName = context.getName();

            Set<ITestResult> failedConfigurations = context.getFailedConfigurations().getAllResults();
            Set<ITestResult> failedTests = context.getFailedTests().getAllResults();
            Set<ITestResult> skippedConfigurations = context.getSkippedConfigurations().getAllResults();
            Set<ITestResult> skippedTests = context.getSkippedTests().getAllResults();
            Set<ITestResult> passedTests = context.getPassedTests().getAllResults();

            failedConfigurationResults = groupResults(failedConfigurations);
            failedTestResults = groupResults(failedTests);
            skippedConfigurationResults = groupResults(skippedConfigurations);
            skippedTestResults = groupResults(skippedTests);
            passedTestResults = groupResults(passedTests);

            failedTestCount = failedTests.size();
            skippedTestCount = skippedTests.size();
            passedTestCount = passedTests.size();

            duration = context.getEndDate().getTime()
                    - context.getStartDate().getTime();

            includedGroups = formatGroups(context.getIncludedGroups());
            excludedGroups = formatGroups(context.getExcludedGroups());
        }

        /**
         * Groups test results by method and then by class.
         *
         * @param results
         *        All test results
         * @return Test result grouped by method and class
         */
        protected List<ClassResult> groupResults(Set<ITestResult> results) {
            List<ClassResult> classResults = Lists.newArrayList();
            if (!results.isEmpty()) {
                List<MethodResult> resultsPerClass = Lists.newArrayList();
                List<ITestResult> resultsPerMethod = Lists.newArrayList();

                List<ITestResult> resultsList = Lists.newArrayList(results);
                resultsList.sort(RESULT_COMPARATOR);
                Iterator<ITestResult> resultsIterator = resultsList.iterator();
                assert resultsIterator.hasNext();

                ITestResult result = resultsIterator.next();
                resultsPerMethod.add(result);

                String previousClassName = result.getTestClass().getName();
                String previousMethodName = result.getMethod().getMethodName();
                while (resultsIterator.hasNext()) {
                    result = resultsIterator.next();

                    String className = result.getTestClass().getName();
                    if (!previousClassName.equals(className)) {
                        // Different class implies different method
                        assert !resultsPerMethod.isEmpty();
                        resultsPerClass.add(new MethodResult(resultsPerMethod));
                        resultsPerMethod = Lists.newArrayList();

                        assert !resultsPerClass.isEmpty();
                        classResults.add(new ClassResult(previousClassName, resultsPerClass));
                        resultsPerClass = Lists.newArrayList();

                        previousClassName = className;
                        previousMethodName = result.getMethod().getMethodName();
                    } else {
                        String methodName = result.getMethod().getMethodName();
                        if (!previousMethodName.equals(methodName)) {
                            assert !resultsPerMethod.isEmpty();
                            resultsPerClass.add(new MethodResult(resultsPerMethod));
                            resultsPerMethod = Lists.newArrayList();

                            previousMethodName = methodName;
                        }
                    }
                    resultsPerMethod.add(result);
                }
                assert !resultsPerMethod.isEmpty();
                resultsPerClass.add(new MethodResult(resultsPerMethod));
                assert !resultsPerClass.isEmpty();
                classResults.add(new ClassResult(previousClassName, resultsPerClass));
            }
            return classResults;
        }

        public String getTestName() {
            return testName;
        }

        /** @return the results for failed configurations (possibly empty) */
        public List<ClassResult> getFailedConfigurationResults() {
            return failedConfigurationResults;
        }

        /** @return the results for failed tests (possibly empty) */
        public List<ClassResult> getFailedTestResults() {
            return failedTestResults;
        }

        /** @return the results for skipped configurations (possibly empty) */
        public List<ClassResult> getSkippedConfigurationResults() {
            return skippedConfigurationResults;
        }

        /** @return the results for skipped tests (possibly empty) */
        public List<ClassResult> getSkippedTestResults() {
            return skippedTestResults;
        }

        /** @return the results for passed tests (possibly empty) */
        public List<ClassResult> getPassedTestResults() {
            return passedTestResults;
        }

        public int getFailedTestCount() {
            return failedTestCount;
        }

        public int getSkippedTestCount() {
            return skippedTestCount;
        }

        public int getPassedTestCount() {
            return passedTestCount;
        }

        public long getDuration() {
            return duration;
        }

        public String getIncludedGroups() {
            return includedGroups;
        }

        public String getExcludedGroups() {
            return excludedGroups;
        }

        /**
         * Formats an array of groups for display.
         *
         * @param groups
         *        The groups
         * @return The String value of the groups
         */
        protected String formatGroups(String[] groups) {
            if (groups.length == 0) {
                return "";
            }

            StringBuilder builder = new StringBuilder();
            builder.append(groups[0]);
            for (int i = 1; i < groups.length; i++) {
                builder.append(", ").append(groups[i]);
            }
            return builder.toString();
        }
    }

    /** Groups {@link MethodResult}s by class. */
    protected static class ClassResult {
        private final String className;
        private final List<MethodResult> methodResults;

        /**
         * @param className
         *        the class name
         * @param methodResults
         *        the non-null, non-empty {@link MethodResult} list
         */
        public ClassResult(String className, List<MethodResult> methodResults) {
            this.className = className;
            this.methodResults = methodResults;
        }

        public String getClassName() {
            return className;
        }

        /** @return the non-null, non-empty {@link MethodResult} list */
        public List<MethodResult> getMethodResults() {
            return methodResults;
        }
    }

    /** Groups test results by method. */
    protected static class MethodResult {
        private final List<ITestResult> results;

        /**
         * @param results
         *        the non-null, non-empty result list
         */
        public MethodResult(List<ITestResult> results) {
            this.results = results;
        }

        /** @return the non-null, non-empty result list */
        public List<ITestResult> getResults() {
            return results;
        }
    }

    static final String SMALLRYE_LOGO = "data:image/svg+xml;base64,PHN2ZyBpZD0iTGF5ZXJfMSIgZGF0YS1uYW1lPSJMYXllciAxIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNTAgNjQiPgogICAgPGRlZnM+CiAgICAgICAgPHN0eWxlPgogICAgICAgICAgICAuY2xzLTF7CiAgICAgICAgICAgICAgICBmaWxsOiM0M2IzZDc7CiAgICAgICAgICAgIH0KICAgICAgICAgICAgLmNscy0yewogICAgICAgICAgICAgICAgZmlsbDojMzIyZjUxOwogICAgICAgICAgICB9CiAgICAgICAgPC9zdHlsZT4KICAgIDwvZGVmcz4KICAgIDx0aXRsZT5zbWFsbHJ5ZV9hc3NldHM8L3RpdGxlPgogICAgPHBhdGggY2xhc3M9ImNscy0xIiBkPSJNMzguNjcsMTEuNjJsLTcuMjgsMy4yNmEuNjIuNjIsMCwwLDAsMCwxLjEybDUuNDQsMi40MSwxLjA3LjQ4YS42Mi42MiwwLDAsMCwuNSwwbDIuOTEtMS4zMUw0Mi41NCwxN2EuNjIuNjIsMCwwLDAsLjM2LS41NnYtNS44YS42MS42MSwwLDAsMC0uODYtLjU2bC0xLjQ4LjY2WiIvPgogICAgPHBhdGggY2xhc3M9ImNscy0xIiBkPSJNMzcuNzQsNTMuMzF2LjJhLjYxLjYxLDAsMCwwLC44Ni41NmwuNDItLjE5LDMuMTktMS40MiwxMC00LjQ2YS42Mi42MiwwLDAsMCwuMzYtLjU2VjMxLjE2YS42Mi42MiwwLDAsMC0uODctLjU2bC0yLjE4LDEtMiwuOTItLjU4LjI2LTIsLjkyLTIuNiwxLjE2LTIsLjkyLTQuNSwyLTIuNTEsMS4xM0wzMC42OSw0MGEuNjEuNjEsMCwwLDAsMCwxLjEybDEuMjMuNTUsMi41MSwxLjExLDIuOTQsMS4zMWEuNjEuNjEsMCwwLDEsLjM3LjU2djguNjdaIi8+CiAgICA8cGF0aCBjbGFzcz0iY2xzLTEiIGQ9Ik00MC42NiwyMC4xMWwtMi41MSwxLjEzLTQuMSwxLjgzLTMuNzMsMS42Ny0yLjMsMWEuNjEuNjEsMCwwLDAsMCwxLjEybDgsMy41NSwyLjUyLDEuMTEsMi43NiwxLjIzYS42Mi42MiwwLDAsMCwuNSwwbDMtMS4zNiwyLS45MS4yMS0uMWEuNjEuNjEsMCwwLDAsLjM3LS41NlYxOGEuNjIuNjIsMCwwLDAtLjg3LS41Nkw0NSwxOC4xOWwtMi4wNS45MloiLz4KICAgIDxwYXRoIGNsYXNzPSJjbHMtMiIgZD0iTTM0LjgzLDYyLjgyYS42MS42MSwwLDAsMCwuODYtLjU2VjQ2YS42Mi42MiwwLDAsMC0uMzYtLjU2TDMxLjkyLDQzLjlsLTIuNTEtMS4xMi0xLjQ3LS42NS0yLjA1LS45MS0zLTEuMzYtMS40NS0uNjRMMjAuOCwzOWwtMS45Mi0uODYtLjU3LS4yNS0yLS45MS0yLjYtMS4xNi0yLjA1LS45LTEuNTYtLjdhLjYyLjYyLDAsMCwwLS44Ny41NlY1MWEuNjIuNjIsMCwwLDAsLjM3LjU2bDE2LjMzLDcuMjYsMy41NCwxLjU3WiIvPgogICAgPHBhdGggY2xhc3M9ImNscy0yIiBkPSJNMTYuMjcsMjMuOTFsLTEuNzQtLjc4YS42MS42MSwwLDAsMC0uODYuNTZ2OS40NmEuNjIuNjIsMCwwLDAsLjM2LjU2bDIuMjQsMSwyLC45MSwyLjQ5LDEuMTEuNTkuMjYsMS40Ni42NSwxLC40NywyLjc3LDEuMjJhLjU4LjU4LDAsMCwwLC41LDBsMy40Ny0xLjU1LDIuNTEtMS4xMyw0LjYyLTIuMDZhLjYxLjYxLDAsMCwwLDAtMS4xMkwzNiwzMi42OGwtMi41Mi0xLjEyLTkuMjMtNC4xLTIuNTEtMS4xMi0zLjQ0LTEuNTNaIi8+CiAgICA8cGF0aCBjbGFzcz0iY2xzLTIiIGQ9Ik0yMC43NSwxMy41bC0xLjU4LS42OWEuNjEuNjEsMCwwLDAtLjg2LjU2djguODFhLjYxLjYxLDAsMCwwLC4zNy41NkwyNCwyNS4xMWEuNjIuNjIsMCwwLDAsLjUsMGw0LTEuODIsMi4wOS0uOTNMMzMsMjEuM2wxLjY0LS43NGEuNDguNDgsMCwwLDAsMC0uODhsLTctMy4xMi0yLjUyLTEuMTItMi40Ny0xLjFaIi8+CiAgICA8cGF0aCBjbGFzcz0iY2xzLTIiIGQ9Ik0yNC4zOSwxMi44OGwzLDEuMzNhLjYyLjYyLDAsMCwwLC41LDBsOS00YS42MS42MSwwLDAsMCwuMjYtLjlMMzEuODQsMS40YS42Mi42MiwwLDAsMC0xLDBMMjQuMTIsMTJBLjYxLjYxLDAsMCwwLDI0LjM5LDEyLjg4WiIvPgogICAgPHBhdGggY2xhc3M9ImNscy0yIiBkPSJNNzIuOTMsMjUuN2E0LjQ5LDQuNDksMCwwLDAtMi42NS42OCwyLjM0LDIuMzQsMCwwLDAtMSwyLDIuNjgsMi42OCwwLDAsMCwxLjM3LDIuMTksMTcuMzEsMTcuMzEsMCwwLDAsMy42NiwxLjhBMTUuMSwxNS4xLDAsMCwxLDc3LjUzLDM0YTcsNywwLDAsMSwyLjIsMi40MSw3LjgsNy44LDAsMCwxLC44NywzLjg5LDcuMTEsNy4xMSwwLDAsMS0xLDMuNzYsNy4yOCw3LjI4LDAsMCwxLTMuMDksMi43NCwxMC42OCwxMC42OCwwLDAsMS00Ljg1LDEsMTYuMywxNi4zLDAsMCwxLTQuNjctLjY5QTE0LjU3LDE0LjU3LDAsMCwxLDYyLjYzLDQ1bDIuMjgtNEExMiwxMiwwLDAsMCw2OCw0Mi41N2E5LjIyLDkuMjIsMCwwLDAsMy4xNC42NSw1LjY1LDUuNjUsMCwwLDAsMi44NS0uNywyLjQxLDIuNDEsMCwwLDAsMS4yMi0yLjI2YzAtMS4zNi0xLjI4LTIuNTQtMy44Mi0zLjUzQTMzLjQ2LDMzLjQ2LDAsMCwxLDY3LjY2LDM1YTcuNiw3LjYsMCwwLDEtMi41OS0yLjM1QTYuNDYsNi40NiwwLDAsMSw2NCwyOC44MWE3LjA5LDcuMDksMCwwLDEsMi4yMy01LjQ2LDkuMDgsOS4wOCwwLDAsMSw1Ljk0LTIuMjUsMTUuNTQsMTUuNTQsMCwwLDEsNC44OS42NiwxNi44NSwxNi44NSwwLDAsMSwzLjgxLDEuODhsLTIsNEExMi44LDEyLjgsMCwwLDAsNzIuOTMsMjUuN1oiLz4KICAgIDxwYXRoIGNsYXNzPSJjbHMtMiIgZD0iTTEwOS44NSwyMC4xOFY0Ny41aC01LjE3VjM1TDk2LjEyLDQ3LjE0SDk2TDg3LjgxLDM1Ljc2VjQ3LjVoLTVWMjAuMThoMEw5Ni4xNSwzOWwxMy42My0xOC44NloiLz4KICAgIDxwYXRoIGNsYXNzPSJjbHMtMiIgZD0iTTEyNy40Niw0Mi4zNmgtOC43bC0yLjE0LDUuMTRoLTUuMWwxMS44NC0yNy4yOWguMjlMMTM1LjQ5LDQ3LjVoLTZaTTEyNS43NSwzOGwtMi41My02LjM4TDEyMC41OCwzOFoiLz4KICAgIDxwYXRoIGNsYXNzPSJjbHMtMiIgZD0iTTE0Mi4zNCwyMS4yNVY0Mi41aDEyLjM0djVIMTM3LjE3VjIxLjI1WiIvPgogICAgPHBhdGggY2xhc3M9ImNscy0yIiBkPSJNMTYyLjM0LDIxLjI1VjQyLjVoMTIuMzV2NUgxNTcuMTdWMjEuMjVaIi8+CiAgICA8cGF0aCBjbGFzcz0iY2xzLTEiIGQ9Ik0xOTEuMzQsNDcuNWwtNS41My04LjM1aC0zLjQ2VjQ3LjVoLTUuMTdWMjEuMjVoNy45NXE0Ljg5LDAsNy41OCwyLjM3YTguMTcsOC4xNywwLDAsMSwyLjcsNi40NywxMC4zOCwxMC4zOCwwLDAsMS0xLDQuNTVBNy41NCw3LjU0LDAsMCwxLDE5MS4yNywzOGw2LjEsOS41M1ptLTktMTMuMzRIMTg2YTMuNjQsMy42NCwwLDAsMCwzLjA2LTEuMjMsNC4yLDQuMiwwLDAsMCwxLTIuNjYsNS4xMSw1LjExLDAsMCwwLS44Mi0yLjczYy0uNTUtLjg3LTEuNjEtMS4zLTMuMTgtMS4zaC0zLjcxWiIvPgogICAgPHBhdGggY2xhc3M9ImNscy0xIiBkPSJNMjIwLjYyLDIxLjI1bC05Ljg0LDE4LjA4VjQ3LjVoLTUuMTdWMzkuMzNsLTkuNjctMTguMDhoNi4zOGw2LDEyLjEyLDUuODktMTIuMTJaIi8+CiAgICA8cGF0aCBjbGFzcz0iY2xzLTEiIGQ9Ik0yNDAuMzEsMjEuMjV2NUgyMjcuNTd2NS42aDExLjI3djVIMjI3LjU3VjQyLjVoMTMuMjR2NUgyMjIuNFYyMS4yNVoiLz4KPC9zdmc+";
}
