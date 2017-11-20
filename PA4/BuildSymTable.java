package ast_visitors;

import ast.node.*;
import ast.visitor.DepthFirstVisitor;
import java.util.*;

import symtable.*;
import exceptions.InternalException;
import exceptions.SemanticException;

public class BuildSymTable extends DepthFirstVisitor
{
    
	private SymTable CurrentST;
	static int offset = 1; //offset variables in current method
	static String currentClass = null; //name of current class
   
	public BuildSymTable() {
   		CurrentST = new SymTable();
	}

	public SymTable getSymTable() {
		return CurrentST;
	}

	public void inTopClassDecl(TopClassDecl node)
    {
		//check for doubly-defined class
		if(CurrentST.lookupCurrent(node.getName()) != null)
			throw new SemanticException("Doubly Defined Class", node.getLine(), node.getPos());
 		//otherwise add new ClassSTE to current scope
		ClassSTE Class = new ClassSTE(node.getName());
        CurrentST.insert(Class);
		CurrentST.enterScope(node.getName());
		currentClass = node.getName();
    }

    public void outTopClassDecl(TopClassDecl node)
    {
        CurrentST.exitScope();
		currentClass = null;
    }

	public void inMethodDecl(MethodDecl node)
    {		
		//check for doubly-defined method
		if(CurrentST.lookupCurrent(node.getName()) != null)
			throw new SemanticException("Doubly Defined Method", node.getLine(), node.getPos());
		//otherwise add new MethodSTE to current scope
		MethodSTE Method = new MethodSTE(node.getName(), new Type.Signature(node.getType(), node.getFormals()));
		CurrentST.insert(Method);
		CurrentST.enterScope(node.getName());
		//add implied "this" VarSTE
		Type varType = new Type.Class(currentClass);
        VarSTE Formal = new VarSTE("this", varType, 'Y', offset);
		CurrentST.insert(Formal);
		offset += varType.getAVRTypeSize();
    }

    public void outMethodDecl(MethodDecl node)
    {
		offset = 1;
        CurrentST.exitScope();
    }

	public void inFormal(Formal node)
    {
		//check for doubly-defined variable
		if(CurrentST.lookupCurrent(node.getName()) != null)
			throw new SemanticException("Doubly Defined Variable", node.getLine(), node.getPos());
		//otherwise, add new VarSTE to current scope
		Type varType = CurrentST.getType(node.getType());
        VarSTE Formal = new VarSTE(node.getName(), varType, 'Y', offset);
		CurrentST.insert(Formal);
		offset += varType.getAVRTypeSize(); 
    }

}
