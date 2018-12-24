///////////////////////////////////////////////////////////////////////////////
//                   ALL STUDENTS COMPLETE THESE SECTIONS
// Main Class File:  P1.java
// File:             SymTable.java
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

import java.util.*;


/**
 * An implementation of a symbol table. The symbol table is represented as a 
 * linked list of hash maps, where each hash map represents a single scope.
 * The front of the list represents the innermost/nearest scope.
 * @author Harsha Kodavalla
 */
public class SymTable {
	LinkedList<HashMap<String, Sym>> list;
	int scopeSize;	// Tracks the number of scopes stored in the table
	
	/**
 	* The default constructor for the SymTable class.
	* Initalizes the table to have a single empty hash map.
 	*/
	SymTable() {
		list = new LinkedList<HashMap<String, Sym>>();
		list.addFirst(new HashMap<String,Sym>());
		
		scopeSize = 1;
	}

	/**
	 * Adds an entry to the innermost scope in the symbol table.
	 * @param String name - the key for the entry to be added.
	 * @param Sym sym - the information, in a Sym, for the entry to be added.
	 */
	void addDecl(String name, Sym sym) throws DuplicateSymException, EmptySymTableException {
		// If the symbol table contains no scope, throw an exception.
		if (scopeSize < 1) {
			throw new EmptySymTableException();
		}
		
		// If the first hash map in the list contains the given name as a key 
		// throw an exception.
		if (name == null || sym == null) {
			throw new NullPointerException();
		}
		if (list.getFirst().containsKey(name) == true) {
			throw new DuplicateSymException();
		} else {
			// Add the given key & value to the hash map.
			list.getFirst().put(name, sym);
		}
	}
	
	/**
	 * Adds a new scope which will become the innermost scope. 
	 */
	void addScope() {
		list.addFirst(new HashMap<String,Sym>());
		scopeSize++;
	}
	
	/**
	 * Searches the innermost hash map for a key and returns that key's
	 * information if it is located.
	 * @param String name - the key to search for.
	 * @return - Return the key's corresponding Sym if the key
	 * is located. If the key is not located, return null. 
	 */
	Sym lookupLocal(String name) throws EmptySymTableException {
		// If the symbol table contains no scopes, throw an exception.
		if (scopeSize < 1) {
			throw new EmptySymTableException();
		}
		
		if (list.getFirst().containsKey(name) == true) {
			return list.getFirst().get(name);
		} else {
			return null;
		}
	}
	
	/**
	 * Searches the entire symbol table for a key and returns that key's
	 * information if it is located.
	 * @param String name - the key to search for.
	 * @return - Return the key's corresponding Sym if the key
	 * is located. If the key is not located, return null.
	 */
	Sym lookupGlobal(String name) throws EmptySymTableException {
		// If the symbol table contains no scope, throw an exception.
		if (scopeSize < 1) {
			throw new EmptySymTableException();
		}
		
		// Iterate through the symbol table, searching each hash map.
		int index = 0;
		while (index < list.size()) {
			if (list.get(index).containsKey(name) == true) {
				return list.get(index).get(name);
			}
			index++;
		}
		
		return null;
	}
	
	/**
	 * Removes the innermost scope.
	 */
	void removeScope() throws EmptySymTableException {
		// If the symbol table contains no scope, throw an exception.
		if (scopeSize < 1) {
			throw new EmptySymTableException();
		}
		
		list.remove();
		scopeSize--;
	}
	
	/**
	 * Prints the entire symbol table.
	 */
	void print() {
		int index = 0;
		System.out.print("\nSym Table\n");
		while (index < list.size()) {
			System.out.print(list.get(index).toString() + "\n");
			index++;
		}
		System.out.print("\n");
	}
}

