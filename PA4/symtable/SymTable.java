package symtable;
import java.util.*;
import ast.node.*;

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
    private final HashMap<Node,Type> mExpType = new HashMap<Node,Type>(); //maps ast nodes to types for type checking
	private Scope GlobalScope;  //root of symbol table tree	
	private STE CurrentScope; //points to current scope in symbol table tree

    public SymTable() {
		GlobalScope = null;
		CurrentScope = null;
    }

	//get current scope
	public STE getCurrentScope() {
		return CurrentScope;
	}

	/* Looks up a symbol in symbol table.
	   Starts looking in innermost scope and then
	   looks in enclosing scopes. Returns null if symbol not found */
    public STE lookup(String sym) {
		
	}

	/* Looks up a symbol in the current scope only.
	   Returns null if symbol not found */
	public STE lookupCurrent(String sym) {
		
	}

	/* Inserts an STE into the current scope */
	public void insert(STE ste) {
		
	}
	
	/* Looks up the given method or class STE and makes it
	   the current scope */
	public void enterScope(String id) {
		
	}
	
	/* Exits current STE and moves the current scope up one level */
	public void exitScope() {
		
	}
    
    public void setExpType(Node exp, Type t)
    {
    	this.mExpType.put(exp, t);
    }
    
    public Type getExpType(Node exp)
    {
    	return this.mExpType.get(exp);
    }

}
