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
    private HashMap<Node,Type> mExpType; //maps ast nodes to types for type checking
	private Scope GlobalScope;  //root of symbol table tree	
	private STE CurrentScope; //points to current scope in symbol table tree.
							  //CurrentScope == null when GlobalScope is the current scope
							  //note that CurrentScope does not point to the scope itself but to the STE containing the scope

    public SymTable() {
		GlobalScope = new Scope();
		CurrentScope = null;
		mExpType = new HashMap<Node,Type>();
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

}
