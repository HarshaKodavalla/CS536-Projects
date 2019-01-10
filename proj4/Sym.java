import java.util.List;

public class Sym {
    String type;
    
    public Sym(String type) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
    
    public String toString() {
        return type;
    }
}

class StructDeclSym extends Sym {
    private String type;
    private SymTable symTab;

    public StructDeclSym(String type, SymTable symTab) {
        super(null);
        this.type = type;
        this.symTab = symTab;
    }

    public String getType() {
        return type;
    }

    public SymTable getTable() {
        return symTab;
    }
}

class StructVarSym extends Sym {
    private String type;
    private String name;
    private StructDeclSym decl;

    public StructVarSym(String name, String type, StructDeclSym decl) {
        super(null);
        this.name = name;
        this.type = type;
        this.decl = decl;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public StructDeclSym getDecl() {
        return decl;
    }
}

class FnSym extends Sym {
    private String name;
    private List<String> paramTypes;
    private int numParams;
    private String retType;
    
    public FnSym(String name, String retType, List<String> paramTypes) {
        super(null);
        this.name = name;
        this.retType = retType;
        this.paramTypes = paramTypes;
        numParams = paramTypes.size();
    }

    public List<String> getParamTypes() {
        return paramTypes;
    }

    public String getName() {
        return name;
    }

    public int getNumParams() {
        return numParams;
    }

    public String getRType() {
        return retType;
    }

}
