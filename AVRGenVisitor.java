package ast_visitors;

import ast.node.*;
import ast.visitor.DepthFirstVisitor;
import java.util.*;

//extends DepthFirstVisitor, but overrides some visit methods to make AVR printing easier
public class AVRGenVisitor extends DepthFirstVisitor
{
   private int labelIndex = 0;  //keeps track of next available label index

   private PrintWriter out;
   public AVRGenVisitor(PrintWriter out) {
      this.out = out;
   }

   //========================= Overriding the visitor interface

   public void defaultOut(Node node) {
       System.err.println("Node not implemented in AVRGenVisitor, " + node.getClass());
   }
   
   public void visitAndExp(AndExp node)
   {
        if(node.getLExp() != null)
        {
            node.getLExp().accept(this);
        }
		inAndExp(node);
        if(node.getRExp() != null)
        {
            node.getRExp().accept(this);
        }
        outAndExp(node);
   }
	
   public void inAndExp(AndExp node)
   {
	 //left expression has been called at this point and value is on the stack
	 //save initial value of labelIndex and preincrement it to what will be the next unused label
	 int index = labelIndex;
	 labelIndex += 2;
     out.println("# &&: if left operand is false do not eval right");
     out.println("# load a one byte expression off stack");
     out.println("pop    r24");
     out.println("# push one byte expression onto stack");
     out.println("push   r24");
     out.println("# compare left exp with zero");
     out.println("ldi r25, 0");
     out.println("cp    r24, r25");
     out.println("# Want this, breq MJ_L"+index);
     out.println("brne  MJ_L"+(index+1));
     out.println("jmp   MJ_L"+index);
     out.println("MJ_L"+(index+1)+":");
   	 out.println("# right operand");
     out.println("# load a one byte expression off stack");
     out.println("pop    r24");
   }
	
   public void outAndExp(AndExp node) {
     //right expression has been called at this point and value is on the stack
     out.println("# load a one byte expression off stack");
     out.println("pop    r24");
     out.println("# push one byte expression onto stack");
     out.println("push   r24");

     out.println("MJ_L"+index+":");

     out.println("# load condition and branch if false");
     out.println("# load a one byte expression off stack");
     out.println("pop    r24");
     out.println("#load zero into reg");
     out.println("ldi    r25, 0");

     out.println("#use cp to set SREG");
     out.println("cp     r24, r25");
   }

   public void outTrueLiteral(TrueLiteral node) {
     out.println("# True/1 expression");
     out.println("ldi    r22, 1");
     out.println("# push one byte expression onto stack");
     out.println("push   r22");
   }

   public void outFalseLiteral(FalseLiteral node) {
     out.println("# False/0 expression");
     out.println("ldi    r24,0");
     out.println("# push one byte expression onto stack");
     out.println("push   r24");
   }

   public void visitIfStatement(IfStatement node)
    {
        //save initial value of labelIndex and preincrement it to what will be the next unused label
	    int index = labelIndex;
	    labelIndex += 3;
        inIfStatement(node);
        if(node.getExp() != null)
        {
            node.getExp().accept(this);
        }
		out.println("#WANT breq MJ_L"+index);
    	out.println("brne   MJ_L"+(index+1));
    	out.println("jmp    MJ_L"+index);
		out.println("# then label for if");
		out.println("MJ_L"+(index+1)+":");
        if(node.getThenStatement() != null)
        {
            node.getThenStatement().accept(this);
        }
	    out.println("jmp    MJ_L"+(index+2));
		out.println("# else label for if");
		out.println("MJ_L"+index+":");
        if(node.getElseStatement() != null)
        {
            node.getElseStatement().accept(this);
        }
		out.println("# done label for if");
	 	out.println("MJ_L"+(index+2)+":");
        outIfStatement(node);
    }

   public void inIfStatement(IfStatement node) {
     out.println("#### if statement");
   }

   public void inProgram(Program node) {
    out.println(".file  \"main.java\"");
	out.println("__SREG__ = 0x3f");
	out.println("__SP_H__ = 0x3e");
	out.println("__SP_L__ = 0x3d");
	out.println("__tmp_reg__ = 0");
	out.println("__zero_reg__ = 1");
    out.println(".global __do_copy_data");
    out.println(".global __do_clear_bss");
    out.println(".text");
	out.println(".global main");
    out.println(".type   main, @function");
	out.println("main:");
    out.println("push r29");
    out.println("push r28");
    out.println("in r28,__SP_L__");
    out.println("in r29,__SP_H__");
	out.println("/* prologue: function */");
    out.println("call _Z18MeggyJrSimpleSetupv");
    out.println("/* Need to call this so that the meggy library gets set up */");
   }

   public void outProgram(Program node) {
    out.println("/* epilogue start */");
    out.println("endLabel:");
    out.println("jmp endLabel");
    out.println("ret");
    out.println(".size   main, .-main");
   }

   /*
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

  
   public void outNotExp(NotExp node) {
	 Type ExpType = this.mCurrentST.getExpType(node.getExp());
     if(ExpType != Type.BOOL && ExpType != Type.INT && ExpType != Type.BYTE) {
       throw new SemanticException(
         "Invalid type for negation",
         node.getExp().getLine(), node.getExp().getPos());
     }

     this.mCurrentST.setExpType(node, Type.INT);
   }
   
   public void outWhileStatement(WhileStatement node) {
     if(this.mCurrentST.getExpType(node.getExp()) != Type.BOOL) {
       throw new SemanticException(
         "Invalid type for while statement",
         node.getExp().getLine(), node.getExp().getPos());
     }

     this.mCurrentST.setExpType(node, Type.VOID);
   }


 */






}
