package symtable;

import exceptions.*;

import java.util.*;

import ast.node.*;

public class Type
{
  //primitive types
  public static final Type BOOL = new Type();
  public static final Type INT = new Type();
  public static final Type BYTE = new Type();
  public static final Type COLOR = new Type();
  public static final Type BUTTON = new Type();
  public static final Type VOID = new Type();
  public static final Type TONE = new Type();	

  private Type()
  {

  }

  //class type, real type is ClassObject.getClassName()
  public static class Class extends Type {
	private String ClassName;
	
	public Class(String ClassName) {
		this.ClassName = ClassName;
	}

	public String getClassName() {
		return ClassName;
	}
  }

  //The following nested classes ONLY WORK if methods take as arguments+return PRIMITIVE TYPES ONLY

  //method signature type
  public static class Signature {
	
	private Type ReturnType; //return type of the function
	private LinkedList<Type> Arguments; //list of arguments	
			
	//builds method signature from IType AST node and LinkedList of Formal AST nodes
  	public Signature(IType node, LinkedList<Formal> formals) {
		ReturnType = getType(node);
		Arguments = new LinkedList<Type>();
		for(int i = 0; i < formals.size(); i++)
			Arguments.add(getType(formals.get(i).getType()));
	}

	public Type getReturnType() {
		return ReturnType;
	}
	
	public LinkedList<Type> getArguments() {
		return Arguments;
	}

	//returns 1 if signatures are equal, 0 if unequal
	public boolean equals(Type.Signature sig) {
		if(this.ReturnType != sig.ReturnType)
			return false;
		if(this.Arguments.size() != sig.Arguments.size())
			return false;
		for(int i = 0; i < this.Arguments.size(); i++) {
			if(this.Arguments.get(i) != sig.Arguments.get(i))
				return false;
		}
		return true;
	}
	
	//returns correct Type object given an IType node of some type subclass
	private Type getType(IType node) {
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

    
/*
*/

  public String toString()
  {
    if(this == INT)
    {
      return "INT";
    }

    if(this == BOOL)
    {
      return "BOOL";
    }

    if(this == BYTE)
    {
      return "BYTE";
    }

    if(this == COLOR)
    {
      return "COLOR";
    }

    if(this == BUTTON)
    {
      return "BUTTON";
    }
	
	if(this == TONE)
    {
      return "TONE";
    }

	if(this instanceof Type.Class)
	{
		return "class_" + ((Type.Class) this).getClassName() + ";";
	}

    
/*
*/
    return "MAINCLASS;";
  }
  
  public int getAVRTypeSize() {
      if(this == INT) { return 2; }
      if(this == BOOL) { return 1; }
      if(this == BYTE) { return 1; }
      if(this == COLOR) { return 1; }
      if(this == BUTTON) { return 1; }
      if(this == VOID) { return 0; }
	  if(this == TONE) { return 2; }

      return 2; // class references are 2 bytes
  }

    
/*  
*/

}
