# Security Policy

## Supported Versions

Security fixes are handled on the latest published plugin version.

Please update to the latest JetBrains Marketplace version before reporting a security issue.

## Reporting A Vulnerability

Please do not publish security vulnerabilities in a public issue.

Report sensitive issues privately to the maintainer:

```text
thibaultpernel@gmail.com
```

Include:

- Plugin version
- IDE name and version
- Operating system
- Runtime information from Help -> About
- Clear reproduction steps
- Impact and expected risk
- Relevant logs with secrets removed

## Sensitive Data

The plugin can send selected editor text to Perplexity when you explicitly use a send action.

Do not send secrets, credentials, private keys, access tokens, or private customer data.

The Perplexity session token should be treated as sensitive. Keep it private and remove it from logs or screenshots before sharing diagnostics.
