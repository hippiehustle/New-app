# Contributing to MiniCount

Thank you for your interest in contributing to MiniCount! This document provides guidelines for contributing to the project.

## Getting Started

1. Fork the repository
2. Clone your fork: `git clone https://github.com/YOUR_USERNAME/MiniCount.git`
3. Create a feature branch: `git checkout -b feature/amazing-feature`
4. Make your changes
5. Commit: `git commit -m 'Add amazing feature'`
6. Push: `git push origin feature/amazing-feature`
7. Open a Pull Request

## Development Setup

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 17
- Android SDK with API 34
- Git

### Building
```bash
./gradlew assembleDebug
```

### Running Tests
```bash
./gradlew test
./gradlew connectedAndroidTest
```

## Code Style

- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add comments for complex logic
- Keep functions small and focused
- Use Jetpack Compose best practices

## Commit Messages

Follow conventional commits:
- `feat:` New feature
- `fix:` Bug fix
- `docs:` Documentation changes
- `style:` Code style changes (formatting)
- `refactor:` Code refactoring
- `test:` Adding tests
- `chore:` Maintenance tasks

Example: `feat: add widget background customization`

## Pull Request Process

1. Update README.md if needed
2. Ensure all tests pass
3. Update documentation
4. Describe changes in PR description
5. Link related issues

## Feature Requests

- Open an issue with the "enhancement" label
- Describe the feature clearly
- Explain the use case
- Add mockups if applicable

## Bug Reports

Include:
- Device model and Android version
- Steps to reproduce
- Expected behavior
- Actual behavior
- Screenshots if applicable
- Crash logs

## Code Review

All submissions require review. We use GitHub pull requests for this purpose.

## Community

- Be respectful and inclusive
- Help others learn
- Give constructive feedback
- Follow our Code of Conduct

## License

By contributing, you agree that your contributions will be licensed under the same license as the project.

## Questions?

Feel free to open an issue or email: dev@minicount.app
