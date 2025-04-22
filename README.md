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
         - Variable: a                    | Register: 2   
         - Variable: b                    | Register: 2   
         - Variable: c                    | Register: 2   
         - Variable: d                    | Register: 2   
         - Variable: arg                  | Register: 1   
         - Variable: this                 | Register: 0   
         - Variable: tmp1                 | Register: 2   
         - Variable: tmp0                 | Register: 2   
    
    Method: main
         - Variable: args                 | Register: 0   
    ```



