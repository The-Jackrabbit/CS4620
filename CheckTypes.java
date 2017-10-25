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

import symtable.SymTable;
import symtable.Type;
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

   public void defaultOut(Node node) {
       System.err.println("Node not implemented in CheckTypes, " + node.getClass());
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
           (rexpType==Type.BYTE || rexpType==Type.INT)) || (lexpType==rexpType)
          ){
           this.mCurrentST.setExpType(node, Type.BOOL);
       } else {
           throw new SemanticException(
                   "Operands to == operator must match types or both be byte or int",
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

   //need to change this in PA4 so it will accept a byte type as an argument
   public void outMeggyDelay(MeggyDelay node) {
     if(this.mCurrentST.getExpType(node.getExp()) != Type.INT) {
       throw new SemanticException(
         "Invalid argument type for delay",
         node.getExp().getLine(), node.getExp().getPos());
     }

     this.mCurrentST.setExpType(node, Type.VOID);
   }

   
   public void outByteCast(ByteCast node) {
     if(this.mCurrentST.getExpType(node.getExp()) != Type.INT) {
       throw new SemanticException(
         "Invalid type for byte casting",
         node.getExp().getLine(), node.getExp().getPos());
     }

     this.mCurrentST.setExpType(node, Type.BYTE);
   }

   public void outIntLiteral(IntLiteral node) {
     this.mCurrentST.setExpType(node, Type.INT);
   }

   public void outColorLiteral(ColorLiteral node) {
     this.mCurrentST.setExpType(node, Type.COLOR);
   }

   public void outButtonLiteral(ButtonLiteral node) {
     this.mCurrentST.setExpType(node, Type.BUTTON);
   }
   
   public void outTrueLiteral(TrueLiteral node) {
     this.mCurrentST.setExpType(node, Type.BOOL);
   }

   public void outFalseLiteral(FalseLiteral node) {
     this.mCurrentST.setExpType(node, Type.BOOL);
   }

  
   public void outNotExp(NotExp node) {
	 Type ExpType = this.mCurrentST.getExpType(node.getExp());
     if(ExpType != Type.BOOL && ExpType != Type.INT && ExpType != Type.BYTE) {
       throw new SemanticException(
         "Invalid type for negation",
         node.getExp().getLine(), node.getExp().getPos());
     }

     this.mCurrentST.setExpType(node, Type.INT);
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
