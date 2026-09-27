# Synthetic University

New to this repo: read [AGENTS.md](AGENTS.md) before changing code.

Aldermere University is a fictional, seed-deterministic world for a later centralized multi-agent system. This repository generates the world, stores it in an embedded database, checks invariants, and exports ground truth. It does not implement the agents, retrieval, or a Gmail/Microsoft adapter.

Requirements: Java 21.

```bash
./gradlew generateUniversity --args="--seed 42 --profile small"
./gradlew validateUniversity
./gradlew exportUniversity --args="--seed 42 --profile small"
./gradlew test
```

Profiles are `small`, `medium`, and `large` in `config/profiles`. Counts live only in those files. The database file is `data/university`. Exports go to `exports/<profile>-seed<seed>/`. Organization charts are written to `docs/university-overview.md` and `docs/university-relationships.md`.

The same profile and seed produce the same rows. Email and document prose is the only seeded variation. Foreign keys, roles, visibility, and participants do not depend on a language model.

People reach the world through a university account and mailbox. The assistant mailbox is `assistant@aldermere.edu`, owned by a service principal. Message text is not identity. `checkPermission` denies by default; administrators are not omnipotent.

Security fixtures are ordinary email and document content marked `UNTRUSTED_CONTENT`. Nothing in that text is executed.

The benchmark that scores a future mail assistant is separate from this world. It does not implement the assistant.

```bash
./gradlew benchmark --args="--profile smoke --mas oracle --university-seed 42 --dataset-seed 1"
```

Profiles are `smoke`, `small`, `standard`, `stress`, `security`, and `complex`. Reports land in `exports/benchmark/`. The task view and the ground truth are written to different files. See `docs/benchmark-usage.md`.
