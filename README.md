[![License](https://img.shields.io/badge/license-EUPL-green)](https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12)

# VTL E&E

An engine and editor for the 
[Validation and Transformation Language](https://sdmx.org/?page_id=5096), 
written in Java, Javascript and R.

## Usage and documentation

Usage info, documentation and examples are available at the
[project site](http://vpinna80.github.io/VTL/).

## Build information

To build this project, you will need:

* A Windows or Linux machine
* JDK 8
* [Apache Maven](https://maven.apache.org/) 3.6.3
* [GNU R](https://www.r-project.org/) 3.5.3
* Configured internet connection (to download [node.js](https://nodejs.org/))

To build the project, launch the command:

    mvn [-P with-r] [-Dsdmx.version=x.x.x] clean package

Each artifact will be generated inside the `target` folder of each module.

If you want to build the editor and the R package along the engine, activate the 
`with-r` maven profile; you may need to configure your internet connection in 
Maven. The R package, ready for installation in R (with install.packages), will 
be located there.

If you want to use a different version of the 
[SDMX connectors](https://github.com/amattioc/SDMX.git) dependency, change 
the relative property value. The current default version is 2.3.3. [Check 
here](https://search.maven.org/artifact/it.bancaditalia.oss/sdmx) the available maven releases.

## Project Status

The Project is an ongoing development effort at Bank of Italy, with the 
purpose of demonstrating the feasibility and usefulness of VTL in a real 
scenario, such as translating validation rules exchanged by entities 
operating in the ESCB.

Thus, some VTL operators and statements offer only limited functionality,
and some have to be implemented yet.

The following map shows the current implementation status:
![statusmap](https://vpinna80.github.io/VTL/images/VTL.png)

## Copyright notice

This software is a copyright of Bank of Italy, 2019-2020.
The software is distributed under the European Public Licence v1.2.
