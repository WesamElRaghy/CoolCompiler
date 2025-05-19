# Cool Compiler Project

## Project Overview
The Cool Compiler is a full-featured compiler for the Cool programming language, implementing the complete compilation pipeline from source code to optimized assembly output. This project demonstrates core compiler concepts and techniques, including lexical analysis, parsing, semantic checks, intermediate code generation, and machine code optimization.

## Key Features

- **Lexical Analysis:** Utilizes an ANTLR4-based lexer to tokenize source code into meaningful symbols such as keywords, identifiers, literals, and operators.

- **Syntax Analysis:** Parses token streams against the Cool language grammar, validating syntax and building parse trees.

- **Abstract Syntax Tree (AST) Construction:** Transforms parse trees into a simplified AST representing the hierarchical program structure, facilitating further semantic processing.

- **Semantic Analysis:** Performs comprehensive type checking, scope resolution, inheritance verification, and semantic error detection to ensure code correctness.

- **Intermediate Code Generation:** Produces three-address code (TAC), serving as an intermediate representation that bridges high-level constructs and low-level code.

- **Machine Code Generation and Optimization:** Translates TAC into efficient assembly code, applying optimizations such as constant folding and dead code elimination for improved performance.

## Usage Instructions

1. Build the compiler project using your preferred build system.

2. Provide Cool language source files as input to the compiler.

3. The compiler generates optimized assembly code as output, which can be assembled and linked to produce executable programs.

## Dependencies

- **ANTLR4:** Required for lexer and parser generation and runtime support.

- **Java Runtime Environment:** Needed to run ANTLR tools if applicable.

- **Graphviz:** Used to visualize the AST from DOT files.

---



