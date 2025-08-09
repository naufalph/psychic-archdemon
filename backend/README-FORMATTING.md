# Code Formatting with Spotless

This project uses **Spotless** for automatic code formatting, similar to Prettier for JavaScript. Spotless ensures consistent code style across the entire codebase.

## What Gets Formatted

- **Java files**: Formatted using Google Java Format
- **Import statements**: Organized and unused imports removed
- **POM files**: Sorted and cleaned

## Maven Commands

### Check if code is properly formatted
```bash
mvn spotless:check
```
This command will fail if any files need formatting.

### Apply formatting to all files
```bash
mvn spotless:apply
```
This command automatically formats all Java files and sorts POM files.

### Format and run tests
```bash
mvn spotless:apply test
```

## Automatic Formatting on Commit

### Setup Pre-commit Hook
Run this command from the project root to set up automatic formatting on every commit:

```bash
./setup-hooks.sh
```

This creates a Git pre-commit hook that:
- Automatically formats code before each commit
- Adds formatted files back to the staging area
- Ensures no unformatted code enters the repository

### Bypass Hook (Emergency Only)
If you need to commit without formatting (not recommended):
```bash
git commit --no-verify
```

## IDE Integration

### IntelliJ IDEA
1. Install the "google-java-format" plugin
2. Enable it in Settings → google-java-format Settings
3. Set code style to "Google Style"

### VS Code
1. Install the "Language Support for Java" extension
2. Configure the Java formatter to use Google style:
   ```json
   {
     "java.format.settings.profile": "GoogleStyle"
   }
   ```

## Configuration

The Spotless configuration is defined in `pom.xml`:
- **Google Java Format** with GOOGLE style
- **Import ordering**: `java|javax`, `org`, `com`, others
- **POM sorting** with expanded empty elements disabled

## Build Integration

Formatting checks run automatically during the `validate` phase of Maven builds. This means:
- `mvn compile` will fail if code isn't formatted
- CI/CD pipelines will catch formatting issues early
- No unformatted code can be built for production

## Troubleshooting

### "Command 'mvn' not found"
Make sure Maven is installed and in your PATH.

### Formatting fails on specific files
1. Check that the file is valid Java syntax
2. Try running `mvn spotless:apply` on individual files
3. Check the Maven output for specific error messages

### Pre-commit hook not working
1. Ensure the hook file is executable: `chmod +x .git/hooks/pre-commit`
2. Verify you're committing from the project root directory
3. Check that the backend directory exists with a valid pom.xml

## Benefits

✅ **Consistent code style** across the entire team  
✅ **No more formatting discussions** in code reviews  
✅ **Automatic cleanup** of imports and whitespace  
✅ **CI/CD integration** prevents unformatted code from being deployed  
✅ **Zero configuration** for new developers