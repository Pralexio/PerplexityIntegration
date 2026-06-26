# Contributing

Thanks for your interest in improving Perplexity AI Chat for JetBrains IDEs.

## Before Opening An Issue

Please check existing issues first:

https://github.com/Pralexio/PerplexityIntegration/issues

For embedded browser problems, also check whether your IDE, operating system, and runtime officially support JCEF.

## Bug Reports

Include:

- IDE name and version
- Plugin version
- Operating system
- IDE runtime information from Help -> About
- Whether you use the bundled JBR or a custom runtime
- Steps to reproduce
- Expected behavior
- Actual behavior
- Error notification text, if any
- `idea.log` when the embedded browser fails

## Feature Requests

Please describe:

- The workflow you want to improve
- Why it matters
- How you expect the feature to behave
- Any relevant screenshots or examples

## Development

Read the development notes before changing code:

- [Development Notes](docs/DEVELOPMENT.md)
- [Troubleshooting](docs/TROUBLESHOOTING.md)

Useful commands:

```bash
./gradlew runIde
./gradlew buildPlugin
./gradlew test
./gradlew verifyPlugin
```

Generated plugin archives are written to:

```text
build/distributions/
```

## Pull Requests

Keep pull requests focused and small when possible.

Before opening a pull request:

1. Describe the user-visible behavior change.
2. Link related issues.
3. Mention the IDE versions tested.
4. Include screenshots for UI changes.
5. Update documentation when behavior changes.

Do not include generated build output or local IDE files.
