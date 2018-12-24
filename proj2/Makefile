###
# This Makefile can be used to make a scanner for the C-- language
# (Yylex.class) and to make a program that tests the scanner (P2.class).
#
# The default makes both the scanner and the test program.
#
# make clean removes all generated files.
#
# Note: P2.java will not compile unless Yylex.class exists.
#
###

# define the java compiler to be used and the flags
JC = javac
FLAGS = -g -cp $(CP)
CP = ../deps:.

P2.class: P2.java Yylex.class sym.class
	$(JC) $(FLAGS) P2.java

Yylex.class: cdull.jlex.java ErrMsg.class sym.class
	$(JC) $(FLAGS) cdull.jlex.java

cdull.jlex.java: cdull.jlex sym.class
	java -cp $(CP) JLex.Main cdull.jlex

sym.class: sym.java
	$(JC) $(FLAGS) sym.java

ErrMsg.class: ErrMsg.java
	$(JC) $(FLAGS) ErrMsg.java

	
###
# testing - when testing properly written code
# output files are compared to input files.
# when testing improperly written code or code with comments,
# output files are instead compared to expected result files.
# None of the diff commands should report ANY differences.
###
test:
	java -cp $(CP) P2 
	diff commentsTestExpected.txt commentsTest.out
	diff testGoodStrings.in testGoodStrings.out
	diff testKeywords.in testKeywords.out
	diff testGoodIden.in testGoodIden.out
	diff badStringsTestExpected.txt testBadStrings.out
	diff integersTestExpected.txt testIntegers.out
	diff badCharTestExpected.txt testBadChar.out
	diff allTokensExpected.txt allTokens.out

###
# clean up
###

clean:
	rm -f *~ *.class cdull.jlex.java

cleantest:
	rm -f commentsTest.out
	rm -f testGoodStrings.out
	rm -f testBadStrings.out
	rm -f testIntegers.out
	rm -f testKeywords.out
	rm -f testGoodIden.out
	rm -f testBadChar.out
	rm -f allTokens.out