///////////////////////////////////////////////////////////////////////////////
//                   ALL STUDENTS COMPLETE THESE SECTIONS
// Main Class File:  P1.java
// File:             Sym.java
// Semester:         CS 536 Fall 2018
//
// Author:           Harsha Kodavalla - kodavalla@wisc.edu
// CS Login:         harsha
// Lecturer's Name:  Aws Albarghouthi
// Lab Section:      N/A
//
//////////////////// PAIR PROGRAMMERS COMPLETE THIS SECTION ////////////////////
//
// Pair Partner:     (name of your pair programming partner)
// Email:            (email address of your programming partner)
// CS Login:         (partner's login name)
// Lecturer's Name:  (name of your partner's lecturer)
// Lab Section:      (your partner's lab section number)
//
//////////////////// STUDENTS WHO GET HELP FROM OTHER THAN THEIR PARTNER //////
//                   fully acknowledge and credit all sources of help,
//                   other than Instructors and TAs.
//
// Persons:          Identify persons by name, relationship to you, and email.
//                   Describe in detail the the ideas and help they provided.
//
// Online sources:   avoid web searches to solve your problems, but if you do
//                   search, be sure to include Web URLs and description of 
//                   of any information you find.
//////////////////////////// 80 columns wide //////////////////////////////////

/**
 * Implements the Sym class. Contains information about a given identifier.
 * @author Harsha Kodavalla
 */
public class Sym {
	// data members
	String type;
	
	// Constructor
	Sym(String type) {
		this.type = type;
	}
	
	// Return's the type
	String getType() {
		return type;
	}
	
	// Return's the type
	public String toString() {	//******NAMING********
		return type;
	}
}
