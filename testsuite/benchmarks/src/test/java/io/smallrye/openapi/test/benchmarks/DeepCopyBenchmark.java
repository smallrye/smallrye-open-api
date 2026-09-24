package io.smallrye.openapi.test.benchmarks;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.openapi.api.SmallRyeOpenAPI;
import io.smallrye.openapi.model.BaseModel;

@BenchmarkMode(Mode.SingleShotTime)
@Fork(5)
@Warmup(iterations = 5, time = 1, batchSize = 8192)
@Measurement(iterations = 5, time = 1, batchSize = 8192)
@State(Scope.Benchmark)
public class DeepCopyBenchmark {

    private OpenAPI openAPI;

    @Setup(Level.Trial)
    public void setup() {
        openAPI = SmallRyeOpenAPI.builder()
                .withConfig(new SmallRyeConfigBuilder()
                        .build())
                .withCustomStaticFile(() -> this.getClass().getClassLoader().getResourceAsStream("petstore.json"))
                .enableModelReader(false)
                .enableStandardStaticFiles(false)
                .enableAnnotationScan(false)
                .enableStandardFilter(false).build().model();
    }

    @Benchmark
    public OpenAPI deepCopyOpenAPI() {
        return BaseModel.deepCopy(openAPI, OpenAPI.class, false);
    }
}
