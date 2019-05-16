# querier

A simple Clojure library designed for building SQL queries for a variety of
SQL platforms from custom DSL.

## Usage

Examples:

````$clojure
(generate-sql :postgres test_fields {:where [:= [:field 3] nil]})
(generate-sql :mysql test_fields {:where [:= [:field 2] "cam"], :limit 10})
(generate-sql :sqlserver "fieeeeldz" limit_only_query)
(generate-sql :mysql test_fields
    {:where [:and [:< [:field 1] 5] [:macro "is_joe"]]}
    {
     "is_joe" [:= [:field 2] "joe"]
     "is_foo" [:= [:field 2] "foo"]
    }
)
````

To start development environment run `make up`. If you want to 
access environment run `make ssh-querier`, if you just want to
run tests run `make run-tests`. See `Makefile` for raw commands.

Supports PostgreSQL, MySQL and SQL Server with `:where` and `:limit` statements,
with an optional `:macro` part of query for custom macros.

Will throw `Unsupported SQL dialect` if unsupported dialect is used and 
`Circular macros detected` if you defined a circularly dependant macros.

Circular dependencies are calculated with [Kahn's algorithm](https://en.wikipedia.org/wiki/Topological_sorting#Kahn's_algorithm).

## License

Copyright © 2019 FIXME

Distributed under the MIT License (see LICENSE) unless otherwise mentioned 
(see src/querier/kahn.clj).
