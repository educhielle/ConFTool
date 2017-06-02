# ConFTool

ConFTool is a configurable architecture-independent tool that applies state of the art SIHFT techniques [\[1\]](#references) to targeted programs by modifying their assembly code.

Potential uses are:
* Provide low-cost reliability to microprocessors against soft errors
* Increase the observability of permanent faults in post-silicon validation
* Increase the probability of detecting hard errors in deployed devices

This project is the evolution of [\[2\]](#references).

## Contents

* [Development Goals](#development-goals)
* [References](#references)

## Development Goals

#### New Configuration Files

Implementing a new format in the configuration files will provide more control of the code to the techniques. In addition, the code will be cleaner, once fewer exceptions need to be added.

#### New Input Files

Add capacity of receiving only a disassembly or a binary file as input. By getting directly from the binary file, we don't need to worry that much with the assembler.

#### Scripting Techniques

SIHFT techniques don't need to be hard code in the tool. They can be loaded from script files.

## References
[1] O. Goloubeva et al., "Software-Implemented Hardware Fault Tolerance," Boston, MA: Springer Sci.+Bus. Media, LLC, 2006.
[2] E. Chielle et al., "Configurable tool to protect processors against SEE by software-based detection techniques," Proc. 13th Latin American Test Workshop, Quito: 2012, pp. 1-6.

[top](#conftool)
