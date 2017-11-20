package ast_visitors;

/** 
 * CheckTypes
 * 
 * This AST visitor traverses a MiniJava Abstract Syntax Tree and checks
 * for a number of type errors.  If a type error is found a SymanticException
 * is thrown
 * 
 * CHANGES to make next year (2012)
 *  - make the error messages between *, +, and - consistent <= ??
 *
 * Bring down the symtab code so that it only does get and set Type
 *  for expressions
 */

import ast.node.*;
import ast.visitor.DepthFirstVisitor;
import java.util.*;

import symtable.*;
import exceptions.InternalException;
import exceptions.SemanticException;

public class CheckTypes extends DepthFirstVisitor
{
    
   private SymTable mCurrentST;
   
   public CheckTypes(SymTable st) {
     if(st==null) {
          throw new InternalException("unexpected null argument");
      }
      mCurrentST = st;
   }
   
   //========================= Overriding the visitor interface

   /*public void defaultOut(Node node) {
       System.err.println("Node not implemented in CheckTypes, " + node.getClass());
   }*/

	public void inTopClassDecl(TopClassDecl node)
    {
		//keep symbol table updated with what scope we're in
		this.mCurrentST.enterScope(node.getName());
    }

    public void outTopClassDecl(TopClassDecl node)
    {
		//keep symbol table updated with what scope we're in
        this.mCurrentST.exitScope();
    }

	public void inMethodDecl(MethodDecl node)
    {
		//keep symbol table updated with what scope we're in		
		this.mCurrentST.enterScope(node.getName());
    }

    public void outMethodDecl(MethodDecl node)
    {
		//keep symbol table updated with what scope we're in
        this.mCurrentST.exitScope();
    }

	public void outIdLiteral(IdLiteral node)
    {
        //check for undefined literal, first need to get the varSTE it corresponds to
		STE result; //will hold the potential symbol table entry for the referenced var
		VarSTE var; //will hold the verified STE for the referenced var		
		if ((result = this.mCurrentST.lookup(node.getLexeme())) == null || !(result instanceof VarSTE))
			throw new SemanticException("Undefined variable", node.getLine(), node.getPos());	
		var = (VarSTE) result;
		//set the type of the IDLiteral to the type of its associated VarSTE
		this.mCurrentST.setExpType(node, var.getVarType());
    }

	public void outCallStatement(CallStatement node)
    {
        STE result; //will hold the potential symbol table entry for the called method
		MethodSTE method; //will hold the verified STE for the called method
        //check for undefined method or class
		if(node.getExp() instanceof ThisLiteral) {
			if((result = this.mCurrentST.lookup(node.getId())) == null || !(result instanceof MethodSTE))
				throw new SemanticException("Undefined method", node.getLine(), node.getPos());
			else
				method = (MethodSTE) result;
		} else if (!(node.getExp() instanceof NewExp) || (result = this.mCurrentST.lookup(((NewExp) (node.getExp())).getId())) == null 
					|| !(result instanceof ClassSTE)) {
						throw new SemanticException("Undefined class", node.getLine(), node.getPos());			
		} else {
			STE savedScope = this.mCurrentST.getCurrentScope();
			this.mCurrentST.enterScope(((NewExp) node.getExp()).getId());
			if((result = this.mCurrentST.lookup(node.getId())) == null || !(result instanceof MethodSTE))
				throw new SemanticException("Undefined method", node.getLine(), node.getPos());
			else
				method = (MethodSTE) result;
			if(savedScope == null)
				this.mCurrentST.enterScope(null);
			else
				this.mCurrentST.enterScope(savedScope.getName());
		} 
		//check that signature of method STE matches calling signature
		LinkedList<IExp> CallArgs = node.getArgs();
		LinkedList<Type> DeclArgs = method.getSig().getArguments();
		if(CallArgs.size() != DeclArgs.size())
			throw new SemanticException("Call signature mismatch", node.getLine(), node.getPos());
		//checks that types are equal unless a byte was PASSED INTO an int 
		for(int i = 0; i < DeclArgs.size(); i++) {
			if(!(DeclArgs.get(i).equals(this.mCurrentST.getExpType(CallArgs.get(i)))) 
				&& !(DeclArgs.get(i).equals(Type.INT) && this.mCurrentST.getExpType(CallArgs.get(i)).equals(Type.BYTE) ))
					throw new SemanticException("Call signature mismatch", node.getLine(), node.getPos());
		}
    }

    public void outCallExp(CallExp node)
    {
        STE result; //will hold the potential symbol table entry for the called method
		MethodSTE method; //will hold the verified STE for the called method
        //check for undefined method or class
		if(node.getExp() instanceof ThisLiteral) {
			if((result = this.mCurrentST.lookup(node.getId())) == null || !(result instanceof MethodSTE))
				throw new SemanticException("Undefined method", node.getLine(), node.getPos());
			else
				method = (MethodSTE) result;
		} else if (!(node.getExp() instanceof NewExp) || (result = this.mCurrentST.lookup(((NewExp) (node.getExp())).getId())) == null 
					|| !(result instanceof ClassSTE)) {
						throw new SemanticException("Undefined class", node.getLine(), node.getPos());			
		} else {
			STE savedScope = this.mCurrentST.getCurrentScope();
			this.mCurrentST.enterScope(((NewExp) node.getExp()).getId());
			if((result = this.mCurrentST.lookup(node.getId())) == null || !(result instanceof MethodSTE))
				throw new SemanticException("Undefined method", node.getLine(), node.getPos());
			else
				method = (MethodSTE) result;
			if(savedScope == null)
				this.mCurrentST.enterScope(null);
			else
				this.mCurrentST.enterScope(savedScope.getName());
		}
		//check that signature of method STE matches calling signature
		LinkedList<IExp> CallArgs = node.getArgs();
		LinkedList<Type> DeclArgs = method.getSig().getArguments();
		if(CallArgs.size() != DeclArgs.size())
			throw new SemanticException("Call signature mismatch", node.getLine(), node.getPos());
		//checks that types are equal unless a byte was PASSED INTO an int 
		for(int i = 0; i < DeclArgs.size(); i++) {
			if(!(DeclArgs.get(i).equals(this.mCurrentST.getExpType(CallArgs.get(i)))) 
				&& !(DeclArgs.get(i).equals(Type.INT) && this.mCurrentST.getExpType(CallArgs.get(i)).equals(Type.BYTE) ))
					throw new SemanticException("Call signature mismatch", node.getLine(), node.getPos());
		}
		
		//for call expression, set type of node to return type of method
		this.mCurrentST.setExpType(node, method.getSig().getReturnType());
    }

	public void outLtExp(LtExp node)
    {
       Type lexpType = this.mCurrentST.getExpType(node.getLExp());
       Type rexpType = this.mCurrentST.getExpType(node.getRExp());
       if ((lexpType==Type.INT  || lexpType==Type.BYTE) &&
           (rexpType==Type.INT  || rexpType==Type.BYTE)
          ){
           this.mCurrentST.setExpType(node, Type.BOOL);
       } else {
           throw new SemanticException(
                   "Operands to < operator must be INT or BYTE",
                   node.getLExp().getLine(),
                   node.getLExp().getPos());
       }
    }
   
   public void outAndExp(AndExp node)
   {
     if(this.mCurrentST.getExpType(node.getLExp()) != Type.BOOL) {
       throw new SemanticException(
         "Invalid left operand type for operator &&",
         node.getLExp().getLine(), node.getLExp().getPos());
     }

     if(this.mCurrentST.getExpType(node.getRExp()) != Type.BOOL) {
       throw new SemanticException(
         "Invalid right operand type for operator &&",
         node.getRExp().getLine(), node.getRExp().getPos());
     }

     this.mCurrentST.setExpType(node, Type.BOOL);
   }
  
   public void outPlusExp(PlusExp node)
   {
       Type lexpType = this.mCurrentST.getExpType(node.getLExp());
       Type rexpType = this.mCurrentST.getExpType(node.getRExp());
       if ((lexpType==Type.INT  || lexpType==Type.BYTE) &&
           (rexpType==Type.INT  || rexpType==Type.BYTE)
          ){
           this.mCurrentST.setExpType(node, Type.INT);
       } else {
           throw new SemanticException(
                   "Operands to + operator must be INT or BYTE",
                   node.getLExp().getLine(),
                   node.getLExp().getPos());
       }

   }
  
   
   public void outMinusExp(MinusExp node) {
   	   Type lexpType = this.mCurrentST.getExpType(node.getLExp());
       Type rexpType = this.mCurrentST.getExpType(node.getRExp());
       if ((lexpType==Type.INT  || lexpType==Type.BYTE) &&
           (rexpType==Type.INT  || rexpType==Type.BYTE)
          ){
           this.mCurrentST.setExpType(node, Type.INT);
       } else {
           throw new SemanticException(
                   "Operands to - operator must be INT or BYTE",
                   node.getLExp().getLine(),
                   node.getLExp().getPos());
       }
   } 

   public void outNegExp(NegExp node) {
	 Type ExpType = this.mCurrentST.getExpType(node.getExp());
     if(ExpType != Type.BYTE && ExpType != Type.INT) {
       throw new SemanticException(
         "Operands to unary - operator must be INT or BYTE",
         node.getExp().getLine(), node.getExp().getPos());
     }

     this.mCurrentST.setExpType(node, Type.INT);
   }


   
   public void outMulExp(MulExp node) {
	   Type lexpType = this.mCurrentST.getExpType(node.getLExp());
       Type rexpType = this.mCurrentST.getExpType(node.getRExp());
       if ((lexpType==Type.BYTE) &&
           (rexpType==Type.BYTE)
          ){
           this.mCurrentST.setExpType(node, Type.INT);
       } else {
           throw new SemanticException(
                   "Operands to * operator must be BYTE",
                   node.getLExp().getLine(),
                   node.getLExp().getPos());
       }
   }
 
   public void outEqualExp(EqualExp node) {
	   Type lexpType = this.mCurrentST.getExpType(node.getLExp());
       Type rexpType = this.mCurrentST.getExpType(node.getRExp());
       if (((lexpType==Type.BYTE || lexpType==Type.INT) &&
           (rexpType==Type.BYTE || rexpType==Type.INT)) || ((lexpType==rexpType) && (lexpType != Type.BUTTON) && (lexpType != Type.VOID))
          ){
           this.mCurrentST.setExpType(node, Type.BOOL);
       } else {
           throw new SemanticException(
                   "Invalid operand for ==",
                   node.getLExp().getLine(),
                   node.getLExp().getPos());
       }
   }

   public void outMeggyGetPixel(MeggyGetPixel node) {
      if(this.mCurrentST.getExpType(node.getXExp()) != Type.BYTE) {
       throw new SemanticException(
         "Invalid argument type for getPixel",
         node.getXExp().getLine(), node.getXExp().getPos());
     }

     if(this.mCurrentST.getExpType(node.getYExp()) != Type.BYTE) {
       throw new SemanticException(
         "Invalid argument type for getPixel",
         node.getYExp().getLine(), node.getYExp().getPos());
     }

     this.mCurrentST.setExpType(node, Type.COLOR);
   }

   public void outMeggyCheckButton(MeggyCheckButton node) {
     if(this.mCurrentST.getExpType(node.getExp()) != Type.BUTTON) {
       throw new SemanticException(
         "Invalid argument type for checkButton",
         node.getExp().getLine(), node.getExp().getPos());
     }

     this.mCurrentST.setExpType(node, Type.BOOL);
   }

   public void outMeggySetPixel(MeggySetPixel node) {
      if(this.mCurrentST.getExpType(node.getXExp()) != Type.BYTE) {
       throw new SemanticException(
         "Invalid argument type for setPixel",
         node.getXExp().getLine(), node.getXExp().getPos());
     }

     if(this.mCurrentST.getExpType(node.getYExp()) != Type.BYTE) {
       throw new SemanticException(
         "Invalid argument type for setPixel",
         node.getYExp().getLine(), node.getYExp().getPos());
     }

	 if(this.mCurrentST.getExpType(node.getColor()) != Type.COLOR) {
       throw new SemanticException(
         "Invalid argument type for setPixel",
         node.getColor().getLine(), node.getColor().getPos());
     }
	
     this.mCurrentST.setExpType(node, Type.VOID);
   }

   public void outMeggyDelay(MeggyDelay node) {
     if(this.mCurrentST.getExpType(node.getExp()) != Type.INT) {
       throw new SemanticException(
         "Invalid argument type for delay",
         node.getExp().getLine(), node.getExp().getPos());
     }

     this.mCurrentST.setExpType(node, Type.VOID);
   }

   
   public void outByteCast(ByteCast node) {
     if(this.mCurrentST.getExpType(node.getExp()) != Type.INT && this.mCurrentST.getExpType(node.getExp()) != Type.BYTE) {
       throw new SemanticException(
         "Invalid type for byte casting",
         node.getExp().getLine(), node.getExp().getPos());
     }

     this.mCurrentST.setExpType(node, Type.BYTE);
   }

   public void outIntegerExp(IntLiteral node) {
     this.mCurrentST.setExpType(node, Type.INT);
   }

   public void outColorExp(ColorLiteral node) {
     this.mCurrentST.setExpType(node, Type.COLOR);
   }

   public void outButtonExp(ButtonLiteral node) {
     this.mCurrentST.setExpType(node, Type.BUTTON);
   }
   
   public void outTrueExp(TrueLiteral node) {
     this.mCurrentST.setExpType(node, Type.BOOL);
   }

   public void outFalseExp(FalseLiteral node) {
     this.mCurrentST.setExpType(node, Type.BOOL);
   }

  
   public void outNotExp(NotExp node) {
	 Type ExpType = this.mCurrentST.getExpType(node.getExp());
     if(ExpType != Type.BOOL && ExpType != Type.INT && ExpType != Type.BYTE) {
       throw new SemanticException(
         "Invalid type for negation",
         node.getExp().getLine(), node.getExp().getPos());
     }

     this.mCurrentST.setExpType(node, Type.BOOL);
   }


   public void outIfStatement(IfStatement node) {
     if(this.mCurrentST.getExpType(node.getExp()) != Type.BOOL) {
       throw new SemanticException(
         "Invalid type for if statement",
         node.getExp().getLine(), node.getExp().getPos());
     }

     this.mCurrentST.setExpType(node, Type.VOID);
   }

   
   public void outWhileStatement(WhileStatement node) {
     if(this.mCurrentST.getExpType(node.getExp()) != Type.BOOL) {
       throw new SemanticException(
         "Invalid type for while statement",
         node.getExp().getLine(), node.getExp().getPos());
     }

     this.mCurrentST.setExpType(node, Type.VOID);
   }









}
