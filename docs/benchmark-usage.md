# Benchmark usage

Java 21. From the repository root:

```bash
./gradlew benchmark --args="--profile smoke --mas oracle --university-seed 42 --dataset-seed 1"
./gradlew test
```

`--profile` is `smoke`, `small`, `standard`, `stress`, `security`, or `complex`. Files live in `config/benchmark/`.

`--mas` is `oracle`, `random`, `unsafe`, `hallucinating`, `retrieval-only`, `no-rag`, or `no-tool`.

`--university-profile` defaults to `small` and selects `config/profiles/<name>.json`. `--university-seed` regenerates that world in memory. It does not overwrite `data/university`.

The report is written to:

```text
exports/benchmark/<profile>-u<universitySeed>-d<datasetSeed>-<mas>/
    observations.jsonl
    ground-truth.jsonl
    report.json
    report.md
```

`observations.jsonl` is the only task view a system under test should receive. `ground-truth.jsonl` is for the evaluator and for audit.

A new assistant implements `edu.aldermere.benchmark.MasUnderTest`:

```java
ExecutionResult execute(TaskObservation observation, ToolGateway tools);
```

Do not implement `Baseline` for that assistant. `Baseline` is the validation hook that can see `BenchmarkTask`, including ground truth.

The gateway's retrieved ids, tool calls, errors, and send count replace whatever the agent reports. Claims, draft state, reply label, token counts, and `delegations` are taken from the returned `ExecutionResult`.
