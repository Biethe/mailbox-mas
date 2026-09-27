# Installed skills

Skills on this laptop. When a task matches one, read its `SKILL.md` and follow it. Do not apply a design or deploy skill to the university or benchmark unless the user asked for that work.

This repository is a Java world plus a benchmark. For code changes here, Ponytail applies: simplest thing that works, no extra frameworks.

## Ponytail

Simplest code that works. Default intensity is full. Say "ponytail lite" or "ponytail ultra" to change it. Stop with "stop ponytail".

| Skill | Use when |
| --- | --- |
| `ponytail` | Any coding task. Prefer the standard library, delete speculative abstractions, keep the diff short. |
| `ponytail-review` | Review a diff only for over-engineering: what to delete, and what replaces it. |
| `ponytail-audit` | Same review across the whole repo, not just a diff. |
| `ponytail-debt` | List `ponytail:` comments, the shortcuts left for later. |
| `ponytail-gain` | Show Ponytail's published savings scoreboard. Not a number for this repo. |
| `ponytail-help` | List Ponytail modes and commands. |

Paths: `~/.claude/plugins/cache/ponytail/ponytail/4.10.0/skills/<name>/SKILL.md`

## Design

Use only when the user wants visual design, a landing page, or generated images. Not for this Java project.

| Skill | Use when |
| --- | --- |
| `design-taste-frontend` | Landing pages, portfolios, redesigns that should not look templated. Current default. |
| `design-taste-frontend-v1` | A project that depends on the old taste skill exactly. |
| `high-end-visual-design` | Fonts, spacing, shadows, and motion that should feel expensive. |
| `gpt-taste` | Editorial layout, GSAP scroll scenes, wide type. |
| `minimalist-ui` | Warm monochrome, flat grids, no gradients or heavy shadows. |
| `industrial-brutalist-ui` | Mechanical, print-like dashboards and editorial sites. |
| `redesign-existing-projects` | Upgrade an existing UI without breaking behavior. |
| `web-design-guidelines` | Review UI against the web interface guidelines. |
| `writing-guidelines` | Review prose against the writing handbook. |
| `imagegen-frontend-web` | One horizontal design image per page section. |
| `imagegen-frontend-mobile` | Mobile screen concepts inside a phone frame. Images only. |
| `image-to-code` | Generate section images, then implement the site to match them. |
| `brandkit` | Brand boards, logo systems, identity decks. |
| `stitch-design-taste` | A `DESIGN.md` for Google Stitch. |
| `full-output-enforcement` | A task that must be generated in full, with no placeholders. |

Paths: `~/.claude/skills/<name>/SKILL.md` and `~/.agents/skills/<name>/SKILL.md` for the two guideline skills.

## Documents

| Skill | Use when |
| --- | --- |
| `docx` | Create or edit a Word file (`.docx`, `.dotx`). |
| `pptx` | Create or edit a PowerPoint file (`.pptx`, `.potx`). |
| `xlsx` | A spreadsheet is the main input or output (`.xlsx`, `.csv`, `.tsv`). |
| `pdf` | Read, merge, split, fill, or create a PDF. |
| `docs` | The user asked for a shared living doc, page, or memo, and did not name a file format. |
| `morning` | The user asked to run or set up their morning brief, or typed `/morning`. |
| `import-memory` | Import a memory export from another assistant. |
| `skill-creator` | Create, edit, or evaluate a skill. |

Paths: `~/.claude/skills/synced/*/ <name>/SKILL.md`

## Vercel

| Skill | Use when |
| --- | --- |
| `deploy-to-vercel` | Deploy and return a link. |
| `vercel-cli-with-tokens` | Vercel CLI with an access token instead of interactive login. |
| `vercel-optimize` | Cut Vercel cost or speed up a deployed app, using metrics first. |
| `vercel-react-best-practices` | Performance while writing React or Next.js. |
| `vercel-react-native-skills` | React Native or Expo performance. |
| `vercel-react-view-transitions` | React View Transition API, without a third-party animation library. |
| `vercel-composition-patterns` | Component APIs that have grown too many boolean props. |
| `find-skills` | The user asks whether a skill exists for a job. |

Paths: `~/.agents/skills/<name>/SKILL.md`

## Cursor

Built in. Open `~/.cursor/skills-cursor/<name>/SKILL.md`.

| Skill | Use when |
| --- | --- |
| `create-rule` | Add a Cursor rule or `AGENTS.md`. |
| `create-skill` | Author a Cursor skill. |
| `create-hook` | Add a hook in `hooks.json`. |
| `create-subagent` | Add a custom subagent. |
| `canvas` | A live React canvas beside the chat for a durable analysis. |
| `visualize` | A compact chart or diagram inside the chat. |
| `automate` | Create a Cursor Automation. |
| `loop` | Run a prompt on a timer (`/loop`). |
| `goal` | Set a goal Cursor should pursue to completion. |
| `autopilot` | Keep a PR merge-ready: comments, conflicts, CI. |
| `review` | Bugbot or security review of local changes. |
| `review-bugbot` | Bugbot review only. |
| `review-security` | Security review only. |
| `split-to-prs` | Split current work into smaller PRs. |
| `share` | Save or share the project on Cursor. |
| `new-repo` | Create a Cursor-hosted repo and push. |
| `origin` | Install or repair the Cursor origin CLI. |
| `sdk` | Build on the Cursor SDK (`@cursor/sdk` or `cursor-sdk`). |
| `update-cursor-settings` | Change editor `settings.json`. |
| `update-cli-config` | Change Cursor CLI config. |
| `statusline` | Custom CLI status line. |
| `onboard` | `/onboard` |
| `rename-chat` | `/rename-chat` |
| `shell` | `/shell` runs the rest of the message as a shell command. |
| `migrate-to-skills` | Convert always-on rules and slash commands into skills. |
