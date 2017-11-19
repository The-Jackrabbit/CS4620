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
	private Stack ScopeStack;  //current scope stack
	

    public SymTable() {
		
    }

	/* Looks up a symbol in symbol table.
	   Starts looking in innermost scope and then
	   looks in enclosing scopes. Returns null if symbol not found */
    public STE lookup(String sym) {
		
	}

	/* Looks up a symbol in the innermost scope only.
	   Returns null if symbol not found */
	public STE lookupInnermost(String sym) {
		
	}

	/* Inserts an STE into scope at top of scope stack */
	public void insert(STE ste) {
		
	}
	
	/* Looks up the given method or class STE and makes it
	   the innermost scope (makes it top of the scope stack) */
	public void pushScope(String id) {
		
	}

	public void popScope() {
		
	}
    
    public void setExpType(Node exp, Type t)
    {
    	this.mExpType.put(exp, t);
    }
    
    public Type getExpType(Node exp)
    {
    	return this.mExpType.get(exp);
    }
   
/*
 */

}
