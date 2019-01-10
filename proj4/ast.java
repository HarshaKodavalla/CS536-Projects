import java.io.*;
import java.util.*;

// **********************************************************************
// The ASTnode class defines the nodes of the abstract-syntax tree that
// represents a cdull program.
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
//        IfStmtNode,      IfElseStmtNode,  WhileStmtNode,  RepeatStmtNode,
//        CallStmtNode
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
        for (int k = 0; k < indent; k++) p.print(" ");
    }
}

// **********************************************************************
// ProgramNode,  DeclListNode, FormalsListNode, FnBodyNode,
// StmtListNode, ExpListNode
// **********************************************************************

class ProgramNode extends ASTnode {
    public ProgramNode(DeclListNode L) {
        declList = L;
    }

    public void nameAnalysis(SymTable symTab) {
        symTab.addScope();
        declList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (Exception e) {
            System.out.println("Exception 0");
        }
        return;
    }

    public void unparse(PrintWriter p, int indent) {
        declList.unparse(p, indent);
    }

    // 1 kid
    private DeclListNode declList;
}

class DeclListNode extends ASTnode {
    public DeclListNode(List<DeclNode> S) {
        decls = S;
    }

    public void nameAnalysis(SymTable symTab) {
        Iterator it = decls.iterator();
        try {
            while (it.hasNext()) {
                ((DeclNode)it.next()).nameAnalysis(symTab);   
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.nameAnalysis");
            System.exit(-1);
        }
    }

    // Used to analyze the declarations within a struct
    public void nameAnalysis(SymTable symTab, SymTable data) {
        Iterator it = decls.iterator();
        try {
            while (it.hasNext()) {
                ((VarDeclNode)it.next()).nameAnalysis(symTab, data);   
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.nameAnalysis");
            System.exit(-1);
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
        formalDecls = S;
    }

    public void nameAnalysis(SymTable symTab) {
        Iterator<FormalDeclNode> it = formalDecls.iterator();
        while (it.hasNext()) {
            ((FormalDeclNode)it.next()).nameAnalysis(symTab);
        }
        return;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<FormalDeclNode> it = formalDecls.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        } 
    }

    // Create a list containing the types of all the formals
    public List<String> getTypes() {
        List<String> s = new ArrayList<String>();
        String temp;

        Iterator<FormalDeclNode> it = formalDecls.iterator();
        while (it.hasNext()) {
            temp = it.next().getType();
            s.add(temp);
        }
        return s;
    }

    // list of kids (FormalDeclNodes)
    private List<FormalDeclNode> formalDecls;
}

class FnBodyNode extends ASTnode {
    public FnBodyNode(DeclListNode declList, StmtListNode stmtList) {
        this.declList = declList;
        this.stmtList = stmtList;
    }

    public void nameAnalysis(SymTable symTab) {
        declList.nameAnalysis(symTab);
        stmtList.nameAnalysis(symTab);
        return;
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

    public void nameAnalysis(SymTable symTab) {
        Iterator<StmtNode> it = stmts.iterator();
        while (it.hasNext()) {  
            ((StmtNode)it.next()).nameAnalysis(symTab);
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

    public void nameAnalysis(SymTable symTab) {
        Iterator<ExpNode> it = exps.iterator();
        while (it.hasNext()) {  
            ((ExpNode)it.next()).nameAnalysis(symTab);
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
    abstract public void nameAnalysis(SymTable symTab);
}

class VarDeclNode extends DeclNode {
    public VarDeclNode(TypeNode type, IdNode id, int size) {
        this.type = type;
        this.id = id;
        this.size = size;
    }

    public void nameAnalysis(SymTable symTab) {
        if (type instanceof VoidNode) {
            ErrMsg.fatal(id.getLineNum(), id.getCharNum(), "Non-function declared void");
            return;
        }
        if (symTab.lookupLocal(id.getName()) != null) {
            ErrMsg.fatal(id.getLineNum(), id.getCharNum(), "Multiply declared identifier");
            return;
        }

        // Struct variable decls handled here
        if (type instanceof StructNode) {
            // Ensure that the struct has been declared
            Sym temp = symTab.lookupGlobal(type.getType());
            if (temp instanceof StructDeclSym) {
                try {
                    StructVarSym sym = new StructVarSym(id.getName(),
                                                type.getType(), (StructDeclSym) temp);
                    symTab.addDecl(id.getName(), sym);
                    id.link(sym);
                    return;
                } catch (Exception e) {
                    // Shouldn't happen
                    System.err.println(e + " occurred in VarDeclNode name analysis. Exiting.");
                    System.exit(-1);
                }
            } else {
                // Struct wasn't defined
                ErrMsg.fatal(id.getLineNum(), id.getCharNum(), "Invalid name of struct type");
                return;
            }
        }

        try {
            Sym sym = new Sym(type.getType());
            symTab.addDecl(id.getName(), sym);
            id.link(sym);
        } catch (Exception e) {
            // Shouldn't happen
            System.err.println(e + " occurred in VarDeclNode name analysis. Exiting.");
            System.exit(-1);
        }
        return;
    }

    // Used to analyze a single declaration within a struct
    public void nameAnalysis(SymTable symTab, SymTable data) {
        if (type instanceof VoidNode) {
            ErrMsg.fatal(id.getLineNum(), id.getCharNum(), "Non-function declared void");
            return;
        }
        if (data.lookupGlobal(id.getName()) != null) {
            ErrMsg.fatal(id.getLineNum(), id.getCharNum(), "Multiply declared identifier");
            return;
        }

        // If the node is a variable struct declaration, check if the struct has
        // been previously defined
        if (type instanceof StructNode) {
            Sym temp = symTab.lookupGlobal(type.getType());
            if (temp == null) {
            }
            if (temp instanceof StructDeclSym) {
                StructVarSym sym = new StructVarSym(id.getName(), temp.getType(), (StructDeclSym) temp);
                try {
                    data.addDecl(id.getName(), sym);
                    id.link(sym);
                } catch (Exception e) {
                    // Shouldn't happen
                    System.err.println(e + " occurred in VarDeclNode name analysis. Exiting.");
                    System.exit(-1);
                }
            } else {
                ErrMsg.fatal(id.getLineNum(), id.getCharNum(), "Invalid name of struct type");
            }
        } else {
            // Not a struct, add variable to the structure's symbol table
            try {
                Sym sym = new Sym(type.getType());
                data.addDecl(id.getName(), sym);
                id.link(sym);
            } catch (Exception e) {
                // Shouldn't happen
                System.err.println(e + " occurred in VarDeclNode name analysis. Exiting.");
                System.exit(-1);
            } 
        }
        return;
    }

    public void unparse(PrintWriter p, int indent) {
        printSpace(p, indent);
        type.unparse(p, 0);
        p.print(" ");
        id.unparseNoType(p, 0);
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
        formalsList = formalList;
        fnBody = body;
    }

    public void nameAnalysis(SymTable symTab) {
        // If a doubly declared function appears, do not add to symbol table
        // but process its formals and body.
        if (symTab.lookupLocal(id.getName()) != null) {
            ErrMsg.fatal(id.getLineNum(), id.getCharNum(), "Multiply declared identifier");
        } else {
            try {
                Sym sym = new FnSym(id.getName(), type.getType(), formalsList.getTypes());
                symTab.addDecl(id.getName(), sym);
                id.link(sym);
            } catch (Exception e) {
                System.err.println(e + " occurred in FnDeclNode name analysis. Exiting.");
                System.exit(-1);
            }
        }

        symTab.addScope();
        formalsList.nameAnalysis(symTab);
        fnBody.nameAnalysis(symTab);

        try {
            symTab.removeScope();
        } catch (EmptySymTableException e) {
            System.out.println("Unexpected EmptySymTableException occurred in FnDeclNode name" +
                                " analysis. Exiting.");
            System.exit(-1);
        }

        return;
    }

    public void unparse(PrintWriter p, int indent) {
        printSpace(p, indent);
        type.unparse(p, 0);
        p.print(" ");
        id.unparseNoType(p, 0);
        p.print("(");
        formalsList.unparse(p, 0);
        p.println(") {");
        fnBody.unparse(p, indent+4);
        p.println("}\n");
    }

    // 4 kids
    private TypeNode type;
    private IdNode id;
    private FormalsListNode formalsList;
    private FnBodyNode fnBody;
}

class FormalDeclNode extends DeclNode {
    public FormalDeclNode(TypeNode type, IdNode id) {
        this.type = type;
        this.id = id;
    }

    public void nameAnalysis(SymTable symTab) {
        if (type instanceof VoidNode) {
            ErrMsg.fatal(id.getLineNum(), id.getCharNum(), "Non-function declared void");
            return;
        }
        if (symTab.lookupLocal(id.getName()) != null) {
            ErrMsg.fatal(id.getLineNum(), id.getCharNum(), "Multiply declared identifier");
            return;
        }

        try {
            Sym sym = new Sym(type.getType());
            symTab.addDecl(id.getName(), sym);
            id.link(sym);
        } catch (Exception e) {
            // Shouldn't happen
            System.err.println(e + " occurred in FnDeclNode name analysis. Exiting.");
            System.exit(-1);
        }

        return;
    }

    public void unparse(PrintWriter p, int indent) {
        type.unparse(p, 0);
        p.print(" ");
        id.unparseNoType(p, 0);
    }

    public String getType() {
        return type.getType();
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

    public void nameAnalysis(SymTable symTab) {
        SymTable data = new SymTable();

        if (symTab.lookupGlobal(id.getName()) != null) {
            ErrMsg.fatal(id.getLineNum(), id.getCharNum(), "Multiply declared identifier");
            return;
        }

        StructDeclSym sym = new StructDeclSym(id.getName(), data);

        declList.nameAnalysis(symTab, data);

        try {
            symTab.addDecl(id.getName(), sym);
            id.link(sym);
        } catch (Exception e) {
            System.err.println(e + " occurred in StructDeclNode nameAnalysis. Exiting.");
            System.exit(-1);
        }
        return;
    }

    public void unparse(PrintWriter p, int indent) {
        printSpace(p, indent);
        p.print("struct ");
        id.unparseNoType(p, 0);
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
    abstract public String getType();
}

class IntNode extends TypeNode {
    public IntNode() {
    }

    public String getType() {
        return "int";
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("int");
    }
}

class BoolNode extends TypeNode {
    public BoolNode() {
    }

    public String getType() {
        return "bool";
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("bool");
    }
}

class VoidNode extends TypeNode {
    public VoidNode() {
    }

    public String getType() {
        return "void";
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("void");
    }
}

class StructNode extends TypeNode {
    public StructNode(IdNode id) {
        this.id = id;
    }

    public String getType() {
        return id.getName();
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("struct ");
        id.unparseNoType(p, 0);
    }
    
    // 1 kid
    private IdNode id;
}

// **********************************************************************
// StmtNode and its subclasses
// **********************************************************************

abstract class StmtNode extends ASTnode {
    abstract public void nameAnalysis(SymTable symTab);
}

class AssignStmtNode extends StmtNode {
    public AssignStmtNode(AssignNode assign) {
        this.assign = assign;
    }

    public void nameAnalysis(SymTable symTab) {
        assign.nameAnalysis(symTab);
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

    public void nameAnalysis(SymTable symTab) {
        exp.nameAnalysis(symTab);
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

    public void nameAnalysis(SymTable symTab) {
        exp.nameAnalysis(symTab);
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
    public ReadStmtNode(ExpNode exp) {
        this.exp = exp;
    }

    public void nameAnalysis(SymTable symTab) {
        exp.nameAnalysis(symTab);
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

    public void nameAnalysis(SymTable symTab) {
        exp.nameAnalysis(symTab);
    }

    public void unparse(PrintWriter p, int indent) {
        printSpace(p, indent);
        p.print("cout << ");
        exp.unparse(p, 0);
        p.println(";");
    }

    // 1 kid
    private ExpNode exp;
}

class IfStmtNode extends StmtNode {
    public IfStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        declList = dlist;
        this.exp = exp;
        stmtList = slist;
    }

    public void nameAnalysis(SymTable symTab) {
        exp.nameAnalysis(symTab);

        symTab.addScope();
        declList.nameAnalysis(symTab);
        stmtList.nameAnalysis(symTab);

        try {
            symTab.removeScope();
        } catch (EmptySymTableException e) {
            System.out.println("Unexpected EmptySymTableException thrown in IfStmtNode nameAnalysis.");
            System.exit(-1);
        }
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
    public IfElseStmtNode(ExpNode exp, DeclListNode thenDeclList,
                          StmtListNode thenStmtList, DeclListNode elseDeclList,
                          StmtListNode elseStmtList) {
        this.exp = exp;
        this.thenDeclList = thenDeclList;
        this.thenStmtList = thenStmtList;
        this.elseDeclList = elseDeclList;
        this.elseStmtList = elseStmtList;
    }

    public void nameAnalysis(SymTable symTab) {
        exp.nameAnalysis(symTab);

        symTab.addScope();
        thenDeclList.nameAnalysis(symTab);
        thenStmtList.nameAnalysis(symTab);

        try {
            symTab.removeScope();
        } catch (EmptySymTableException e) {
            System.out.println("Unexpected EmptySymTableException thrown in IfElseStmtNode nameAnalysis.");
            System.exit(-1);
        }

        symTab.addScope();
        elseDeclList.nameAnalysis(symTab);
        elseStmtList.nameAnalysis(symTab);

        try {
            symTab.removeScope();
        } catch (EmptySymTableException e) {
            System.out.println("Unexpected EmptySymTableException thrown in IfElseStmtNode nameAnalysis.");
            System.exit(-1);
        }

        return;

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
    
    public void nameAnalysis(SymTable symTab) {
        exp.nameAnalysis(symTab);

        symTab.addScope();
        declList.nameAnalysis(symTab);
        stmtList.nameAnalysis(symTab);

        try {
            symTab.removeScope();
        } catch (EmptySymTableException e) {
            System.out.println("Unexpected EmptySymTableException thrown in WhileStmtNode nameAnalysis.");
            System.exit(-1);
        }
        return;
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
    
    public void nameAnalysis(SymTable symTab) {
        exp.nameAnalysis(symTab);

        symTab.addScope();
        declList.nameAnalysis(symTab);
        stmtList.nameAnalysis(symTab);

        try {
            symTab.removeScope();
        } catch (EmptySymTableException e) {
            System.out.println("Unexpected EmptySymTableException thrown in RepeatStmtNode nameAnalysis.");
            System.exit(-1);
        }
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
        callExp = call;
    }

    public void nameAnalysis(SymTable symTab) {
        callExp.nameAnalysis(symTab);
    }

    public void unparse(PrintWriter p, int indent) {
        printSpace(p, indent);
        callExp.unparse(p, indent);
        p.println(";");
    }

    // 1 kid
    private CallExpNode callExp;
}

class ReturnStmtNode extends StmtNode {
    public ReturnStmtNode(ExpNode exp) {
        this.exp = exp;
    }

    public void nameAnalysis(SymTable symTab) {
        if (exp != null) {
            exp.nameAnalysis(symTab);
        }
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
    public void nameAnalysis(SymTable symTab) {}
}

class IntLitNode extends ExpNode {
    public IntLitNode(int lineNum, int charNum, int intVal) {
        this.lineNum = lineNum;
        this.charNum = charNum;
        this.intVal = intVal;
    }

    public void nameAnalysis(SymTable symTab) {}

    public void unparse(PrintWriter p, int indent) {
        p.print(intVal);
    }

    private int lineNum;
    private int charNum;
    private int intVal;
}

class StringLitNode extends ExpNode {
    public StringLitNode(int lineNum, int charNum, String strVal) {
        this.lineNum = lineNum;
        this.charNum = charNum;
        this.strVal = strVal;
    }

    public void nameAnalysis(SymTable symTab) {}

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

    public void nameAnalysis(SymTable symTab) {}

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

    public void nameAnalysis(SymTable symTab) {}

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

    public void nameAnalysis(SymTable symTab) {
        Sym sym = symTab.lookupGlobal(strVal);

        if (sym == null) {
            ErrMsg.fatal(lineNum, charNum, "Undeclared identifier");
        } else {
            link(sym);
        }
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(strVal);
        if (sym != null) {
            p.print("(");
            p.print(sym.getType());
            p.print(")");
        } 
    }

    public void unparseNoType(PrintWriter p, int indent) {
        p.print(strVal);
    }

    public int getLineNum() {
        return lineNum;
    }

    public int getCharNum() {
        return charNum;
    }

    public Sym getSym() {
        return sym;
    }

    public void link(Sym sym) {
        this.sym = sym;
    }

    public String getType() {
        return sym.getType();
    }

    public String getName() {
        return strVal;
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
    }

    public void nameAnalysis(SymTable symTab) {
        loc.nameAnalysis(symTab);

        Sym lhsSym = null;

        // Loc can be either a struct (ID)
        if (loc instanceof IdNode) {
            lhsSym = ((IdNode)loc).getSym();
        }
        // Or a nested DotAccess
        if (loc instanceof DotAccessExpNode) {
            // This resolves to a struct's sym
            lhsSym = ((DotAccessExpNode)loc).getID().getSym();
        }

        // Ensure LHS is a struct
        if (lhsSym instanceof StructVarSym) {
            // Check if struct field name is valid
            Sym sym = ((StructVarSym)lhsSym).getDecl().getTable().lookupGlobal(id.getName());
            if (sym == null) {
                ErrMsg.fatal(id.getLineNum(), id.getCharNum(), "Invalid struct field name");
            } else {
                id.link(sym);
            }
        } else {
            // not a struct
            ErrMsg.fatal(id.getLineNum(), id.getCharNum(), "Dot-access of non-struct type");
        }
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        loc.unparse(p, 0);
        p.print(").");
        id.unparse(p, 0);
    }

    public IdNode getID() {
        return id;
    }

    // 2 kids
    private ExpNode loc;    
    private IdNode id;
}

class AssignNode extends ExpNode {
    public AssignNode(ExpNode lhs, ExpNode exp) {
        this.lhs = lhs;
        this.exp = exp;
    }

    public void nameAnalysis(SymTable symTab) {
        lhs.nameAnalysis(symTab);
        exp.nameAnalysis(symTab);
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

    public void nameAnalysis(SymTable symTab) {
        id.nameAnalysis(symTab);
        expList.nameAnalysis(symTab);
    }

    // ** unparse **
    public void unparse(PrintWriter p, int indent) {
        id.unparseNoType(p, 0);

        p.print("(");
        boolean first = true;
        FnSym sym = (FnSym)(id.getSym());
        List<String> paramTypes = sym.getParamTypes();
        Iterator it = paramTypes.iterator();
        while (it.hasNext()) {
            if (first) {
                first = false;
            } else {
                p.print(",");
            }
            p.print(it.next());
        }
        p.print("->" + sym.getRType());
        p.print(")");

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

    public void nameAnalysis(SymTable symTab) {
        exp1.nameAnalysis(symTab);
        exp2.nameAnalysis(symTab);
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

    public void unparse(PrintWriter p, int indent) {
        p.print("(!");
        exp.unparse(p, 0);
        p.print(")");
    }
}

// **********************************************************************
// Subclasses of BinaryExpNode
// **********************************************************************

class PlusNode extends BinaryExpNode {
    public PlusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" + ");
        exp2.unparse(p, 0);
        p.print(")");
    }
}

class MinusNode extends BinaryExpNode {
    public MinusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" - ");
        exp2.unparse(p, 0);
        p.print(")");
    }
}

class TimesNode extends BinaryExpNode {
    public TimesNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" * ");
        exp2.unparse(p, 0);
        p.print(")");
    }
}

class DivideNode extends BinaryExpNode {
    public DivideNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" / ");
        exp2.unparse(p, 0);
        p.print(")");
    }
}

class AndNode extends BinaryExpNode {
    public AndNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" && ");
        exp2.unparse(p, 0);
        p.print(")");
    }
}

class OrNode extends BinaryExpNode {
    public OrNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" || ");
        exp2.unparse(p, 0);
        p.print(")");
    }
}

class EqualsNode extends BinaryExpNode {
    public EqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" == ");
        exp2.unparse(p, 0);
        p.print(")");
    }
}

class NotEqualsNode extends BinaryExpNode {
    public NotEqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" != ");
        exp2.unparse(p, 0);
        p.print(")");
    }
}

class LessNode extends BinaryExpNode {
    public LessNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" < ");
        exp2.unparse(p, 0);
        p.print(")");
    }
}

class GreaterNode extends BinaryExpNode {
    public GreaterNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" > ");
        exp2.unparse(p, 0);
        p.print(")");
    }
}

class LessEqNode extends BinaryExpNode {
    public LessEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" <= ");
        exp2.unparse(p, 0);
        p.print(")");
    }
}

class GreaterEqNode extends BinaryExpNode {
    public GreaterEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" >= ");
        exp2.unparse(p, 0);
        p.print(")");
    }
}
