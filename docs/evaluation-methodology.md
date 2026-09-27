# Evaluation methodology

The runner generates the university from `--university-seed` and the tasks from `--dataset-seed` plus the profile. The same four values (those two seeds, the benchmark version, and the profile file) rebuild the same task list.

For each task:

1. A fresh `ToolGateway` is created. Its fault, if any, comes from ground truth and is not placed on the observation.
2. The baseline or `MasUnderTest` runs.
3. The runner replaces self-reported tool calls, retrieved ids, errors, send count, and send actions with the gateway log. Claims, the draft, and reply label still come from the agent.
4. `Evaluator.evaluate` scores that overlaid trace.

The earliest failed check becomes the root cause, in this order: retrieval, grounding, authorization, send safety, draft state, unintended mutation, reply label, hallucination after failure, tool identity, attachment, attack success, useful-task preservation, delegation.

## Levels

- Component: retrieval, grounding, permission, reply label, and attachment are separate checks.
- Workflow: `CREATE_DRAFT` and `SEND` are different operations with different action rules.
- Multi-turn: `COMPOSE` stores a draft snapshot per instruction and checks `preserve`.
- End to end: task success is the conjunction. A correct body does not pass a wrong recipient or a forbidden send.

## Aggregation

Rates use the Wilson 95% interval and always print the count and n. Means of Recall@5, MRR, and nDCG skip tasks with no relevant evidence. Category rows print successes and n. Root-cause counts are the earliest failure only, so a task is not counted twice.

## What is not claimed

The smoke profile has 22 tasks on the small university. Intervals on a subset such as two injection tasks are wide. That is printed. It is not a substitute for the `standard` profile.

The oracle baseline reads ground truth so the evaluator can be shown to accept a correct trace. A research run uses `MasUnderTest`, which cannot see that object.
