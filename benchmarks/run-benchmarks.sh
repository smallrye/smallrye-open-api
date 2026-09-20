#!/bin/bash

VERSIONS="HEAD"
BENCHMARKS=".*"

help() {
  echo "Usage: run-benchmarks.sh [--versions <versions>] [--benchmarks <benchmarks>] [--help]"
  echo
  echo "  --versions <versions>: comma-separated list of smallrye-openapi versions (Git commits) to benchmark"
  echo "                         defaults to 'HEAD'"
  echo
  echo "  --benchmarks <benchmarks>: comma-separated list of regexps that select the benchmarks to run"
  echo "                             defaults to '.*'"
  echo
  echo "  --help: print this text"
}

while [[ $# -gt 0 ]] ; do
  case $1 in
    --help)
      help
      exit
      ;;
    --versions)
      VERSIONS="${2//,/ }"
      shift
      shift
      ;;
    --benchmarks)
      BENCHMARKS="$2"
      shift
      shift
      ;;
    *)
      help
      exit 1
      ;;
  esac
done

echo "Running benchmarks $BENCHMARKS against smallrye-openapi versions:"
for VERSION in $VERSIONS ; do
  echo "- $VERSION"
done

ROOT_DIR=$(git rev-parse --show-toplevel)
for VERSION in $VERSIONS ; do
  TEMP=$(mktemp --tmpdir -d smallrye-openapi.XXXXXXXXXX)
  git -C $ROOT_DIR archive $VERSION | tar -x -C $TEMP
  pushd $TEMP
  GROUP_ID=$(mvn org.apache.maven.plugins:maven-help-plugin:3.2.0:evaluate -q -DforceStdout -Dexpression=project.groupId)
  mvn -q versions:set -DnewVersion=1.0.0-dev-SNAPSHOT
  mvn -q versions:commit
  mvn -q clean install -T 1C -DskipTests -Dinvoker.skip -Dformat.skip -Dimpsort.skip -pl '!io.smallrye:smallrye-open-api-testsuite-data,!io.smallrye:smallrye-open-api-testsuite-extra,!io.smallrye:smallrye-open-api-testsuite-coverage,!io.smallrye:smallrye-open-api-testsuite-tck,!io.smallrye:smallrye-open-api-gradle-plugin,!io.smallrye:smallrye-open-api-maven-plugin'
  popd
  rm -r $TEMP

   mvn test-compile jmh:benchmark -Djmh.benchmarks=$BENCHMARKS -Djmh.rf=json -Djmh.rff=target/results-$VERSION.json -DgroupId.smallrye-openapi=$GROUP_ID -Dversion.smallrye-openapi=1.0.0-dev-SNAPSHOT
#  mvn test-compile jmh:benchmark "-Djmh.prof=perf" -Djmh.benchmarks=$BENCHMARKS -Djmh.rf=json -Djmh.rff=target/results-$VERSION.json -DgroupId.smallrye-openapi=$GROUP_ID -Dversion.smallrye-openapi=1.0.0-dev-SNAPSHOT
#  mvn test-compile jmh:benchmark "-Djmh.prof=perfasm:saveLog=true;hotThreshold=0.01" -Djmh.benchmarks=$BENCHMARKS -Djmh.rf=json -Djmh.rff=target/results-$VERSION.json -DgroupId.smallrye-openapi=$GROUP_ID -Dversion.smallrye-openapi=1.0.0-dev-SNAPSHOT
#  mvn test-compile jmh:benchmark "-Djmh.prof=async:output=flamegraph;libPath=/home/ladicek/software/async-profiler/build/libasyncProfiler.so" -Djmh.benchmarks=$BENCHMARKS -Djmh.rf=json -Djmh.rff=target/results-$VERSION.json -DgroupId.smallrye-openapi=$GROUP_ID -Dversion.smallrye-openapi=1.0.0-dev-SNAPSHOT
#  mvn test-compile jmh:benchmark "-Djmh.prof=async:event=alloc;rawCommand=total;output=flamegraph;libPath=/home/ladicek/software/async-profiler/build/libasyncProfiler.so" -Djmh.benchmarks=$BENCHMARKS -Djmh.rf=json -Djmh.rff=target/results-$VERSION.json -DgroupId.smallrye-openapi=$GROUP_ID -Dversion.smallrye-openapi=1.0.0-dev-SNAPSHOT
#  mvn test-compile jmh:benchmark "-Djmh.prof=gc" -Djmh.benchmarks=$BENCHMARKS -Djmh.rf=json -Djmh.rff=target/results-$VERSION.json -DgroupId.smallrye-openapi=$GROUP_ID -Dversion.smallrye-openapi=1.0.0-dev-SNAPSHOT
done

# this requires current smallrye-openapi workspace to be built and installed to local Maven repo (`mvn clean install -DskipTests`)
RESULTS=$(find target -type f -name "results-*.json" -print0 | tr '\0' ',' | sed -e 's/,$//')
mvn compile exec:java -Dexec.mainClass=io.smallrye.openapi.chart.ChartGenerator -Dexec.arguments="$RESULTS"
