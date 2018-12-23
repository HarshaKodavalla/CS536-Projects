///////////////////////////////////////////////////////////////////////////////
//                   ALL STUDENTS COMPLETE THESE SECTIONS
// Title:            P1.java
// Files:            P1.java
// Semester:         CS 536 Fall 2018
//
// Author:           Harsha Kodavalla
// Email:            kodavalla@wisc.edu
// CS Login:         kodavalla
// Lecturer's Name:  Aws Albarghouthi
// Lab Section:      N/A
///////////////////////////////////////////////////////////////////////////////
/**
 * This class tests the Sym.java and SymTable.java implementations.
 * It tests numerous boundary cases as well as normal usage of the symbol table
 * and its functions.
 * @Harsha Kodavalla
 */


// Tests the SymTable and Sym classes
public class P1 {
	// Data members used by the symbol table testing methods
	private static SymTable symtab = new SymTable();
	private static boolean thrown = false;	// Checks if correct exception is thrown
	private static Sym tempSym;
	
	// These two key and value pairs with identical keys will test that the 
	// look up methods locate the pair that are in scope nearest to the caller.
	private static String intKey = "temp";
	private static Sym intVal = new Sym("int");
	private static String floatKey = "temp";
	private static Sym floatVal = new Sym("float");
	
	private static String stringKey = "name";
	private static Sym stringVal = new Sym("string");
	
	private static String charKey = "letter";
	private static Sym charVal = new Sym("char");
	
	public static void main(String[] args) {
		// Test Sym class's methods
		getTypeTest();
		toStringTest();
		
		// Test addDecl on an empty symbol table
		emptyTableAddDeclTest();
		
		// Test remove on an empty symbol table
		emptyTableRemoveScopeTest();
		
		// Test print
		printTest();
	
		// Ensure scopes and entries are added correctly. Test the validity
		// of the lookup methods.
		addLookupTest();
		
		// Ensure addDecl detects and does not add duplicate keys
		duplicateAddDeclTest();
		
		// Test removes
		removeScopeTest();
	}
	
	/**
	 * Ensure the Sym class's getType method functions properly.
	 */
	static void getTypeTest() {
		if (!intVal.getType().equals("int")) {
			System.out.println("getTypeTest ERROR - " + 
					"getType expected return: int");
			System.out.println("Returned value: " + intVal.getType());
		}
	}
	
	/**
	 * Ensure the Sym class's toString method functions properly.
	 */
	static void toStringTest() {
		if (!intVal.toString().equals("int")) {
			System.out.println("toStringTest ERROR - " +
					"toString expected return: int");
			System.out.println("Returned value: " + intVal.getType());
		}
	}
	
	/**
	 * Ensures that addDecl throws an EmptySymTableException (ESE) when adding an entry 
	 * to an empty symbol table. If ESE is not thrown, print an error message.
	 */
	static void emptyTableAddDeclTest() {
		// Empty the symbol table
		symtab = new SymTable();
		try {
			symtab.removeScope();
			symtab.addDecl(intKey, intVal);
		} catch (EmptySymTableException e) {
			thrown = true;
		} catch (Exception e) {
			System.out.println("emptyTableAddDeclTest() ERROR - " +
					" addDecl throwing exception when adding entry to empty table.");
		} finally {
			if (!thrown) {
				System.out.println("emptyTableAddDeclTest() ERROR - " +
						"addDecl not throwing EmptySymTableException " +
						"when adding entry to empty table.");
			}
			thrown = false;
		}
		// Empty the symbol table 
		symtab = new SymTable();
	}
	
	/**
	 * Ensures that removeScope throws an EmptySymTableException when removing
	 * a scope from an empty table. If ESE is not thrown, print an error message.
	 */
	static void emptyTableRemoveScopeTest() {
		// Empty the symbol table 
		symtab = new SymTable();
		try {
			symtab.removeScope();
			symtab.removeScope();
		} catch (EmptySymTableException e) {
			thrown = true;
		} finally {
			if (!thrown) {
				System.out.println("emptyTableRemoveScopeTest() ERROR - " +
						"removeScope not throwing EmptySymTableException when " + 
						"removing a scope from an empty table.");
			}
			thrown = false;
		}
		// Empty the symbol table 
		symtab = new SymTable();
	}

	/**
	 * Ensures that addDecl throws a DuplicateSymException (DSE) when adding
	 * duplicate keys. If DSE is not thrown, print an error message.
	 * Ensures addDecl does not add the duplicate key to the table.
	 */
	static void duplicateAddDeclTest() {
		// Empty the symbol table 
		symtab = new SymTable();
		
		String duplicateKeyName = "temp";
		Sym expectedVal = new Sym("int");
		Sym errorVal = new Sym("float");
		
		// Add both two key value pairs; "temp" + "int" and "temp" + "float"
		// Ensure that addDecl detects the duplicate key's and throws DSE.
		try {
			symtab.addDecl(duplicateKeyName, expectedVal);
			symtab.addDecl(duplicateKeyName, errorVal);
		} catch (DuplicateSymException e) {
			thrown = true;
		} catch(Exception e) {
			System.out.println("duplicateAddDeclTest() ERROR - " +
					"exception thrown when adding scopes and entries.");
		} finally {
			if (!thrown) {
				System.out.println("duplicateAddDeclTest() ERROR - " +
						" addDecl not throwing DuplicateSymException.");
			}
			thrown = false;
		}
		
		// Ensure duplicate key was not added - should find the value "int"
		try {
			tempSym = symtab.lookupLocal(duplicateKeyName);
			if ( (tempSym == null) || !(tempSym.getType().equals(expectedVal.getType())) ) {
				System.out.println("duplicateAddDeclTest() ERROR - "
						+ "lookupLocal returning incorrect value.");
			}
		} catch (Exception e) {
			System.out.println("duplicateAddDeclTest() ERROR - " +
					"exception thrown by lookupLocal.");
		}
		
		// Empty the symbol table 
		symtab = new SymTable();
	}
	
	/**
	 * Ensure addScope and addDecl are correctly adding scopes and entries.
	 * Ensure lookups are correctly locating entries.
	 */
	static void addLookupTest() {
		// Empty the symbol table 
		symtab = new SymTable();
		
		try {
			symtab.addDecl(intKey, intVal);
			symtab.addDecl(charKey, charVal);
			symtab.addScope();
			// note: intKey == floatKey == "temp"
			symtab.addDecl(floatKey, floatVal);	
			symtab.addScope();
			symtab.addDecl(stringKey, stringVal);
			
			// Use print to verify that scopes and entries were added correctly
			System.out.println("addScope and addDecl results:");
			symtab.print();
			
			// Use lookupLocal to locate the value in the nearest scope 
			tempSym = symtab.lookupLocal(stringKey);
			if ( (tempSym == null) || 
				 !(tempSym.getType().equals(stringVal.getType())) ) {
				System.out.println("addLookupTest() ERROR - lookupLocal " +
						"returning incorrect value.");
			}
			// Use lookupGlobal to locate the value in the nearest scope
			tempSym = symtab.lookupGlobal(stringKey);
			if ( (tempSym == null) || 
				 !(tempSym.getType().equals(stringVal.getType())) ) {
				System.out.println("addLookupTest() ERROR - lookupGlobal " +
						"returning incorrect value.");
			}
			// Use lookupLocal to locate a value in a non-local scope
			if (symtab.lookupLocal(floatKey) != null) {
				System.out.println("addLookupTest() ERROR - " +
						"lookupLocal not returning null.");
			}
			// Use lookupGlobal to locate value in a non-local scope
			tempSym = symtab.lookupGlobal(floatKey);
			if ( (tempSym == null) ||
				 !(tempSym.getType().equals(floatVal.getType())) ) {
				System.out.println("addLookupTest() ERROR - lookupGlobal " +
						"returning incorrect value.");
			}
			// Use lookupGlobal to locate value in a non-local scope
			tempSym = symtab.lookupGlobal(charKey);
			if ( (tempSym == null) ||
				 !(tempSym.getType().equals(charVal.getType())) ) {
				System.out.println("addLookupTest() ERROR - lookupGlobal " +
						"returning incorrect value.");
			}
			// Use lookupGlobal to locate value in a non-local scope with 
			// a duplicate key name. The key value pair in the closest
			// scope should be returned.
			tempSym = symtab.lookupGlobal(intKey);
			if ( (tempSym == null) || 
				 !(tempSym.getType().equals(floatVal.getType())) ) {
				System.out.println("addLookupTest() ERROR - lookupGlobal " +
						"returning incorrect value.");
			}
		} catch (Exception e) {
			System.out.println("addLookupTest() ERROR - exception thrown.");
		}
		
		// Empty the symbol table 
		symtab = new SymTable();
	}
	
	/**
	 * Ensure removeScope removes scopes correctly
	 */
	static void removeScopeTest() {
		// Empty the symbol table 
		symtab = new SymTable();
		
		try {
			symtab.addDecl(intKey, intVal);
			symtab.addScope();
			symtab.addDecl(charKey, charVal);
			
			symtab.removeScope();
			// Ensure entries in removed scopes cannot be retrieved
			if (symtab.lookupGlobal(charKey) != null) {
				System.out.println("removeScopeTest() ERROR -"
						+ " lookupGlobal locating entry from removed scope.");
			}
			// Ensure integrity of scope locality remains;
			// the scope above the just-removed scope must function as local.
			tempSym = symtab.lookupLocal(intKey);
			if ( (tempSym == null) || 
				 !(tempSym.getType().equals(intVal.getType()))) {
				System.out.println("removeScopeTest() ERROR -"
						+ "unable to locate entry in new local scope.");
			}
			symtab.removeScope();
			// Ensure that all scopes have been removed by attempting to
			// remove a scope from an empty table. Expect an ESE to be thrown.
			try {
				symtab.removeScope();
			} catch (EmptySymTableException e) {
				thrown = true;
			} finally {
				if (!thrown) {
					System.out.println("removeScopeTest() ERROR -"
							+ "not all scopes removed.");
				}
				thrown = false;
			}
		} catch (Exception e) {
			System.out.println("removeScopeTest() ERROR - exception thrown.");
		}
		// Empty the symbol table 
		symtab = new SymTable();
	}
	
	/**
	 * Ensures the print method works correctly.
	 * Adds scopes and entries and then outputs them.
	 * Expected output should be an innermost scope containing charKey & charVal,
	 * a middle scope containing intKey, intVal, stringKey & stringVal,
	 * an outer scope containing floatKey & floatVal,
	 * and an empty outermost scope.
	 */
	static void printTest() {
		// Empty the symbol table 
		symtab = new SymTable();
		
		System.out.println("Print test:");
		try {
			symtab.addDecl(floatKey, floatVal);
			symtab.addScope();
			symtab.addDecl(intKey, intVal);
			symtab.addDecl(stringKey, stringVal);
			symtab.addScope();
			symtab.addDecl(charKey, charVal);
		} catch (Exception e) {
			System.out.println("printTest ERROR - exception thrown " +
					"when adding scopes/entries.");
		}
		symtab.print();
		
		// Empty the symbol table 
		symtab = new SymTable();
	}
}
