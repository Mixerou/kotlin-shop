# Kotlin Shop

> This project built to simply try out the Kotlin Lang.

CLI tool that replays a log of inventory operations
from one file and writes the resulting stock balance to another.

## Content

- [Logging](#logging)
- [Development](#development)
  - [Essential Tasks](#essential-tasks)
- [License](#license)

## Logging

We're using `kotlin-logging` with `slf4j-simple` for logging.
So, if you want to change a log level,
you can do it in the [`simplelogger.properties`](./app/src/main/resources/simplelogger.properties)

## Development

### Essential Tasks

Use `gradlew` or `gradlew.bat`, depending on your OS.

```shell
# Start CLI
$ ./gradlew run --args='--help'

# Run Tests
$ ./gradlew test

# Prepare for Distribution
$ ./gradlew assembleDist
```

## License

This project (`kotlin-shop`) is available under the MIT license.
See the [LICENSE](./LICENSE) file for more info.
