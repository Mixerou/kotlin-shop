# Kotlin Shop

> This project is built to simply try out the Kotlin Lang.

CLI tool that replays a log of inventory operations
from one file and writes the resulting stock balance to another.

## Content

- [Usage](#usage)
- [How it works](#how-it-works)
  - [Input](#input)
  - [Output](#output)
  - [Assumptions](#assumptions)
  - [Sale Allocation](#sale-allocation)
  - [Debts](#debts)
  - [Example](#example)
- [Logging](#logging)
- [Development](#development)
  - [Essential Tasks](#essential-tasks)
- [License](#license)

## Usage

```shell
Usage: app [<options>]

Options:
  -i, --input=<path>   Input CSV file path
  -o, --output=<path>  Output CSV file path
  -h, --help           Show this message and exit
```

To generate an input file, you can you the transactions generator:

```shell
Usage: scripts/transarator228.main.kts <outputFile> [transactions] [seed]
Defaults: transactions=1000, seed=339
```

## How it works

The input file is a chronological log of inventory transactions.
One transaction per line, `;`-separated, no header.
The CSV line type depends on the number of fields for both input and output.

### Input

- `RECEIPT` (`<groupId>;<itemId>;<count>`) adds `count` items to a group
- `SALE` (`<groupId>;<count>`) removes `count` items from a group

### Output

- `GROUP_WITH_ITEMS` (`<groupId>;<itemId>;<count>`) shows the number of items in a group
- `GROUP_WITH_DEBT` (`<groupId>;-<count>`) show debt for a group without any item

### Assumptions

- A receipt that tries to attach an already known item to a different group is treated as a bad line
- Counts in `input.csv` must be integers `> 0`
- Counts are 32-bit integers (`Int` in Kotlin)
- An item id belongs to exactly one group
- Ranking is a plain string comparison (`10` before `2` and `Z` before `a`)
- Input is expected to be UTF-8
- Output is a final snapshot after the whole transactions

### Sale Allocation

Sales apply to groups, not individual items. The program draws stock from items sorted alphabetically by ID.
Stock is taken from the highest-ranked item first, skipping any with zero or negative balances.
If an item runs out, the tool moves to the next.
If total group stock is not enough, the last item will be charged, making its balance negative.

### Debts

`Group Debt`

Sales from an empty group create group-level debt. The new receipt burns this debt first.

`Item Debt`

Negative balances from overselling a specific item are different.
They can only be restored by receipts into that same item.

### Example

`input.csv`:

```
1;100;10
1;200;5
1;12
2;1;3
2;5
1;100;4
3;9
```

`output.csv`:

```
1;100;4
1;200;3
2;1;-2
3;-9
```

## Logging

We're using `kotlin-logging` with `slf4j-simple` for logging.
So, if you want to change a log level,
you can do it in the [`simplelogger.properties`](./app/src/main/resources/simplelogger.properties).

## Development

### Essential Tasks

Use `gradlew` or `gradlew.bat`, depending on your OS.

```shell
# Start CLI
$ ./gradlew run --args='-i ../examples/input.csv -o ../examples/output.csv'

# Run Tests
$ ./gradlew test

# Prepare for Distribution
$ ./gradlew assembleDist
```

## License

This project (`kotlin-shop`) is available under the MIT license.
See the [LICENSE](./LICENSE) file for more info.
