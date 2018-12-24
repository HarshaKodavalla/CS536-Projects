///////////////////////////////////////////////////////////////////////////////
//                   ALL STUDENTS COMPLETE THESE SECTIONS
// Title:            Programming Assignment 2
// Files:            allTokensExpected.txt, allTokens.in, 
// badCharTestExpected.txt, badStringsTestExpected.txt, cdull.jlex, 
// commentsTestexpected.txt, commentsTest.in, eof.txt, ErrMsg.java
// integersTestExpected.txt, Makefile, P2.java, sym.java, testBadChar.in
// testBadChar.in, testBadStrings.in, testGoodIden.in, testGoodStrings.in, 
// testIntegers.in, testKewords.in
//
// Semester:         cs 536 Fall 2018
//
// Author:           Harsha Kodavalla
// Email:            kodavalla@wisc.edu
// CS Login:         harsha
// Lecturer's Name:  Aws Albarghouthi
// Lab Section:      (your lab section number)
//
//////////////////// PAIR PROGRAMMERS COMPLETE THIS SECTION ////////////////////
//
//                   CHECK ASSIGNMENT PAGE TO see IF PAIR-PROGRAMMING IS ALLOWED
//                   If pair programming is allowed:
//                   1. Read PAIR-PROGRAMMING policy (in cs302 policy) 
//                   2. choose a partner wisely
//                   3. REGISTER THE TEAM BEFORE YOU WORK TOGETHER 
//                      a. one partner creates the team
//                      b. the other partner must join the team
//                   4. complete this section for each program file.
//
// Pair Partner:     (name of your pair programming partner)
// Email:            (email address of your programming partner)
// CS Login:         (partner's login name)
// Lecturer's Name:  (name of your partner's lecturer)
// Lab Section:      (your partner's lab section number)
//
//////////////////// STUDENTS WHO GET HELP FROM OTHER THAN THEIR PARTNER //////
//                   must fully acknowledge and credit those sources of help.
//                   Instructors and TAs do not have to be credited here,
//                   but tutors, roommates, relatives, strangers, etc do.
//
// Persons:          Identify persons by name, relationship to you, and email.
//                   Describe in detail the the ideas and help they provided.
//
// Online sources:   avoid web searches to solve your problems, but if you do
//                   search, be sure to include Web URLs and description of 
//                   of any information you find.
//////////////////////////// 80 columns wide //////////////////////////////////
import java.util.*;
import java.io.*;
import java_cup.runtime.*;  // defines Symbol

/**
 * This program is to be used to test the C-- scanner.
 * This version is set up to test all tokens, as well as testing for
 * errors and correct line and character counting. 
 */
public class P2 {
    public static void main(String[] args) throws IOException {
        // exception may be thrown by yylex
        runCommentsTest();

        runGoodStringsTest();

        runKeywordsTest();

        // test good identifiers
        runGoodIdenTest();

        runBadStringsTest();

        runIntegersTest();

        runBadCharTest();

        // test char and line numbers are tracked correctly
        testCharLineNum();

        // final test - larger, varied piece of correct code
        runFinalTest();
    }

    /**
     * Creates and calls the scanner on the input file. Used in testing
     * the validity of the scanner on a variety of files with a variety
     * of combinations of tokens.
     *
     * @param String inputFname name of the file to read from
     * @param String outputFname name of the file to write to
     */
    private static void testAllTokens(String inputFname, 
                                        String outputFname) throws IOException {
        // open input and output files
        FileReader inFile = null;
        PrintWriter outFile = null;
        try {
            inFile = new FileReader(inputFname);
            outFile = new PrintWriter(new FileWriter(outputFname));
        } catch (FileNotFoundException ex) {
            System.err.println("File" + inputFname + "not found.");
            System.exit(-1);
        } catch (IOException ex) {
            System.err.println(outputFname + "cannot be opened.");
            System.exit(-1);
        }

        // create and call the scanner
        Yylex scanner = new Yylex(inFile);
        Symbol token = scanner.next_token();
        while (token.sym != sym.EOF) {
            switch (token.sym) {
            case sym.BOOL:
                outFile.println("bool"); 
                break;
			case sym.INT:
                outFile.println("int");
                break;
            case sym.VOID:
                outFile.println("void");
                break;
            case sym.TRUE:
                outFile.println("true"); 
                break;
            case sym.FALSE:
                outFile.println("false"); 
                break;
            case sym.STRUCT:
                outFile.println("struct"); 
                break;
            case sym.CIN:
                outFile.println("cin"); 
                break;
            case sym.COUT:
                outFile.println("cout");
                break;				
            case sym.IF:
                outFile.println("if");
                break;
            case sym.ELSE:
                outFile.println("else");
                break;
            case sym.WHILE:
                outFile.println("while");
                break;
            case sym.RETURN:
                outFile.println("return");
                break;
            case sym.ID:
                outFile.println(((IdTokenVal)token.value).idVal);
                break;
            case sym.INTLITERAL:  
                outFile.println(((IntLitTokenVal)token.value).intVal);
                break;
            case sym.STRINGLITERAL: 
                outFile.println(((StrLitTokenVal)token.value).strVal);
                break;    
            case sym.LCURLY:
                outFile.println("{");
                break;
            case sym.RCURLY:
                outFile.println("}");
                break;
            case sym.LPAREN:
                outFile.println("(");
                break;
            case sym.RPAREN:
                outFile.println(")");
                break;
            case sym.SEMICOLON:
                outFile.println(";");
                break;
            case sym.COMMA:
                outFile.println(",");
                break;
            case sym.DOT:
                outFile.println(".");
                break;
            case sym.WRITE:
                outFile.println("<<");
                break;
            case sym.READ:
                outFile.println(">>");
                break;				
            case sym.PLUSPLUS:
                outFile.println("++");
                break;
            case sym.MINUSMINUS:
                outFile.println("--");
                break;	
            case sym.PLUS:
                outFile.println("+");
                break;
            case sym.MINUS:
                outFile.println("-");
                break;
            case sym.TIMES:
                outFile.println("*");
                break;
            case sym.DIVIDE:
                outFile.println("/");
                break;
            case sym.NOT:
                outFile.println("!");
                break;
            case sym.AND:
                outFile.println("&&");
                break;
            case sym.OR:
                outFile.println("||");
                break;
            case sym.EQUALS:
                outFile.println("==");
                break;
            case sym.NOTEQUALS:
                outFile.println("!=");
                break;
            case sym.LESS:
                outFile.println("<");
                break;
            case sym.GREATER:
                outFile.println(">");
                break;
            case sym.LESSEQ:
                outFile.println("<=");
                break;
            case sym.GREATEREQ:
                outFile.println(">=");
                break;
			case sym.ASSIGN:
                outFile.println("=");
                break;
			default:
				outFile.println("UNKNOWN TOKEN");
            } // end switch

            token = scanner.next_token();
        } // end while
        outFile.close();
    }

    /**
     * Runs the scanner on commentsTest.in and places output
     * in commentsTest.out. Used in testing the validity of the scanner
     * on files with comments.
     */
    private static void runCommentsTest() throws IOException {
        System.out.println("Testing comments. Expect no errors.");
        testAllTokens("commentsTest.in", "commentsTest.out");
        System.out.println();
        CharNum.num = 1;
    }

    /**
     * Runs the scanner on testGoodStrings.in and places output
     * in testGoodStrings.out. Used in testing the validity of the scanner
     * on files with good strings.
     */
    private static void runGoodStringsTest() throws IOException {
        System.out.println("Testing good strings. Expect no errors.");
        testAllTokens("testGoodStrings.in", "testGoodStrings.out");
        System.out.println();
        CharNum.num = 1;
    }

    /**
     * Runs the scanner on testKeywords.in and places output
     * in testKeywords.out. Used in testing the validity of the scanner
     * on files with keywords.
     */
    private static void runKeywordsTest() throws IOException {
        System.out.println("Testing keywords and symbols. Expect no errors.");
        testAllTokens("testKeywords.in", "testKeywords.out");
        System.out.println();
        CharNum.num = 1;
    }

    /**
     * Runs the scanner on testGoodIden.in and places output
     * in testGoodIden.out. Used in testing the validity of the scanner
     * on files with good identifiers.
     */
    private static void runGoodIdenTest() throws IOException {
        System.out.println("Testing correct identifiers. Expect no errors.");
        testAllTokens("testGoodIden.in", "testGoodIden.out");
        System.out.println();
        CharNum.num = 1;
    }

    /**
     * Runs the scanner on testBadStrings.in and places output
     * in testBadStrings.out. Used in testing the validity of the scanner
     * on files with bad strings. Notifies user of the expected errors.
     */
    private static void runBadStringsTest() throws IOException {
        System.out.println("Testing bad strings. Expect these 4 errors:");
        System.out.println("line 1: unterminated string literal ignored");
        System.out.println("line 2: string literal with bad escaped" + 
                                                "character ignored.");
        System.out.println("line 3: unterminated string literal with bad escaped" + 
                                                "character ignored.");
        System.out.println("line 7: unterminated string literal with bad escaped" + 
                                                "character ignored.");
        System.out.println("TEST RESULTS:");
        testAllTokens("testBadStrings.in", "testBadStrings.out");
        System.out.println();
        CharNum.num = 1;
    }

    /**
     * Runs the scanner on testIntegers.in and places output
     * in testIntegers.out. Used in testing the validity of the scanner
     * on files with integers. Notifies user of the expected overflow error.
     */
    private static void runIntegersTest() throws IOException {
        System.out.println("Testing integers. Expect only this warning:");
        System.out.println("line 4: integer literal too large; using max value");
        System.out.println("TEST RESULTS:");
        testAllTokens("testIntegers.in", "testIntegers.out");
        System.out.println();
        CharNum.num = 1;
    }

    /**
     * Runs the scanner on testBadChar.in and places output
     * in testBadChar.out. Used in testing the validity of the scanner
     * on files with invalid characters. Notifies user of the expected 
     * errors.
     */
    private static void runBadCharTest() throws IOException {
        System.out.println("Testing bad characters. Expect these 6 errors:");
        System.out.println("line 1: ignoring illegal character: @");
        System.out.println("line 1, char 2: ignoring illegal character: @");
        System.out.println("line 3: ignoring illegal character: $");
        System.out.println("line 5: ignoring illegal character: %");
        System.out.println("line 7: ignoring illegal character: #");
        System.out.println("line 9: ignoring illegal character: ~");
        System.out.println("TEST RESULTS:");
        testAllTokens("testBadChar.in", "testBadChar.out");
        System.out.println();
        CharNum.num = 1;
    }

    /**
     * Tests that the scanner correctly counts character and line numbers.
     * Reads from testBadChar.in and checks the returned character and line
     * number values against the expected values for the tokens that are expected
     * in the file. If any values do not match, an error is printed.
     */
    private static void testCharLineNum() throws IOException {    
        System.out.println("Testing line and character number counting " +
                                                "functionality. Expect no errors.");

        // open input and output files
        FileReader inFile = null;
        PrintWriter outFile = null;
        try {
            inFile = new FileReader("testCharLineNum.in");
            outFile = new PrintWriter(new FileWriter("testCharLineNum.out"));
        } catch (FileNotFoundException ex) {
            System.err.println("testCharLineNum.in not found.");
            System.exit(-1);
        } catch (IOException ex) {
            System.err.println("testCharLineNum.out cannot be opened.");
            System.exit(-1);
        }

        // create and call the scanner
        Yylex scanner = new Yylex(inFile);
        Symbol token = scanner.next_token();
        while (token.sym != sym.EOF) {
            switch (token.sym) {
            case sym.BOOL:
                outFile.println("bool"); 
                if (
                    ((TokenVal)token.value).linenum != 1
                    || ((TokenVal)token.value).charnum != 1
                    ) {
                    System.err.println("***ERROR*** incorrect line or charnum");
                }
                break;
            case sym.INT:
                outFile.println("int");
                break;
            case sym.VOID:
                outFile.println("void");
                break;
            case sym.TRUE:
                outFile.println("true"); 
                break;
            case sym.FALSE:
                outFile.println("false"); 
                break;
            case sym.STRUCT:
                if (
                    ((TokenVal)token.value).linenum != 3
                    || ((TokenVal)token.value).charnum != 1
                    ) {
                    System.err.println("***ERROR*** incorrect line or charnum");
                }
                outFile.println("struct"); 
                break;
            case sym.CIN:
                if (
                    ((TokenVal)token.value).linenum != 4
                    || ((TokenVal)token.value).charnum != 1
                    ) {
                    System.err.println("***ERROR*** incorrect line or charnum");
                }
                outFile.println("cin"); 
                break;
            case sym.COUT:
                outFile.println("cout");
                break;				
            case sym.IF:
                outFile.println("if");
                break;
            case sym.ELSE:
                outFile.println("else");
                break;
            case sym.WHILE:
                if (
                    ((TokenVal)token.value).linenum != 6
                    || ((TokenVal)token.value).charnum != 14
                    ) {
                    System.err.println("***ERROR*** incorrect line or charnum");
                }
                outFile.println("while");
                break;
            case sym.RETURN:
                outFile.println("return");
                break;
            case sym.ID:
                outFile.println(((IdTokenVal)token.value).idVal);
                break;
            case sym.INTLITERAL:  
                if (
                    ((IntLitTokenVal)token.value).linenum != 5
                    || ((IntLitTokenVal)token.value).charnum != 12
                    ) {
                System.err.println("***ERROR*** incorrect line or charnum");
                }
                outFile.println(((IntLitTokenVal)token.value).intVal);
                break;
            case sym.STRINGLITERAL: 
                if (
                    ((StrLitTokenVal)token.value).linenum != 4
                    || ((StrLitTokenVal)token.value).charnum != 5
                    ) {
                    System.err.println("***ERROR*** incorrect line or charnum");
                }
                outFile.println(((StrLitTokenVal)token.value).strVal);
                break;    
            case sym.LCURLY:
                outFile.println("{");
                break;
            case sym.RCURLY:
                outFile.println("}");
                break;
            case sym.LPAREN:
                outFile.println("(");
                break;
            case sym.RPAREN:
                outFile.println(")");
                break;
            case sym.SEMICOLON:
                outFile.println(";");
                break;
            case sym.COMMA:
                outFile.println(",");
                break;
            case sym.DOT:
                outFile.println(".");
                break;
            case sym.WRITE:
                outFile.println("<<");
                break;
            case sym.READ:
                outFile.println(">>");
                break;				
            case sym.PLUSPLUS:
                outFile.println("++");
                break;
            case sym.MINUSMINUS:
                outFile.println("--");
                break;	
            case sym.PLUS:
                outFile.println("+");
                break;
            case sym.MINUS:
                outFile.println("-");
                break;
            case sym.TIMES:
                outFile.println("*");
                break;
            case sym.DIVIDE:
                outFile.println("/");
                break;
            case sym.NOT:
                outFile.println("!");
                break;
            case sym.AND:
                outFile.println("&&");
                break;
            case sym.OR:
                outFile.println("||");
                break;
            case sym.EQUALS:
                outFile.println("==");
                break;
            case sym.NOTEQUALS:
                outFile.println("!=");
                break;
            case sym.LESS:
                outFile.println("<");
                break;
            case sym.GREATER:
                outFile.println(">");
                break;
            case sym.LESSEQ:
                outFile.println("<=");
                break;
            case sym.GREATEREQ:
                outFile.println(">=");
                break;
            case sym.ASSIGN:
                outFile.println("=");
                break;
            default:
                outFile.println("UNKNOWN TOKEN");
            } // end switch

            token = scanner.next_token();
        } // end while
        outFile.close();
        System.out.println();
    }

    /**
     * Runs the scanner on allTokens.in and places output
     * in allTokens.out. Used in testing the validity of the scanner
     * on a larger, more varied, properly written program. 
     */
    private static void runFinalTest() throws IOException {
        System.out.println("Testing on large piece of code. Expect no errors.");
        testAllTokens("allTokens.in", "allTokens.out");
        System.out.println();
        CharNum.num = 1;
    }

    

}
