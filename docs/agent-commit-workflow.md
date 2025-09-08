## Agent Task: Commit local changes following repository style

Short alias: Agent task: commit changes

### Goal
Stage and commit local changes, using the existing conventional commit style in this repository.

### Steps
1) Review current changes
```bash
git --no-pager status --porcelain=v1 | cat
git --no-pager diff --name-only | cat
git --no-pager diff --stat | cat
```

2) Inspect recent commit message style
```bash
git --no-pager log --oneline -n 8 | cat
```
- Follow the established conventional format: `type(scope): summary`
- Common types: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`
- Example scopes seen here: `graphql`, `config`, `client`, `observability`, `security`, `testing`

3) Stage the changes
- Specific files:
```bash
git add <file1> <file2>
```
- Or all changes:
```bash
git add -A
```

4) Commit with a matching message
```bash
git commit -m "<type>(<scope>): <concise summary>"
```
Examples:
```bash
git commit -m "feat(graphql): Extend task update flow; align controller/service"
git commit -m "docs(process): Add agent commit workflow task"
```

5) Verify the commit
```bash
git --no-pager log -1 | cat
```

6) (Optional) Push if requested
```bash
git push
```

### Notes
- Keep summaries short and imperative.
- Use `cat` on commands that might page output to ensure non-interactive runs.
- If only a subset of files should be committed, prefer explicitly listing them in `git add`.
