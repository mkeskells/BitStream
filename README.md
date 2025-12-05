# BitStream

A Java library for efficient bit stream operations and manipulations. This library provides a set of tools for working with streams of bits, 
with support for various logical operations like AND and OR operations between multiple streams.

## Project Structure

The project is organized into multiple Maven modules:

- **bitstream-core**: Core structures
- **bitstream-writer**:
  - Implementation of a writer for bit streams
- **bitstream-reader**: 
  - immutable data, thread safe
  - Reader implementations for bit streams, to read and manage the structure
  - Stream nodes for logical operations (AND, OR), to provide a query like reader interface

## Features

- Efficient bit stream operations focussed on small memory footprint
- Stream composition using logical operation - AND/OR nodes etc
- Support for different bit container implementations
- Skip-ahead functionality for performance optimization
- Thread-safe operations
- Stream based processing model

## Getting Started

### Prerequisites

- Java 25 or higher
- Maven 3.6 or higher

### Building the Project

```bash
mvn clean install
```

### Usage Example

```java
// Create bit containers
BitContainer container1 = new SimpleReadableBitContainer(...);
BitContainer container2 = new SimpleReadableBitContainer(...);

// Create a combined OR stream, or some combination of the streams
var query = new OrStreamNode(
    container1.biterator(),
    container2.biterator()
);

// Process the stream
query.stream().forEach(...)
```

## Module Details

### bitstream-core
Contains common structure and definitions

### bitstream-reader

Contains the fundamental building blocks:

- `BitContainer`: Interface for bit storage
- `BitContainerStream`: Stream wrapper for bit containers
- `Block`: contains a representation of bits stored
- Various block implementations, based on bitmaps, runs, points etc

- Provides stream processing capabilities:

- `StreamNode`: Interface for stream operations
- `AndStreamNode`: Performs logical AND between multiple streams
- `OrStreamNode`: Performs logical OR between multiple streams

## Contributing

Contributions are welcome! Please feel free to submit pull requests.

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

Copyright (C) 2025 BitStream Contributors

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.
