# CS536 Introduction to Programming Languages and Compilers - Programming Assignments
My programming assignments from the Fall 2018 semester. 


## Course Description
Introduction to the theory and practice of compiler design. Comparison of features of several programming languages and their implications for implementation techniques. Several programming projects required.

## Programming Assignments Overview
Each of these projects represent a part of a compiler for a small subset of the C++ language, cdull. Features of the cdull language can be found here: http://pages.cs.wisc.edu/~aws/courses/cs536-f18/asn/p2/p2.html

## proj1-part1: Symbol Table Test Suite
A test suite for the symbol table that is implemented in proj1-part2.

##proj1-part2: Symbol Table Implementation
An implementation of a symbol table which will be used in the later programs.

## proj2: Scanner
A program that tokenizes input. The lexical analyzer generator Jlex was used to write the scanner. 

## proj3: Parser
A program that parses the tokens returned from the scanner. This parser finds syntax errors and builds an abstract syntax tree for syntactically correct programs. An unparse method is also used in order to "pretty print" the parsed program.

## proj4: Name Analyzer
A program that implements name checking by building symbol tables, identifies invalid usage and links identifier uses with their corresponding symbol table entries.

## proj5: Type Checker
A program that determines the types of all expressions in the abstract syntax tree and ensure they are used correctly. 

## proj6: Code Generator
A program that generates MIPS assembly level code for a cdull program.

Authors:

Harsha Kodavalla
