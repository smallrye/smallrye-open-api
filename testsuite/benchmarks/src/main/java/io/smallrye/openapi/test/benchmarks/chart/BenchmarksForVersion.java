package io.smallrye.openapi.test.benchmarks.chart;

import java.util.ArrayList;
import java.util.List;

import io.smallrye.openapi.test.benchmarks.json.BenchmarkDto;

public class BenchmarksForVersion {
    public final String version;
    public final List<BenchmarkDto> benchmarks = new ArrayList<>();

    public BenchmarksForVersion(String version) {
        this.version = version;
    }

    public void add(BenchmarkDto benchmark) {
        benchmarks.add(benchmark);
    }
}
