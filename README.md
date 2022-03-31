# Hotfix for Spring RCE 2022.3.31

## Usage

1. Backup your application.
2. Download `spring-rce-2022-03-hotfix-all.jar` from GitHub release or build from source code.
3. `java -jar spring-rce-2022-03-hotfix-all.jar <the jar or directory to be patched>`

## How it works

It uses bytecode manipulation to replace vulnerable Spring `org.springframework.validation.DataBinder.getDisallowedFields()` with:

```
    String[] fallback = PropertyAccessorUtils.canonicalPropertyNames(new String[]{"class.*", "Class.*", "*.class.*", "*.Class.*"});
    if (this.disallowedFields == null) {
        return fallback;
    } else {
        String[] ret = new String[disallowedFields.length + fallback.length];
        System.arraycopy(disallowedFields, 0, ret, 0, disallowedFields.length);
        System.arraycopy(fallback, 0, ret, disallowedFields.length, fallback.length);
        return ret;
    }
```
