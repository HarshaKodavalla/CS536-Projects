import java.io.*;
import java.util.*;

// **********************************************************************
// The ASTnode class defines the nodes of the abstract-syntax tree that
// represents a Cdull program.
//
// Internal nodes of the tree contain pointers to children, organized
// either in a list (for nodes that may have a variable number of 
// children) or as a fixed set of fields.
//
// The nodes for literals and ids contain line and character number
// information; for string literals and identifiers, they also contain a
// string; for integer literals, they also contain an integer value.
//
// Here are all the different kinds of AST nodes and what kinds of children
// they have.  All of these kinds of AST nodes are subclasses of "ASTnode".
// Indentation indicates further subclassing:
//
//     Subclass            Kids
//     --------            ----
//     ProgramNode         DeclListNode
//     DeclListNode        linked list of DeclNode
//     DeclNode:
//       VarDeclNode       TypeNode, IdNode, int
//       FnDeclNode        TypeNode, IdNode, FormalsListNode, FnBodyNode
//       FormalDeclNode    TypeNode, IdNode
//       StructDeclNode    IdNode, DeclListNode
//
//     FormalsListNode     linked list of FormalDeclNode
//     FnBodyNode          DeclListNode, StmtListNode
//     StmtListNode        linked list of StmtNode
//     ExpListNode         linked list of ExpNode
//
//     TypeNode:
//       IntNode           -- none --
//       BoolNode          -- none --
//       VoidNode          -- none --
//       StructNode        IdNode
//
//     StmtNode:
//       AssignStmtNode      AssignNode
//       PostIncStmtNode     ExpNode
//       PostDecStmtNode     ExpNode
//       ReadStmtNode        ExpNode
//       WriteStmtNode       ExpNode
//       IfStmtNode          ExpNode, DeclListNode, StmtListNode
//       IfElseStmtNode      ExpNode, DeclListNode, StmtListNode,
//                                    DeclListNode, StmtListNode
//       WhileStmtNode       ExpNode, DeclListNode, StmtListNode
//       RepeatStmtNode      ExpNode, DeclListNode, StmtListNode
//       CallStmtNode        CallExpNode
//       ReturnStmtNode      ExpNode
//
//     ExpNode:
//       IntLitNode          -- none --
//       StrLitNode          -- none --
//       TrueNode            -- none --
//       FalseNode           -- none --
//       IdNode              -- none --
//       DotAccessNode       ExpNode, IdNode
//       AssignNode          ExpNode, ExpNode
//       CallExpNode         IdNode, ExpListNode
//       UnaryExpNode        ExpNode
//         UnaryMinusNode
//         NotNode
//       BinaryExpNode       ExpNode ExpNode
//         PlusNode     
//         MinusNode
//         TimesNode
//         DivideNode
//         AndNode
//         OrNode
//         EqualsNode
//         NotEqualsNode
//         LessNode
//         GreaterNode
//         LessEqNode
//         GreaterEqNode
//
// Here are the different kinds of AST nodes again, organized according to
// whether they are leaves, internal nodes with linked lists of kids, or
// internal nodes with a fixed number of kids:
//
// (1) Leaf nodes:
//        IntNode,   BoolNode,  VoidNode,  IntLitNode,  StrLitNode,
//        TrueNode,  FalseNode, IdNode
//
// (2) Internal nodes with (possibly empty) linked lists of children:
//        DeclListNode, FormalsListNode, StmtListNode, ExpListNode
//
// (3) Internal nodes with fixed numbers of kids:
//        ProgramNode,     VarDeclNode,     FnDeclNode,     FormalDeclNode,
//        StructDeclNode,  FnBodyNode,      StructNode,     AssignStmtNode,
//        PostIncStmtNode, PostDecStmtNode, ReadStmtNode,   WriteStmtNode   
//        IfStmtNode,      IfElseStmtNode,  WhileStmtNode,  CallStmtNode
//        ReturnStmtNode,  DotAccessNode,   AssignExpNode,  CallExpNode,
//        UnaryExpNode,    BinaryExpNode,   UnaryMinusNode, NotNode,
//        PlusNode,        MinusNode,       TimesNode,      DivideNode,
//        AndNode,         OrNode,          EqualsNode,     NotEqualsNode,
//        LessNode,        GreaterNode,     LessEqNode,     GreaterEqNode
//
// **********************************************************************

// **********************************************************************
// ASTnode class (base class for all other kinds of nodes)
// **********************************************************************

abstract class ASTnode { 
    // every subclass must provide an unparse operation
    abstract public void unparse(PrintWriter p, int indent);

    // this method can be used by the unparse methods to do indenting
    protected void printSpace(PrintWriter p, int indent) {
        for (int k=0; k<indent; k++) p.print(" ");
    }
	
	public static String currFnName; // name of current function
}

// **********************************************************************
// ProgramNode,  DeclListNode, FormalsListNode, FnBodyNode,
// StmtListNode, ExpListNode
// **********************************************************************

class ProgramNode extends ASTnode {
    public ProgramNode(DeclListNode L) {
        declList = L;
    }

    /**
     * nameAnalysis
     * Creates an empty symbol table for the outermost scope, then processes
     * all of the globals, struct defintions, and functions in the program.
     */
    public void nameAnalysis() {
        SymTable symTab = new SymTable();
        declList.nameAnalysis(symTab);
		if (noMain) {
			ErrMsg.fatal(0, 0, "No main function");
		}
    }
    
    /**
     * typeCheck
     */
    public void typeCheck() {
        declList.typeCheck();
    }
    
	/**
	 * codeGen
	 */
	public void codeGen() {
        declList.codeGen();
	}
	
    public void unparse(PrintWriter p, int indent) {
        declList.unparse(p, indent);
    }

    // 1 kid
    private DeclListNode declList;
	public static boolean noMain = true;
}

class DeclListNode extends ASTnode {
    public DeclListNode(List<DeclNode> S) {
        decls = S;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, process all of the decls in the list.
     */
    public void nameAnalysis(SymTable symTab) {
        nameAnalysis(symTab, symTab);
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab and a global symbol table globalTab
     * (for processing struct names in variable decls), process all of the 
     * decls in the list.
     */    
    public void nameAnalysis(SymTable symTab, SymTable globalTab) {
        for (DeclNode node : decls) {
            if (node instanceof VarDeclNode) {
                ((VarDeclNode)node).nameAnalysis(symTab, globalTab);
            } else {
                node.nameAnalysis(symTab);
            }
        }
    }    
    
    /**
     * typeCheck
     */
    public void typeCheck() {
        for (DeclNode node : decls) {
            node.typeCheck();
        }
    }
 
	/**
	 * codeGen
	 */
	public void codeGen() {
        for (DeclNode node : decls) {
            node.codeGen();
        }
	}
	
    public void unparse(PrintWriter p, int indent) {
        Iterator it = decls.iterator();
        try {
            while (it.hasNext()) {
                ((DeclNode)it.next()).unparse(p, indent);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.print");
            System.exit(-1);
        }
    }

    // list of kids (DeclNodes)
    private List<DeclNode> decls;
}

class FormalsListNode extends ASTnode {
    public FormalsListNode(List<FormalDeclNode> S) {
        formals = S;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * for each formal decl in the list
     *     process the formal decl
     *     if there was no error, add type of formal decl to list
     */
    public List<Type> nameAnalysis(SymTable symTab) {
        List<Type> typeList = new LinkedList<Type>();
        for (FormalDeclNode node : formals) {
            Sym sym = node.nameAnalysis(symTab);
            if (sym != null) {
                typeList.add(sym.getType());
            }
        }
        return typeList;
    }    
    
    /**
     * Return the number of formals in this list.
     */
    public int length() {
        return formals.size();
    }
    
    public void unparse(PrintWriter p, int indent) {
        Iterator<FormalDeclNode> it = formals.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        } 
    }

    // list of kids (FormalDeclNodes)
    private List<FormalDeclNode> formals;
}

class FnBodyNode extends ASTnode {
    public FnBodyNode(DeclListNode declList, StmtListNode stmtList) {
        this.declList = declList;
        this.stmtList = stmtList;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the declaration list
     * - process the statement list
     */
    public void nameAnalysis(SymTable symTab) {
        declList.nameAnalysis(symTab);
        stmtList.nameAnalysis(symTab);
    }    
 
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        stmtList.typeCheck(retType);
    }    
 
	/**
	 * codeGen
	 */
	public void codeGen(String label) {
        stmtList.codeGen(label);
	}
	
    public void unparse(PrintWriter p, int indent) {
        declList.unparse(p, indent);
        stmtList.unparse(p, indent);
    }

    // 2 kids
    private DeclListNode declList;
    private StmtListNode stmtList;
}

class StmtListNode extends ASTnode {
    public StmtListNode(List<StmtNode> S) {
        stmts = S;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, process each statement in the list.
     */
    public void nameAnalysis(SymTable symTab) {
        for (StmtNode node : stmts) {
            node.nameAnalysis(symTab);
        }
    }    
    
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        for(StmtNode node : stmts) {
            node.typeCheck(retType);
        }
    }

	/**
	 * codeGen
	 */
	public void codeGen(String label) {
        for (StmtNode node : stmts) {
            node.codeGen(label);
        }
	}
    
    public void unparse(PrintWriter p, int indent) {
        Iterator<StmtNode> it = stmts.iterator();
        while (it.hasNext()) {
            it.next().unparse(p, indent);
        }
    }

    // list of kids (StmtNodes)
    private List<StmtNode> stmts;
}

class ExpListNode extends ASTnode {
    public ExpListNode(List<ExpNode> S) {
        exps = S;
    }
    
    public int size() {
        return exps.size();
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, process each exp in the list.
     */
    public void nameAnalysis(SymTable symTab) {
        for (ExpNode node : exps) {
            node.nameAnalysis(symTab);
        }
    }
    
    /**
     * typeCheck
     */
    public void typeCheck(List<Type> typeList) {
        int k = 0;
        try {
            for (ExpNode node : exps) {
                Type actualType = node.typeCheck();     // actual type of arg
                
                if (!actualType.isErrorType()) {        // if this is not an error
                    Type formalType = typeList.get(k);  // get the formal type
                    if (!formalType.equals(actualType)) {
                        ErrMsg.fatal(node.lineNum(), node.charNum(),
                                     "Type of actual does not match type of formal");
                    }
                }
                k++;
            }
        } catch (NoSuchElementException e) {
            System.err.println("unexpected NoSuchElementException in ExpListNode.typeCheck");
            System.exit(-1);
        }
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
        for (ExpNode node : exps) {
            node.codeGen();
        }
	}
    
    public void unparse(PrintWriter p, int indent) {
        Iterator<ExpNode> it = exps.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        } 
    }

    // list of kids (ExpNodes)
    private List<ExpNode> exps;
}

// **********************************************************************
// DeclNode and its subclasses
// **********************************************************************

abstract class DeclNode extends ASTnode {
    /**
     * Note: a formal decl needs to return a sym
     */
    abstract public Sym nameAnalysis(SymTable symTab);

    // default version of typeCheck for non-function decls
    public void typeCheck() { }
	
	// default version for struct decls and formal decls
	public void codeGen() { }
}

class VarDeclNode extends DeclNode {
    public VarDeclNode(TypeNode type, IdNode id, int size) {
        this.type = type;
        this.id = id;
        this.size = size;
    }

    /**
     * nameAnalysis (overloaded)
     * Given a symbol table symTab, do:
     * if this name is declared void, then error
     * else if the declaration is of a struct type, 
     *     lookup type name (globally)
     *     if type name doesn't exist, then error
     * if no errors so far,
     *     if name has already been declared in this scope, then error
     *     else add name to local symbol table     
     *
     * symTab is local symbol table (say, for struct field decls)
     * globalTab is global symbol table (for struct type names)
     * symTab and globalTab can be the same
     */
    public Sym nameAnalysis(SymTable symTab) {
        return nameAnalysis(symTab, symTab);
    }
    
    public Sym nameAnalysis(SymTable symTab, SymTable globalTab) {
        boolean badDecl = false;
        String name = id.name();
        Sym sym = null;
        IdNode structId = null;

        if (type instanceof VoidNode) {  // check for void type
            ErrMsg.fatal(id.lineNum(), id.charNum(), 
                         "Non-function declared void");
            badDecl = true;        
        }
        
        else if (type instanceof StructNode) {
            structId = ((StructNode)type).idNode();
            sym = globalTab.lookupGlobal(structId.name());
            
            // if the name for the struct type is not found, 
            // or is not a struct type
            if (sym == null || !(sym instanceof StructDefSym)) {
                ErrMsg.fatal(structId.lineNum(), structId.charNum(), 
                             "Invalid name of struct type");
                badDecl = true;
            }
            else {
                structId.link(sym);
            }
        }
        
        if (symTab.lookupLocal(name) != null) {
            ErrMsg.fatal(id.lineNum(), id.charNum(), 
                         "Multiply declared identifier");
            badDecl = true;            
        }
        
        if (!badDecl) {  // insert into symbol table
            try {
                if (type instanceof StructNode) {
                    sym = new StructSym(structId);
                }
                else {
					sym = new Sym(type.type());
					if (!globalTab.isGlobalScope()) {
						int offset = globalTab.getOffset();
						sym.setOffset(offset);
						globalTab.setOffset(offset - 4); // everything is int or bool
					}
                }
                globalTab.addDecl(name, sym);
                id.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (WrongArgumentException ex) {
                System.err.println("Unexpected WrongArgumentException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            } 
        }
        
        return sym;
    }   
	
	/**
	 * codeGen
     * Only should be executed on global variables - local vars dont need code gen
	 */
	public void codeGen() {
        if (id.sym().isGlobal()) {
            Codegen.p.println("\t.data");
            Codegen.p.println("\t.align 2");
            // int & bool both 4 bytes
            Codegen.p.println("_" + id.name() + ": .space 4");  
        }
	} 
	
    public void unparse(PrintWriter p, int indent) {
        printSpace(p, indent);
        type.unparse(p, 0);
        p.print(" ");
        p.print(id.name());
        p.println(";");
    }

    // 3 kids
    private TypeNode type;
    private IdNode id;
    private int size;  // use value NOT_STRUCT if this is not a struct type

    public static int NOT_STRUCT = -1;
}

class FnDeclNode extends DeclNode {
    public FnDeclNode(TypeNode type,
                      IdNode id,
                      FormalsListNode formalList,
                      FnBodyNode body) {
        this.type = type;
        this.id = id;
        this.formalsList = formalList;
        this.body = body;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this name has already been declared in this scope, then error
     * else add name to local symbol table
     * in any case, do the following:
     *     enter new scope
     *     process the formals
     *     if this function is not multiply declared,
     *         update symbol table entry with types of formals
     *     process the body of the function
     *     exit scope
     */
    public Sym nameAnalysis(SymTable symTab) {
        String name = id.name();
        FnSym sym = null;
        
        if (symTab.lookupLocal(name) != null) {
            ErrMsg.fatal(id.lineNum(), id.charNum(),
                         "Multiply declared identifier");
        }
        
        else { // add function name to local symbol table
			if (name.equals("main")) ProgramNode.noMain = false;
            try {
                sym = new FnSym(type.type(), formalsList.length());
                symTab.addDecl(name, sym);
                id.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " +
                                   " in FnDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                                   " in FnDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (WrongArgumentException ex) {
                System.err.println("Unexpected WrongArgumentException " +
                                   " in FnDeclNode.nameAnalysis");
                System.exit(-1);
            } 
        }
        
		symTab.setGlobalScope(false);
		symTab.setOffset(0);
        symTab.addScope();  // add a new scope for locals and params
        
        // process the formals
        List<Type> typeList = formalsList.nameAnalysis(symTab);
        if (sym != null) {
            sym.addFormals(typeList);
            sym.setParamSize(-1*symTab.getOffset());
        }
        
		symTab.setOffset(symTab.getOffset() - 8);
		int temp = symTab.getOffset();
        body.nameAnalysis(symTab); // process the function body
        if (sym != null) {
            sym.setLocalSize(-1*(symTab.getOffset() - temp));
        }
		symTab.setGlobalScope(true);
		
        try {
            symTab.removeScope();  // exit scope
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in FnDeclNode.nameAnalysis");
            System.exit(-1);
        }
        
        return null;
    } 
       
    /**
     * typeCheck
     */
    public void typeCheck() {
        body.typeCheck(type.type());
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
        String exitLabel = "EXIT_" + id.name();
        FnSym sym = (FnSym) id.sym();

         // Function Preamble
         Codegen.p.println("\t.text");
         id.genFnEntry();
         boolean isMain = id.name().equals("main");
        
        // Push the return addr
        // sw   $ra, 0($sp)
        Codegen.generateIndexed("sw", Codegen.RA, Codegen.SP, 0);
        // subu $sp, $sp, 4
        Codegen.generate("subu", Codegen.SP, Codegen.SP, 4);

        // Push the control link
        // sw   $fp, 0($sp)
        Codegen.generateIndexed("sw", Codegen.FP, Codegen.SP, 0);
        // subu $sp, $sp, 4
        Codegen.generate("subu", Codegen.SP, Codegen.SP, 4);

        // Set the FP
        // addu $fp, $sp, 8 + <paramSize>
        Codegen.generate("addu", Codegen.FP, Codegen.SP, 8 + sym.getParamSize());

        // Push space for the locals
        // subu $sp, $sp, <size of locals>
        Codegen.generate("subu", Codegen.SP, Codegen.SP, sym.getLocalSize());

        // Function Body
        // Exit label is passed due to returnStmt implementation
        body.codeGen(exitLabel);

        // Function Exit

        // Exit label
        Codegen.genLabel(exitLabel);
        
        // Load return address
        // lw   $ra, -<paramSize>($fp)
        Codegen.generateIndexed("lw", Codegen.RA, Codegen.FP, -(sym.getParamSize()));

        // Restore FP and SP
        // move $t0, $fp
        Codegen.generate("move", Codegen.T0, Codegen.FP);
        // lw   $fp, -<paramSize> - 4($fp)
        Codegen.generateIndexed("lw", Codegen.FP, Codegen.FP, -(sym.getParamSize()) - 4);
        // move $sp, $t0
        Codegen.generate("move", Codegen.SP, Codegen.T0);
        
        // Handle main function case
        if (isMain) {
            // li $v0, 10
            Codegen.generate("li", Codegen.V0, "10");
            Codegen.generate("syscall");
        } else {
            // Return
            // jr   $ra
            Codegen.generate("jr", Codegen.RA);
        }
	}
	
    public void unparse(PrintWriter p, int indent) {
        printSpace(p, indent);
        type.unparse(p, 0);
        p.print(" ");
        p.print(id.name());
        p.print("(");
        formalsList.unparse(p, 0);
        p.println(") {");
        body.unparse(p, indent+4);
        p.println("}\n");
    }

    // 4 kids
    private TypeNode type;
    private IdNode id;
    private FormalsListNode formalsList;
    private FnBodyNode body;
}

class FormalDeclNode extends DeclNode {
    public FormalDeclNode(TypeNode type, IdNode id) {
        this.type = type;
        this.id = id;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this formal is declared void, then error
     * else if this formal is already in the local symble table,
     *     then issue multiply declared error message and return null
     * else add a new entry to the symbol table and return that Sym
     */
    public Sym nameAnalysis(SymTable symTab) {
        String name = id.name();
        boolean badDecl = false;
        Sym sym = null;
        
        if (type instanceof VoidNode) {
            ErrMsg.fatal(id.lineNum(), id.charNum(), 
                         "Non-function declared void");
            badDecl = true;        
        }
        
        if (symTab.lookupLocal(name) != null) {
            ErrMsg.fatal(id.lineNum(), id.charNum(), 
                         "Multiply declared identifier");
            badDecl = true;
        }
        
        if (!badDecl) {  // insert into symbol table
            try {
				int offset = symTab.getOffset();
                sym = new Sym(type.type());
				sym.setOffset(offset);
				symTab.setOffset(offset - 4); // only int and bool formals
                symTab.addDecl(name, sym);
                id.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (WrongArgumentException ex) {
                System.err.println("Unexpected WrongArgumentException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            }        
        }
        
        return sym;
    }    
    
    public void unparse(PrintWriter p, int indent) {
        type.unparse(p, 0);
        p.print(" ");
        p.print(id.name());
    }

    // 2 kids
    private TypeNode type;
    private IdNode id;
}

class StructDeclNode extends DeclNode {
    public StructDeclNode(IdNode id, DeclListNode declList) {
        this.id = id;
        this.declList = declList;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this name is already in the symbol table,
     *     then multiply declared error (don't add to symbol table)
     * create a new symbol table for this struct definition
     * process the decl list
     * if no errors
     *     add a new entry to symbol table for this struct
     */
    public Sym nameAnalysis(SymTable symTab) {
        String name = id.name();
        boolean badDecl = false;
        
        if (symTab.lookupLocal(name) != null) {
            ErrMsg.fatal(id.lineNum(), id.charNum(), 
                         "Multiply declared identifier");
            badDecl = true;            
        }

        SymTable structSymTab = new SymTable();
        
        // process the fields of the struct
        declList.nameAnalysis(structSymTab, symTab);
        
        if (!badDecl) {
            try {   // add entry to symbol table
                StructDefSym sym = new StructDefSym(structSymTab);
                symTab.addDecl(name, sym);
                id.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " +
                                   " in StructDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                                   " in StructDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (WrongArgumentException ex) {
                System.err.println("Unexpected WrongArgumentException " +
                                   " in StructDeclNode.nameAnalysis");
                System.exit(-1);
            } 
        }
        
        return null;
    }    
    
    public void unparse(PrintWriter p, int indent) {
        printSpace(p, indent);
        p.print("struct ");
        p.print(id.name());
        p.println("{");
        declList.unparse(p, indent+4);
        printSpace(p, indent);
        p.println("};\n");

    }

    // 2 kids
    private IdNode id;
    private DeclListNode declList;
}

// **********************************************************************
// TypeNode and its Subclasses
// **********************************************************************

abstract class TypeNode extends ASTnode {
    /* all subclasses must provide a type method */
    abstract public Type type();
}

class IntNode extends TypeNode {
    public IntNode() {
    }

    /**
     * type
     */
    public Type type() {
        return new IntType();
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("int");
    }
}

class BoolNode extends TypeNode {
    public BoolNode() {
    }

    /**
     * type
     */
    public Type type() {
        return new BoolType();
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("bool");
    }
}

class VoidNode extends TypeNode {
    public VoidNode() {
    }
    
    /**
     * type
     */
    public Type type() {
        return new VoidType();
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("void");
    }
}

class StructNode extends TypeNode {
    public StructNode(IdNode id) {
        this.id = id;
    }

    public IdNode idNode() {
        return id;
    }
    
    /**
     * type
     */
    public Type type() {
        return new StructType(id);
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("struct ");
        p.print(id.name());
    }
    
    // 1 kid
    private IdNode id;
}

// **********************************************************************
// StmtNode and its subclasses
// **********************************************************************

abstract class StmtNode extends ASTnode {
    abstract public void nameAnalysis(SymTable symTab);
    abstract public void typeCheck(Type retType);
    // Pass label to allow for easy function return
    public void codeGen(String label) { };
}

class AssignStmtNode extends StmtNode {
    public AssignStmtNode(AssignNode assign) {
        this.assign = assign;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        assign.nameAnalysis(symTab);
    }
    
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        assign.typeCheck();
    }

	/**
	 * codeGen
	 */
	public void codeGen(String label) {
        assign.codeGen();
        // Pop the value left on the stack by assign expression
        Codegen.genPop(Codegen.T0);
	}
	
    public void unparse(PrintWriter p, int indent) {
        printSpace(p, indent);
        assign.unparse(p, -1); // no parentheses
        p.println(";");
    }

    // 1 kid
    private AssignNode assign;
}

class PostIncStmtNode extends StmtNode {
    public PostIncStmtNode(ExpNode exp) {
        this.exp = exp;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        exp.nameAnalysis(symTab);
    }
    
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = exp.typeCheck();
        
        if (!type.isErrorType() && !type.isIntType()) {
            ErrMsg.fatal(exp.lineNum(), exp.charNum(),
                         "Arithmetic operator applied to non-numeric operand");
        }
    }

	/**
	 * codeGen
	 */
	public void codeGen(String label) {
        // To access IDnode's codeGen methods
        IdNode idExp = (IdNode) exp;

        // Get value and location of the incremented var
        idExp.codeGen();
        idExp.genAddr();        
        // Store value and location
        Codegen.genPop(Codegen.T1);
        Codegen.genPop(Codegen.T0);
        // add  $t0, $t0, 1
        Codegen.generate("add", Codegen.T0, Codegen.T0, 1);
        // Store updated value in location
        // sw   $t0, 0($t1)
        Codegen.generateIndexed("sw", Codegen.T0, Codegen.T1, 0);
	}
	
    public void unparse(PrintWriter p, int indent) {
        printSpace(p, indent);
        exp.unparse(p, 0);
        p.println("++;");
    }

    // 1 kid
    private ExpNode exp;
}

class PostDecStmtNode extends StmtNode {
    public PostDecStmtNode(ExpNode exp) {
        this.exp = exp;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        exp.nameAnalysis(symTab);
    }
    
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = exp.typeCheck();
        
        if (!type.isErrorType() && !type.isIntType()) {
            ErrMsg.fatal(exp.lineNum(), exp.charNum(),
                         "Arithmetic operator applied to non-numeric operand");
        }
    }
 
	/**
	 * codeGen
	 */
	public void codeGen(String label) {
        // To access IDnode's codeGen methods
        IdNode idExp = (IdNode) exp;

        // Get value and location of the incremented var
        idExp.codeGen();
        idExp.genAddr();        

        // Store value and location
        Codegen.genPop(Codegen.T1);
        Codegen.genPop(Codegen.T0);

        // add  $t0, $t0, 1
        Codegen.generate("sub", Codegen.T0, Codegen.T0, 1);

        // Store updated value in memory location of variable
        // sw   $t0, 0($t1)
        Codegen.generateIndexed("sw", Codegen.T0, Codegen.T1, 0);
	}
	
    public void unparse(PrintWriter p, int indent) {
        printSpace(p, indent);
        exp.unparse(p, 0);
        p.println("--;");
    }
    
    // 1 kid
    private ExpNode exp;
}

class ReadStmtNode extends StmtNode {
    public ReadStmtNode(ExpNode e) {
        exp = e;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        exp.nameAnalysis(symTab);
    }    
 
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = exp.typeCheck();
        
        if (type.isFnType()) {
            ErrMsg.fatal(exp.lineNum(), exp.charNum(),
                         "Attempt to read a function");
        }
        
        if (type.isStructDefType()) {
            ErrMsg.fatal(exp.lineNum(), exp.charNum(),
                         "Attempt to read a struct name");
        }
        
        if (type.isStructType()) {
            ErrMsg.fatal(exp.lineNum(), exp.charNum(),
                         "Attempt to read a struct variable");
        }
    }

	/**
	 * codeGen
	 */
	public void codeGen(String label) {
        // li   $v0, 5
        Codegen.generate("li", Codegen.V0, 5);
        Codegen.generate("syscall");

        // genAddr will push the address and place it into $t0
        ((IdNode)exp).genAddr();

        // Store value 
        // sw   $v0, 0($t0)
        Codegen.generateIndexed("sw", Codegen.V0, Codegen.T0, 0);

        // Pop the address off the stack
        Codegen.genPop(Codegen.V0);
	}
    
    public void unparse(PrintWriter p, int indent) {
        printSpace(p, indent);
        p.print("cin >> ");
        exp.unparse(p, 0);
        p.println(";");
    }

    // 1 kid (actually can only be an IdNode or an ArrayExpNode)
    private ExpNode exp;
}

class WriteStmtNode extends StmtNode {
    public WriteStmtNode(ExpNode exp) {
        this.exp = exp;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        exp.nameAnalysis(symTab);
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = exp.typeCheck();
		this.type = type;
        
        if (type.isFnType()) {
            ErrMsg.fatal(exp.lineNum(), exp.charNum(),
                         "Attempt to write a function");
        }
        
        if (type.isStructDefType()) {
            ErrMsg.fatal(exp.lineNum(), exp.charNum(),
                         "Attempt to write a struct name");
        }
        
        if (type.isStructType()) {
            ErrMsg.fatal(exp.lineNum(), exp.charNum(),
                         "Attempt to write a struct variable");
        }
        
        if (type.isVoidType()) {
            ErrMsg.fatal(exp.lineNum(), exp.charNum(),
                         "Attempt to write void");
        }
    }
	
	/**
	 * codeGen
	 */
	public void codeGen(String label) {
        // Evaluate and pop expression value
        exp.codeGen();
        Codegen.genPop(Codegen.A0);

        if (type.isStringType()) {
            // generate("li", V0, 4);
            Codegen.generate("li", Codegen.V0, 4);
        } else {
            // is an integer
            // generate("li", V0, 1);
            Codegen.generate("li", Codegen.V0, 1);
        }
        Codegen.generate("syscall");
	}
	
    public void unparse(PrintWriter p, int indent) {
        printSpace(p, indent);
        p.print("cout << ");
        exp.unparse(p, 0);
        p.println(";");
    }

    // 1 kid
    private ExpNode exp;
	private Type type;
}

class IfStmtNode extends StmtNode {
    public IfStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        declList = dlist;
        this.exp = exp;
        stmtList = slist;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts
     * - exit the scope
     */
    public void nameAnalysis(SymTable symTab) {
        exp.nameAnalysis(symTab);
        symTab.addScope();
        declList.nameAnalysis(symTab);
        stmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
    }
    
     /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = exp.typeCheck();
        
        if (!type.isErrorType() && !type.isBoolType()) {
            ErrMsg.fatal(exp.lineNum(), exp.charNum(),
                         "Non-bool expression used as an if condition");        
        }
        
        stmtList.typeCheck(retType);
    }

	/**
	 * codeGen
	 */
	public void codeGen(String label) {
        String falseLab = Codegen.nextLabel();
        // Evalute bool expression
        exp.codeGen();
        // Store result into $t0
        Codegen.genPop(Codegen.T0);

        // If false, jump to false label, otherwise execute then block
        // beq  $t0, 0, <label>
        Codegen.generate("beq", Codegen.T0, Codegen.FALSE, falseLab);
        // Fall through - then block
        stmtList.codeGen(label);

        // False label
        Codegen.genLabel(falseLab);
	}
	
    public void unparse(PrintWriter p, int indent) {
        printSpace(p, indent);
        p.print("if (");
        exp.unparse(p, 0);
        p.println(") {");
        declList.unparse(p, indent+4);
        stmtList.unparse(p, indent+4);
        printSpace(p, indent);
        p.println("}");
    }

    // e kids
    private ExpNode exp;
    private DeclListNode declList;
    private StmtListNode stmtList;
}

class IfElseStmtNode extends StmtNode {
    public IfElseStmtNode(ExpNode exp, DeclListNode dlist1,
                          StmtListNode slist1, DeclListNode dlist2,
                          StmtListNode slist2) {
        this.exp = exp;
        thenDeclList = dlist1;
        thenStmtList = slist1;
        elseDeclList = dlist2;
        elseStmtList = slist2;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts of then
     * - exit the scope
     * - enter a new scope
     * - process the decls and stmts of else
     * - exit the scope
     */
    public void nameAnalysis(SymTable symTab) {
        exp.nameAnalysis(symTab);
        symTab.addScope();
        thenDeclList.nameAnalysis(symTab);
        thenStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
        symTab.addScope();
        elseDeclList.nameAnalysis(symTab);
        elseStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
    }
    
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = exp.typeCheck();
        
        if (!type.isErrorType() && !type.isBoolType()) {
            ErrMsg.fatal(exp.lineNum(), exp.charNum(),
                         "Non-bool expression used as an if condition");        
        }
        
        thenStmtList.typeCheck(retType);
        elseStmtList.typeCheck(retType);
    }

	/**
	 * codeGen
	 */
	public void codeGen(String label) {
        String falseLab = Codegen.nextLabel();
        String exitLab = Codegen.nextLabel();

        // Evalute bool expression and store result
        exp.codeGen();
        Codegen.genPop(Codegen.T0);

        // If false, jump to false label, otherwise execute then block
        // beq  $t0, 0, <label>
        Codegen.generate("beq", Codegen.T0, Codegen.FALSE, falseLab);
        // Fall through - then block
        thenStmtList.codeGen(label);
        // Jump to exit
        // j    <label>
        Codegen.generate("j", exitLab);

        // False label
        Codegen.genLabel(falseLab);
        // Else block
        elseStmtList.codeGen(label);

        // Exit label
        Codegen.genLabel(exitLab);
	}
	
    public void unparse(PrintWriter p, int indent) {
        printSpace(p, indent);
        p.print("if (");
        exp.unparse(p, 0);
        p.println(") {");
        thenDeclList.unparse(p, indent+4);
        thenStmtList.unparse(p, indent+4);
        printSpace(p, indent);
        p.println("}");
        printSpace(p, indent);
        p.println("else {");
        elseDeclList.unparse(p, indent+4);
        elseStmtList.unparse(p, indent+4);
        printSpace(p, indent);
        p.println("}");        
    }

    // 5 kids
    private ExpNode exp;
    private DeclListNode thenDeclList;
    private StmtListNode thenStmtList;
    private StmtListNode elseStmtList;
    private DeclListNode elseDeclList;
}

class WhileStmtNode extends StmtNode {
    public WhileStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        this.exp = exp;
        declList = dlist;
        stmtList = slist;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts
     * - exit the scope
     */
    public void nameAnalysis(SymTable symTab) {
        exp.nameAnalysis(symTab);
        symTab.addScope();
        declList.nameAnalysis(symTab);
        stmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
    }
    
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = exp.typeCheck();
        
        if (!type.isErrorType() && !type.isBoolType()) {
            ErrMsg.fatal(exp.lineNum(), exp.charNum(),
                         "Non-bool expression used as a while condition");        
        }
        
        stmtList.typeCheck(retType);
    }
	
	/**
	 * codeGen
	 */
	public void codeGen(String label) {
        String loopLab = Codegen.nextLabel();
        String exitLab = Codegen.nextLabel();

        // Loop start
        Codegen.genLabel(loopLab);

        // Evaluate bool expression and store result
        exp.codeGen();
        Codegen.genPop(Codegen.T0);

        // If false, exit the loop. Otherwise execute loop's body
        // beq  $t0, 0, <label>
        Codegen.generate("beq", Codegen.T0, Codegen.FALSE, exitLab);
        stmtList.codeGen(label);
        // Jump to start of loop
        // j    <label>
        Codegen.generate("j", loopLab);

        // Exit label
        Codegen.genLabel(exitLab);

	}
	
    public void unparse(PrintWriter p, int indent) {
        printSpace(p, indent);
        p.print("while (");
        exp.unparse(p, 0);
        p.println(") {");
        declList.unparse(p, indent+4);
        stmtList.unparse(p, indent+4);
        printSpace(p, indent);
        p.println("}");
    }

    // 3 kids
    private ExpNode exp;
    private DeclListNode declList;
    private StmtListNode stmtList;
}

class RepeatStmtNode extends StmtNode {
    public RepeatStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        this.exp = exp;
        declList = dlist;
        stmtList = slist;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts
     * - exit the scope
     */
    public void nameAnalysis(SymTable symTab) {
        exp.nameAnalysis(symTab);
        symTab.addScope();
        declList.nameAnalysis(symTab);
        stmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
    }
    
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = exp.typeCheck();
        
        if (!type.isErrorType() && !type.isIntType()) {
            ErrMsg.fatal(exp.lineNum(), exp.charNum(),
                         "Non-integer expression used as a repeat clause");        
        }
        
        stmtList.typeCheck(retType);
    }
        
    public void unparse(PrintWriter p, int indent) {
        printSpace(p, indent);
        p.print("repeat (");
        exp.unparse(p, 0);
        p.println(") {");
        declList.unparse(p, indent+4);
        stmtList.unparse(p, indent+4);
        printSpace(p, indent);
        p.println("}");
    }

    // 3 kids
    private ExpNode exp;
    private DeclListNode declList;
    private StmtListNode stmtList;
}


class CallStmtNode extends StmtNode {
    public CallStmtNode(CallExpNode call) {
        this.call = call;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        call.nameAnalysis(symTab);
    }
    
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        call.typeCheck();
    }
 
	/**
	 * codeGen
	 */
	public void codeGen(String label) {
        call.codeGen();
        // Pop value off the stack
        Codegen.genPop(Codegen.T0);
	}
	
    public void unparse(PrintWriter p, int indent) {
        printSpace(p, indent);
        call.unparse(p, indent);
        p.println(";");
    }

    // 1 kid
    private CallExpNode call;
}

class ReturnStmtNode extends StmtNode {
    public ReturnStmtNode(ExpNode exp) {
        this.exp = exp;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child,
     * if it has one
     */
    public void nameAnalysis(SymTable symTab) {
        if (exp != null) {
            exp.nameAnalysis(symTab);
        }
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        if (exp != null) {  // return value given
            Type type = exp.typeCheck();
            
            if (retType.isVoidType()) {
                ErrMsg.fatal(exp.lineNum(), exp.charNum(),
                             "Return with a value in a void function");                
            }
            
            else if (!retType.isErrorType() && !type.isErrorType() && !retType.equals(type)){
                ErrMsg.fatal(exp.lineNum(), exp.charNum(),
                             "Bad return value");
            }
        }
        
        else {  // no return value given -- ok if this is a void function
            if (!retType.isVoidType()) {
                ErrMsg.fatal(0, 0, "Missing return value");                
            }
        }
        
    }
	
	/**
	 * codeGen
     * Implemented by passing the exit label of the function to its statement
     * body. When return is called, jump to the passed label.
	 */
	public void codeGen(String label) {
        if (exp != null) {
            // If a return value exists, pop into $v0
            exp.codeGen();
            Codegen.genPop(Codegen.V0);
        }
        // Jump to function's exit label
        Codegen.generate("j", label);
	}
    
    public void unparse(PrintWriter p, int indent) {
        printSpace(p, indent);
        p.print("return");
        if (exp != null) {
            p.print(" ");
            exp.unparse(p, 0);
        }
        p.println(";");
    }

    // 1 kid
    private ExpNode exp; // possibly null
}

// **********************************************************************
// ExpNode and its subclasses
// **********************************************************************

abstract class ExpNode extends ASTnode {
    /**
     * Default version for nodes with no names
     */
    public void nameAnalysis(SymTable symTab) { }
	
	/**
	 * Default version for non-IdNode
	 */
	public void genStore() { }
    
    abstract public Type typeCheck();
	abstract public void codeGen();
    abstract public int lineNum();
    abstract public int charNum();
}

class IntLitNode extends ExpNode {
    public IntLitNode(int lineNum, int charNum, int intVal) {
        this.lineNum = lineNum;
        this.charNum = charNum;
        this.intVal = intVal;
    }
    
    /**
     * Return the line number for this literal.
     */
    public int lineNum() {
        return lineNum;
    }
    
    /**
     * Return the char number for this literal.
     */
    public int charNum() {
        return charNum;
    }
        
    /**
     * typeCheck
     */
    public Type typeCheck() {
        return new IntType();
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
        // li    $t0, <value> 
        Codegen.generate("li", Codegen.T0, intVal);
        // Push onto stack
        Codegen.genPush(Codegen.T0);
	}
    
    public void unparse(PrintWriter p, int indent) {
        p.print(intVal);
    }

    private int lineNum;
    private int charNum;
    private int intVal;
}

class StringLitNode extends ExpNode {
    private static final Map<String, String> strTab = new HashMap<>();
    public StringLitNode(int lineNum, int charNum, String strVal) {
        this.lineNum = lineNum;
        this.charNum = charNum;
        this.strVal = strVal;
    }
    
    /**
     * Return the line number for this literal.
     */
    public int lineNum() {
        return lineNum;
    }
    
    /**
     * Return the char number for this literal.
     */
    public int charNum() {
        return charNum;
    }
    
    /**
     * typeCheck
     */
    public Type typeCheck() {
        return new StringType();
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
        // Search the hash table in case the literal already has a label
        String strLabel = strTab.get(strVal);

        if (strLabel == null) {
            // String literal is not preexisting so a label must be generated
            strLabel = Codegen.nextLabel();
            strTab.put(strVal, strLabel);
            Codegen.p.println("\t.data");
            Codegen.generateLabeled(strLabel, ".asciiz"+strVal, "");
        }

        Codegen.p.println("\t.text");
        // la   $t0, <label>
        Codegen.generate("la", Codegen.T0, strLabel);
        // push onto stack
        Codegen.genPush(Codegen.T0);
	}
	
    public void unparse(PrintWriter p, int indent) {
        p.print(strVal);
    }

    private int lineNum;
    private int charNum;
    private String strVal;
}

class TrueNode extends ExpNode {
    public TrueNode(int lineNum, int charNum) {
        this.lineNum = lineNum;
        this.charNum = charNum;
    }

    /**
     * Return the line number for this literal.
     */
    public int lineNum() {
        return lineNum;
    }
    
    /**
     * Return the char number for this literal.
     */
    public int charNum() {
        return charNum;
    }
    
    /**
     * typeCheck
     */
    public Type typeCheck() {
        return new BoolType();
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
        // li   $t0, 1
        Codegen.generate("li", Codegen.T0, 1);
        // push onto stack
        Codegen.genPush(Codegen.T0);
	}
	
    public void unparse(PrintWriter p, int indent) {
        p.print("true");
    }

    private int lineNum;
    private int charNum;
}

class FalseNode extends ExpNode {
    public FalseNode(int lineNum, int charNum) {
        this.lineNum = lineNum;
        this.charNum = charNum;
    }

    /**
     * Return the line number for this literal.
     */
    public int lineNum() {
        return lineNum;
    }
    
    /**
     * Return the char number for this literal.
     */
    public int charNum() {
        return charNum;
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        return new BoolType();
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
        // li   $t0, 0
        Codegen.generate("li", Codegen.T0, 0);
        // push onto stack
        Codegen.genPush(Codegen.T0);
	}
	
    public void unparse(PrintWriter p, int indent) {
        p.print("false");
    }

    private int lineNum;
    private int charNum;
}

class IdNode extends ExpNode {
    public IdNode(int lineNum, int charNum, String strVal) {
        this.lineNum = lineNum;
        this.charNum = charNum;
        this.strVal = strVal;
    }

    /**
     * Link the given symbol to this ID.
     */
    public void link(Sym sym) {
        this.sym = sym;
    }
    
    /**
     * Return the name of this ID.
     */
    public String name() {
        return strVal;
    }
    
    /**
     * Return the symbol associated with this ID.
     */
    public Sym sym() {
        return sym;
    }
    
    /**
     * Return the line number for this ID.
     */
    public int lineNum() {
        return lineNum;
    }
    
    /**
     * Return the char number for this ID.
     */
    public int charNum() {
        return charNum;
    }    
    
    // ** paramSize **
    // return the total number of bytes for all params
    public int paramSize() {
	return ((FnSym)sym).getParamSize();
    }

    // ** localSize **
    public int localSize() {
	return ((FnSym)sym).getLocalSize();
    }	
	
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - check for use of undeclared name
     * - if ok, link to symbol table entry
     */
    public void nameAnalysis(SymTable symTab) {
        Sym sym = symTab.lookupGlobal(strVal);
        if (sym == null) {
            ErrMsg.fatal(lineNum, charNum, "Undeclared identifier");
        } else {
            link(sym);
        }
    }
 
    /**
     * typeCheck
     */
    public Type typeCheck() {
        if (sym != null) {
            return sym.getType();
        } 
        else {
            System.err.println("ID with null sym field in IdNode.typeCheck");
            System.exit(-1);
        }
        return null;
    }
 
	/**
	 * codeGen
	 */
	public void codeGen() {
        if (sym.isGlobal()) {
            // lw $t0 _<name>
            Codegen.generate("lw", Codegen.T0, "_"+strVal);
        } else {
            // lw t0 <offset>(fp)
            Codegen.generateIndexed("lw", Codegen.T0, Codegen.FP, sym.getOffset());
        }
        // Push onto the stack
        Codegen.genPush(Codegen.T0);
    }
    
    public void genAddr() {
        if (sym.isGlobal()) {
            // la $t0, _<name>
            Codegen.generate("la", Codegen.T0, "_"+strVal);
        } else {
            // la $t0, <offset>(fp)
            Codegen.generateIndexed("la", Codegen.T0, Codegen.FP, sym.getOffset());
        }
        // Push onto the stack
        Codegen.genPush(Codegen.T0);
    }
	
	/**
	 * genStore - generate a store into this ID's location (when it is an L-value)
	 * from the top of the stack (leaving the stack unchanged)
	 */
	public void genStore() {
		Codegen.generateIndexed("lw", Codegen.T0, Codegen.SP, 4 , "peek");
		if (sym.isGlobal()) {
			Codegen.generate("sw", Codegen.T0, "_" + strVal);
		}
		
		else { // is local
			Codegen.generateIndexed("sw", Codegen.T0, Codegen.FP, sym.getOffset());
		}
	}
	
	/**
	 * genJumpAndLink
	 */
	public void genJumpAndLink() {
		Codegen.generate("jal", (isMain() ? "" : "_") + strVal);
	}
	
	/**
	 * genFnEntry
	 * only called when this ID is a fn name
	 */
	public void genFnEntry() {
		if (isMain()) {
			Codegen.generate(".globl main");
			Codegen.generateLabeled(strVal, "", "METHOD ENTRY");
			Codegen.generate("__start:");
		}
		else {
			Codegen.generateLabeled("_" + strVal, "", "METHOD ENTRY");
		}
    }

	/**
	 * isMain - is this function main?
	 */
    public boolean isMain() {
		return (strVal.equals("main"));
    }
		
    public void unparse(PrintWriter p, int indent) {
        p.print(strVal);
        if (sym != null) {
            p.print("(" + sym + ")");
        }
    }

    private int lineNum;
    private int charNum;
    private String strVal;
    private Sym sym;
}

class DotAccessExpNode extends ExpNode {
    public DotAccessExpNode(ExpNode loc, IdNode id) {
        this.loc = loc;    
        this.id = id;
        sym = null;
    }

    /**
     * Return the symbol associated with this dot-access node.
     */
    public Sym sym() {
        return sym;
    }    
    
    /**
     * Return the line number for this dot-access node. 
     * The line number is the one corresponding to the RHS of the dot-access.
     */
    public int lineNum() {
        return id.lineNum();
    }
    
    /**
     * Return the char number for this dot-access node.
     * The char number is the one corresponding to the RHS of the dot-access.
     */
    public int charNum() {
        return id.charNum();
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the LHS of the dot-access
     * - process the RHS of the dot-access
     * - if the RHS is of a struct type, set the sym for this node so that
     *   a dot-access "higher up" in the AST can get access to the symbol
     *   table for the appropriate struct definition
     */
    public void nameAnalysis(SymTable symTab) {
        badAccess = false;
        SymTable structSymTab = null; // to lookup RHS of dot-access
        Sym sym = null;
        
        loc.nameAnalysis(symTab);  // do name analysis on LHS
        
        // if loc is really an ID, then sym will be a link to the ID's symbol
        if (loc instanceof IdNode) {
            IdNode id = (IdNode)loc;
            sym = id.sym();
            
            // check ID has been declared to be of a struct type
            
            if (sym == null) { // ID was undeclared
                badAccess = true;
            }
            else if (sym instanceof StructSym) { 
                // get symbol table for struct type
                Sym tempSym = ((StructSym)sym).getStructType().sym();
                structSymTab = ((StructDefSym)tempSym).getSymTable();
            } 
            else {  // LHS is not a struct type
                ErrMsg.fatal(id.lineNum(), id.charNum(), 
                             "Dot-access of non-struct type");
                badAccess = true;
            }
        }
        
        // if loc is really a dot-access (i.e., loc was of the form
        // LHSloc.RHSid), then sym will either be
        // null - indicating RHSid is not of a struct type, or
        // a link to the Sym for the struct type RHSid was declared to be
        else if (loc instanceof DotAccessExpNode) {
            DotAccessExpNode loc = (DotAccessExpNode)this.loc;
            
            if (loc.badAccess) {  // if errors in processing loc
                badAccess = true; // don't continue proccessing this dot-access
            }
            else { //  no errors in processing loc
                sym = loc.sym();

                if (sym == null) {  // no struct in which to look up RHS
                    ErrMsg.fatal(loc.lineNum(), loc.charNum(), 
                                 "Dot-access of non-struct type");
                    badAccess = true;
                }
                else {  // get the struct's symbol table in which to lookup RHS
                    if (sym instanceof StructDefSym) {
                        structSymTab = ((StructDefSym)sym).getSymTable();
                    }
                    else {
                        System.err.println("Unexpected Sym type in DotAccessExpNode");
                        System.exit(-1);
                    }
                }
            }

        }
        
        else { // don't know what kind of thing loc is
            System.err.println("Unexpected node type in LHS of dot-access");
            System.exit(-1);
        }
        
        // do name analysis on RHS of dot-access in the struct's symbol table
        if (!badAccess) {
        
            sym = structSymTab.lookupGlobal(id.name()); // lookup
            if (sym == null) { // not found - RHS is not a valid field name
                ErrMsg.fatal(id.lineNum(), id.charNum(), 
                             "Invalid struct field name");
                badAccess = true;
            }
            
            else {
                id.link(sym);  // link the symbol
                // if RHS is itself as struct type, link the symbol for its struct 
                // type to this dot-access node (to allow chained dot-access)
                if (sym instanceof StructSym) {
                    sym = ((StructSym)sym).getStructType().sym();
                }
            }
        }
    }    
 
    /**
     * typeCheck
     */
    public Type typeCheck() {
        return id.typeCheck();
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
	}
    
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        loc.unparse(p, 0);
        p.print(")");
        p.print(".");
        id.unparse(p, 0);
    }

    // 2 kids
    private ExpNode loc;    
    private IdNode id;
    private Sym sym;          // link to Sym for struct type
    private boolean badAccess;  // to prevent multiple, cascading errors
}

class AssignNode extends ExpNode {
    public AssignNode(ExpNode lhs, ExpNode exp) {
        this.lhs = lhs;
        this.exp = exp;
    }
    
    /**
     * Return the line number for this assignment node. 
     * The line number is the one corresponding to the left operand.
     */
    public int lineNum() {
        return lhs.lineNum();
    }
    
    /**
     * Return the char number for this assignment node.
     * The char number is the one corresponding to the left operand.
     */
    public int charNum() {
        return lhs.charNum();
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's 
     * two children
     */
    public void nameAnalysis(SymTable symTab) {
        lhs.nameAnalysis(symTab);
        exp.nameAnalysis(symTab);
    }
 
    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type typeLhs = lhs.typeCheck();
        Type typeExp = exp.typeCheck();
        Type retType = typeLhs;
        
        if (typeLhs.isFnType() && typeExp.isFnType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Function assignment");
            retType = new ErrorType();
        }
        
        if (typeLhs.isStructDefType() && typeExp.isStructDefType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Struct name assignment");
            retType = new ErrorType();
        }
        
        if (typeLhs.isStructType() && typeExp.isStructType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Struct variable assignment");
            retType = new ErrorType();
        }        
        
        if (!typeLhs.equals(typeExp) && !typeLhs.isErrorType() && !typeExp.isErrorType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Type mismatch");
            retType = new ErrorType();
        }
        
        if (typeLhs.isErrorType() || typeExp.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
        // Push the address of the LHS onto the stack
        ((IdNode)lhs).genAddr();
        // Evalute rhs expression and put value on the stack
        exp.codeGen();

        // Store value into the address
        Codegen.genPop(Codegen.T0);
        Codegen.genPop(Codegen.T1);
        // sw $t0, 0($t1)
        Codegen.generateIndexed("sw", Codegen.T0, Codegen.T1, 0);

        // Push the value onto the stack
        Codegen.genPush(Codegen.T0);
	}
    
    public void unparse(PrintWriter p, int indent) {
        if (indent != -1)  p.print("(");
        lhs.unparse(p, 0);
        p.print(" = ");
        exp.unparse(p, 0);
        if (indent != -1)  p.print(")");
    }

    // 2 kids
    private ExpNode lhs;
    private ExpNode exp;
}

class CallExpNode extends ExpNode {
    public CallExpNode(IdNode name, ExpListNode elist) {
        id = name;
        expList = elist;
    }

    public CallExpNode(IdNode name) {
        id = name;
        expList = new ExpListNode(new LinkedList<ExpNode>());
    }

    /**
     * Return the line number for this call node. 
     * The line number is the one corresponding to the function name.
     */
    public int lineNum() {
        return id.lineNum();
    }
    
    /**
     * Return the char number for this call node.
     * The char number is the one corresponding to the function name.
     */
    public int charNum() {
        return id.charNum();
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's 
     * two children
     */
    public void nameAnalysis(SymTable symTab) {
        id.nameAnalysis(symTab);
        expList.nameAnalysis(symTab);
    }  
      
    /**
     * typeCheck
     */
    public Type typeCheck() {
        if (!id.typeCheck().isFnType()) {  
            ErrMsg.fatal(id.lineNum(), id.charNum(), 
                         "Attempt to call a non-function");
            return new ErrorType();
        }
        
        FnSym fnSym = (FnSym)(id.sym());
        
        if (fnSym == null) {
            System.err.println("null sym for Id in CallExpNode.typeCheck");
            System.exit(-1);
        }
        
        if (expList.size() != fnSym.getNumParams()) {
            ErrMsg.fatal(id.lineNum(), id.charNum(), 
                         "Function call with wrong number of args");
            return fnSym.getReturnType();
        }
        
        expList.typeCheck(fnSym.getParamTypes());
        return fnSym.getReturnType();
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
        // Evaluate each paramater, leaving their values on the stack
        expList.codeGen();
        // Jump to function and retain RA
        id.genJumpAndLink();
        // Push the return
        Codegen.genPush(Codegen.V0);
	}
	
    // ** unparse **
    public void unparse(PrintWriter p, int indent) {
        id.unparse(p, 0);
        p.print("(");
        if (expList != null) {
            expList.unparse(p, 0);
        }
        p.print(")");
    }

    // 2 kids
    private IdNode id;
    private ExpListNode expList;  // possibly null
}

abstract class UnaryExpNode extends ExpNode {
    public UnaryExpNode(ExpNode exp) {
        this.exp = exp;
    }
    
    /**
     * Return the line number for this unary expression node. 
     * The line number is the one corresponding to the  operand.
     */
    public int lineNum() {
        return exp.lineNum();
    }
    
    /**
     * Return the char number for this unary expression node.
     * The char number is the one corresponding to the  operand.
     */
    public int charNum() {
        return exp.charNum();
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        exp.nameAnalysis(symTab);
    }
    
    // one child
    protected ExpNode exp;
}

abstract class BinaryExpNode extends ExpNode {
    public BinaryExpNode(ExpNode exp1, ExpNode exp2) {
        this.exp1 = exp1;
        this.exp2 = exp2;
    }
    
    /**
     * Return the line number for this binary expression node. 
     * The line number is the one corresponding to the left operand.
     */
    public int lineNum() {
        return exp1.lineNum();
    }
    
    /**
     * Return the char number for this binary expression node.
     * The char number is the one corresponding to the left operand.
     */
    public int charNum() {
        return exp1.charNum();
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's 
     * two children
     */
    public void nameAnalysis(SymTable symTab) {
        exp1.nameAnalysis(symTab);
        exp2.nameAnalysis(symTab);
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
        // Evaluate both operands
        exp1.codeGen();
        exp2.codeGen();
        // pop RHS value into T1
        Codegen.genPop(Codegen.T1);
        // Pop LHS value into T0
        Codegen.genPop(Codegen.T0);

        // Do the operation
        // <opcode> t0, t0, t1
        Codegen.generate(opcode(), Codegen.T0, Codegen.T0, Codegen.T1);

        // Push the value onto the stack
        Codegen.genPush(Codegen.T0);
	}
	
	/**
	 * opcode - default version, i.e., error version
	 */
	public String opcode() {
		System.err.println("unexpected call to BinaryNode's opcode method");
		System.exit(-1);
		return null;
	}
    
    // two kids
    protected ExpNode exp1;
    protected ExpNode exp2;
}

// **********************************************************************
// Subclasses of UnaryExpNode
// **********************************************************************

class UnaryMinusNode extends UnaryExpNode {
    public UnaryMinusNode(ExpNode exp) {
        super(exp);
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type = exp.typeCheck();
        Type retType = new IntType();
        
        if (!type.isErrorType() && !type.isIntType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Arithmetic operator applied to non-numeric operand");
            retType = new ErrorType();
        }
        
        if (type.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
        // Evaluate the expression and store the value
        exp.codeGen();
        Codegen.genPop(Codegen.T0);

        // -1 * <value> == 0 - <value>

        // li   $t1, 0
        Codegen.generate("li", Codegen.T1, 0);
        // sub   $t0, 0, $t0
        Codegen.generate("sub", Codegen.T0, Codegen.T1, Codegen.T0);

        // Push the value onto the stack
        Codegen.genPush(Codegen.T0);
	}
	
    public void unparse(PrintWriter p, int indent) {
        p.print("(-");
        exp.unparse(p, 0);
        p.print(")");
    }
}

class NotNode extends UnaryExpNode {
    public NotNode(ExpNode exp) {
        super(exp);
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type = exp.typeCheck();
        Type retType = new BoolType();
        
        if (!type.isErrorType() && !type.isBoolType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Logical operator applied to non-bool operand");
            retType = new ErrorType();
        }
        
        if (type.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
        // Evaluate the expression and store the value
        exp.codeGen();
        Codegen.genPop(Codegen.T0);

        // If $t0 is equal to 0, set to 1, otherwise set to 0
        // seq, $t0, $t0, 0
        Codegen.generate("seq", Codegen.T0, Codegen.T0, Codegen.FALSE);

        // Push the value onto the stack
        Codegen.genPush(Codegen.T0);
	}
	
    public void unparse(PrintWriter p, int indent) {
        p.print("(!");
        exp.unparse(p, 0);
        p.print(")");
    }
}

// **********************************************************************
// Subclasses of BinaryExpNode
// **********************************************************************

abstract class ArithmeticExpNode extends BinaryExpNode {
    public ArithmeticExpNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type1 = exp1.typeCheck();
        Type type2 = exp2.typeCheck();
        Type retType = new IntType();
        
        if (!type1.isErrorType() && !type1.isIntType()) {
            ErrMsg.fatal(exp1.lineNum(), exp1.charNum(),
                         "Arithmetic operator applied to non-numeric operand");
            retType = new ErrorType();
        }
        
        if (!type2.isErrorType() && !type2.isIntType()) {
            ErrMsg.fatal(exp2.lineNum(), exp2.charNum(),
                         "Arithmetic operator applied to non-numeric operand");
            retType = new ErrorType();
        }
        
        if (type1.isErrorType() || type2.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }
}

abstract class LogicalExpNode extends BinaryExpNode {
    public LogicalExpNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type1 = exp1.typeCheck();
        Type type2 = exp2.typeCheck();
        Type retType = new BoolType();
        
        if (!type1.isErrorType() && !type1.isBoolType()) {
            ErrMsg.fatal(exp1.lineNum(), exp1.charNum(),
                         "Logical operator applied to non-bool operand");
            retType = new ErrorType();
        }
        
        if (!type2.isErrorType() && !type2.isBoolType()) {
            ErrMsg.fatal(exp2.lineNum(), exp2.charNum(),
                         "Logical operator applied to non-bool operand");
            retType = new ErrorType();
        }
        
        if (type1.isErrorType() || type2.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }
}

abstract class EqualityExpNode extends BinaryExpNode {
    public EqualityExpNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type1 = exp1.typeCheck();
        Type type2 = exp2.typeCheck();
        Type retType = new BoolType();
        
        if (type1.isVoidType() && type2.isVoidType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Equality operator applied to void functions");
            retType = new ErrorType();
        }
        
        if (type1.isFnType() && type2.isFnType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Equality operator applied to functions");
            retType = new ErrorType();
        }
        
        if (type1.isStructDefType() && type2.isStructDefType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Equality operator applied to struct names");
            retType = new ErrorType();
        }
        
        if (type1.isStructType() && type2.isStructType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Equality operator applied to struct variables");
            retType = new ErrorType();
        }        
        
        if (!type1.equals(type2) && !type1.isErrorType() && !type2.isErrorType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Type mismatch");
            retType = new ErrorType();
        }
        
        if (type1.isErrorType() || type2.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }
}

abstract class RelationalExpNode extends BinaryExpNode {
    public RelationalExpNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type1 = exp1.typeCheck();
        Type type2 = exp2.typeCheck();
        Type retType = new BoolType();
        
        if (!type1.isErrorType() && !type1.isIntType()) {
            ErrMsg.fatal(exp1.lineNum(), exp1.charNum(),
                         "Relational operator applied to non-numeric operand");
            retType = new ErrorType();
        }
        
        if (!type2.isErrorType() && !type2.isIntType()) {
            ErrMsg.fatal(exp2.lineNum(), exp2.charNum(),
                         "Relational operator applied to non-numeric operand");
            retType = new ErrorType();
        }
        
        if (type1.isErrorType() || type2.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }
}

class PlusNode extends ArithmeticExpNode {
    public PlusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
	public String opcode() { return "add"; }
	
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" + ");
        exp2.unparse(p, 0);
        p.print(")");
    }
}

class MinusNode extends ArithmeticExpNode {
    public MinusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
 
	public String opcode() { return "sub"; }
	
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" - ");
        exp2.unparse(p, 0);
        p.print(")");
    }
}

class TimesNode extends ArithmeticExpNode {
    public TimesNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

	public String opcode() { return "mulo"; }   
	
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" * ");
        exp2.unparse(p, 0);
        p.print(")");
    }
}

class DivideNode extends ArithmeticExpNode {
    public DivideNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
	
	public String opcode() { return "div"; }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" / ");
        exp2.unparse(p, 0);
        p.print(")");
    }
}

class AndNode extends LogicalExpNode {
    public AndNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
	/**
	 * codegen
	 */
	public void codeGen() {
        String exitLab = Codegen.nextLabel();

        // Evaluate the left operand and store the value
        exp1.codeGen();
        Codegen.genPop(Codegen.T0);
        // Push value back onto the stack
        Codegen.genPush(Codegen.T0);

        // If LHS is false, exit with expression's value as false. 
        // If LHS is true, fall through to RHS's evaluation.
        // beq  T0, 0, <label>
        Codegen.generate("beq", Codegen.T0, Codegen.FALSE, exitLab);
        // Fall through. RHS's evaluation will be value of expression.
	    Codegen.genPop(Codegen.T0);
        exp2.codeGen();

        // Exit label
        Codegen.genLabel(exitLab);
	}
	
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" && ");
        exp2.unparse(p, 0);
        p.print(")");
    }
}

class OrNode extends LogicalExpNode {
    public OrNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
 
	/**
	 * codegen
	 */
	public void codeGen() {
        String exitLab = Codegen.nextLabel();

        // Evaluate the left operand and store the value
        exp1.codeGen();
        Codegen.genPop(Codegen.T0);
        // Push value back onto the stack
        Codegen.genPush(Codegen.T0);

        // If LHS is true, exit with expression's value as true. 
        // If LHS is false, fall through to RHS's evaluation.
        // beq  T0, 1
        Codegen.generateWithComment("beq", "OrNode", Codegen.T0, Codegen.TRUE, exitLab);
        // Fall through. RHS's evaluation will be the value of the expression.
	    Codegen.genPop(Codegen.T0);
        exp2.codeGen();

        // Exit label
        Codegen.genLabel(exitLab);
	}
	
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" || ");
        exp2.unparse(p, 0);
        p.print(")");
    }
}

class EqualsNode extends EqualityExpNode {
    public EqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

	public String opcode() { return "seq"; }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" == ");
        exp2.unparse(p, 0);
        p.print(")");
    }
}

class NotEqualsNode extends EqualityExpNode {
    public NotEqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

	public String opcode() { return "sne"; }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" != ");
        exp2.unparse(p, 0);
        p.print(")");
    }
}

class LessNode extends RelationalExpNode {
    public LessNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

	public String opcode() { return "slt"; }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" < ");
        exp2.unparse(p, 0);
        p.print(")");
    }
}

class GreaterNode extends RelationalExpNode {
    public GreaterNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

	public String opcode() { return "sgt"; }
		
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" > ");
        exp2.unparse(p, 0);
        p.print(")");
    }
}

class LessEqNode extends RelationalExpNode {
    public LessEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

	public String opcode() { return "sle"; }
		
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" <= ");
        exp2.unparse(p, 0);
        p.print(")");
    }
}

class GreaterEqNode extends RelationalExpNode {
    public GreaterEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
	
	public String opcode() { return "sge"; }
	
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" >= ");
        exp2.unparse(p, 0);
        p.print(")");
    }
}
