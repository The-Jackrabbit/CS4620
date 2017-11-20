package symtable;
import java.io.PrintWriter;
import java.io.PrintStream;
import java.util.*;
import ast.node.*;
import ast_visitors.*;
import exceptions.InternalException;

/** 
 * SymTable
 * ....
 * The symbol table also keeps a mapping of expression nodes to
 * types because that information is needed elsewhere especially
 * when looking up method call information.
 * 
 * @author mstrout
 * WB: Simplified to only expression types
 */
public class SymTable {
    private HashMap<Node,Type> mExpType; //maps ast nodes to types for type checking
	private Scope GlobalScope;  //root of symbol table tree	
	private STE CurrentScope; //points to current scope in symbol table tree.
							  //CurrentScope == null when GlobalScope is the current scope
							  //note that CurrentScope does not point to the scope itself but to the STE containing the scope
	private int nodeCount = 0;
    public SymTable() {
		GlobalScope = new Scope();
		CurrentScope = null;
		mExpType = new HashMap<Node,Type>();
	 }
	 
	 public void outputDot(PrintWriter out) {
		DotSymbolTableVisitor stVisitor = new DotSymbolTableVisitor(out);
		out.println("digraph SymTable {\ngraph [rankdir=\"LR\"];\nnode [shape=record];");
		this.stDotRecurse(this.GlobalScope, stVisitor, out, this.nodeCount);
		out.println("}");
		out.close();
	 }

	 private void stDotRecurse(Scope theScope, DotSymbolTableVisitor stVisitor, PrintWriter out, int nodeCount) {
		HashMap<String,STE> scopes = theScope.getHashMap();
		stVisitor.dotScopeOutput(theScope, out, 0, -1, "root");
		/* UTTER NONSENSE
			int scopeRoot = this.nodeCount;
			this.nodeCount++;
			for (HashMap.Entry<String, STE> entry : scopes.entrySet()) {
				if (entry.getValue() instanceof VarSTE) {
					this.nodeCount++;
					stVisitor.dotVarSTEOutput((VarSTE) entry.getValue(), out, this.nodeCount);
					
				}
				else if (entry.getValue() instanceof MethodSTE) {
					
					stVisitor.dotMethodSTEOutput((MethodSTE) entry.getValue(), out, this.nodeCount);
					this.nodeCount++;
					Scope steScope = entry.getValue().getScope();
					this.stDotRecurse(steScope, stVisitor, out, this.nodeCount);
					
					
				}
				else if (entry.getValue() instanceof ClassSTE) {
					
					stVisitor.dotClassSTEOutput((ClassSTE) entry.getValue(), out, this.nodeCount);
					this.nodeCount++;
					Scope steScope = entry.getValue().getScope();
					this.stDotRecurse(steScope, stVisitor, out, this.nodeCount);
					
				}
			}

			for (HashMap.Entry<String, STE> entry : scopes.entrySet()) {
				out.print(scopeRoot);
				out.print(":<f" + Integer.toString(99999) + "> -> ");
				out.print(Integer.toString(1000000000) + ":<f0>;\n");
			}
		*/
	 }

	//get current scope
	public STE getCurrentScope() {
		return CurrentScope;
	}

	/* Looks up a symbol in symbol table.
	   Starts looking in innermost scope and then
	   looks in enclosing scopes. Returns null if symbol not found */
    public STE lookup(String sym) {
		STE found;
		for(STE scopeItr = CurrentScope; scopeItr != null; scopeItr = scopeItr.getEnclosedBy()) {
			if((found = scopeItr.getScope().getEnclosing(sym)) != null) {
				return found;
			}
		}
		found = GlobalScope.getEnclosing(sym);
		return found;
	}

	/* Looks up a symbol in the current scope only.
	   Returns null if symbol not found */
	public STE lookupCurrent(String sym) {
		if(CurrentScope == null) {
			return GlobalScope.getEnclosing(sym);
		}
		return CurrentScope.getScope().getEnclosing(sym);
	}

	/* Inserts an STE into the current scope */
	public void insert(STE ste) {
		ste.setEnclosedBy(CurrentScope);
		if(CurrentScope == null)
			GlobalScope.setEnclosing(ste.getName(), ste);
		else 
			CurrentScope.getScope().setEnclosing(ste.getName(), ste);
	}
	
	/* Looks up the given method or class STE and makes it
	   the current scope */
	public void enterScope(String id) {
		STE found = lookup(id);
		if(found != null)
			CurrentScope = found;
	}
	
	/* Exits current STE and moves the current scope up one level */
	public void exitScope() {
		if(CurrentScope != null)
			CurrentScope = CurrentScope.getEnclosedBy();
	}
    
    public void setExpType(Node exp, Type t)
    {
    	this.mExpType.put(exp, t);
    }
    
    public Type getExpType(Node exp)
    {
    	return this.mExpType.get(exp);
    }
	
	 public Type getType(IType node) {
		//if-else structure to determine what subclass of IType type is
		if(node instanceof BoolType) 
			return Type.BOOL;
		else if(node instanceof IntType)
			return Type.INT;
		else if(node instanceof ByteType)
			return Type.BYTE;
		else if(node instanceof ColorType)
			return Type.COLOR;
		else if(node instanceof VoidType)
			return Type.VOID;
		else if(node instanceof ToneType)
			return Type.TONE;
		else
			return null;
	}
}
