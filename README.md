# Compiler Project

Contains a reference implementation for the compiler project.


## Optimizations

### Register Allocation
The optimization reduces the number of local variables (JVM registers) by performing graph coloring-based register allocation using liveness analysis.

- By default, the variable `this` is always assigned to register 0 in non-static methods.
- Spilling to memory is **not supported**.

- In cases where the requested number of registers is **insufficient**, an **error** will be reported, indicating the minimum number of registers needed. Example:
     ```
     ERROR@optimization, line 0, col 0: Cannot allocate with 1 register(s). It needs at least 2 registers.
     ```

- When allocation is **successful**, a **log** will be generated, displaying the mapping of variables to registers for each method. Example:
     ```
     LOG@optimization, line 0, col 0: Successfully allocated with 2 registers
     Method: RegAlloc
          - Variable: this                 | Register: 0   
     Method: soManyRegisters
          - Variable: a                    | Register: 3   
          - Variable: b                    | Register: 2   
          - Variable: c                    | Register: 2   
          - Variable: arg                  | Register: 1   
          - Variable: this                 | Register: 0   
          - Variable: tmp0                 | Register: 2   
     Method: main
          - Variable: args                 | Register: 0
     ```

### Constant Propagation
This is an optimization technique where the compiler identifies variables that have constant values and replaces their occurrences with the constant value. 
This reduces the number of local variables used in the JVM and simplifies the code.

#### Example:
Input Code:
```
a = 10;
b = a + 5;
c = b + a;

After Constant Propagation:

a = 10;
b = 10 + 5;
c = b + 10;
```

Implementation:
- The [`ConstantPropagationVisitor`](/src/main/pt/up/fe/comp2025/optimization/ConstantPropagationVisitor.java) integer and boolean constants in a method.
- It replaces variable references with their constant values when possible.
- It invalidates constants when variables are reassigned or modified in loops or branches.

Key Features:
- Handles integer and boolean constants.
- Supports propagation across assignments and branches (`if` and `while` statements).

### Constant Folding
This is an optimization technique where the compiler evaluates expressions involving constant values at compile time and replaces them with their resulting value. 
This reduces runtime computation and simplifies the code.

#### Example:
Input Code:
```
a = 10 + 5;

After Constant Folding:

a = 15;
```

Implementation:
- The [`ConstantFoldingVisitor`](/src/main/pt/up/fe/comp2025/optimization/ConstantFoldingVisitor.java) evaluates binary expressions (+, -, *, /, etc.) involving integer or boolean literals.
- It replaces the expression with a single literal node containing the computed value.
- The optimization is applied iteratively until no further changes are made.

Key Features:
- Supports arithmetic and logical operations.
- Avoids division by zero by replacing such cases with a default value (e.g., 0).
- Simplifies the abstract syntax tree (AST) by replacing complex expressions with literals.