package ast_visitors;
import java.io.PrintWriter;

import ast.node.*;
import ast.visitor.DepthFirstVisitor;
import java.util.*;
import symtable.SymTable;
import symtable.Type;

//extends DepthFirstVisitor, but overrides some visit methods to make AVR printing easier
public class AVRgenVisitor extends DepthFirstVisitor
{
   private int labelIndex = 0;  //keeps track of next available label index

   private PrintWriter out;
   private SymTable mCurrentST;

   public AVRgenVisitor(PrintWriter output, SymTable ST) {
	 out = output;
     mCurrentST = ST;
   }

   //promotes to int operands of operators that promote byte types to int
   //takes as arguments type of operand and if it is the first or second operand
   //(for proper reg alloc)
   private void promoteAndLoad(Type nodeType, int num) {
	 int regHigh; //register # for high bits
	 int regLow; //register # for low bits
	 if(num == 1) { //is first operand (below second on stack b/c it was evaluated first, must be LOADED second)
		regHigh = 25;
		regLow = 24;
	 } else { //num == 2, is second operand (above first on stack b/c it was evaluated second, must be LOADED first)
	    regHigh = 19;
		regLow = 18;
	 }
     if(nodeType == Type.BYTE || nodeType == Type.COLOR) {
		int index = labelIndex;
		labelIndex += 2;
		//pop byte and promote to int
        out.println("# load a one byte expression off stack");
    	out.println("pop    r"+regLow);
    	out.println("# promoting a byte to an int");
    	out.println("tst     r"+regLow);
    	out.println("brlt     MJ_L"+index);
    	out.println("ldi    r"+regHigh+", 0");
    	out.println("jmp    MJ_L"+(index+1));
		out.println("MJ_L"+index+":");
    	out.println("ldi    r"+regHigh+", hi8(-1)");
		out.println("MJ_L"+(index+1)+":");
	 } else {
	    //pop int
		out.println("# load a two byte expression off stack");
        out.println("pop    r"+regLow);
        out.println("pop    r"+regHigh);
	 }
   }

   //========================= Overriding the visitor interface

   /*public void defaultOut(Node node) {
       err.println("Node not implemented in AVRGenVisitor, " + node.getClass());
   }*/
   
   public void visitAndExp(AndExp node)
   {
        int index = labelIndex;
	 	labelIndex += 2;
		inAndExp(node);
        if(node.getLExp() != null)
        {
            node.getLExp().accept(this);
        }
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
        if(node.getRExp() != null)
        {
            node.getRExp().accept(this);
        }
		out.println("# load a one byte expression off stack");
     	out.println("pop    r24");
     	out.println("# push one byte expression onto stack");
     	out.println("push   r24");

     	out.println("MJ_L"+index+":");
		outAndExp(node);
   }
	

   public void outTrueExp(TrueLiteral node) {
     out.println("# True/1 expression");
     out.println("ldi    r22, 1");
     out.println("# push one byte expression onto stack");
     out.println("push   r22");
   }

   public void outFalseExp(FalseLiteral node) {
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
		out.println("# load condition and branch if false");
     	out.println("# load a one byte expression off stack");
     	out.println("pop    r24");
     	out.println("#load zero into reg");
     	out.println("ldi    r25, 0");

     	out.println("#use cp to set SREG");
     	out.println("cp     r24, r25");
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
	out.close();
   }

   public void visitWhileStatement(WhileStatement node)
    {
	    //save initial value of labelIndex and preincrement it to what will be the next unused label
	    int index = labelIndex;
	    labelIndex += 3;
        inWhileStatement(node);
		out.println("MJ_L"+index+":");
        if(node.getExp() != null)
        {
            node.getExp().accept(this);
        }
		out.println("# if not(condition)");
    	out.println("# load a one byte expression off stack");
    	out.println("pop    r24");
   		out.println("ldi    r25,0");
    	out.println("cp     r24, r25");
    	out.println("# WANT breq MJ_L"+(index+2));
    	out.println("brne   MJ_L"+(index+1));
    	out.println("jmp    MJ_L"+(index+2));
    	out.println("# while loop body");
		out.println("MJ_L"+(index+1)+":");
        if(node.getStatement() != null)
        {
            node.getStatement().accept(this);
        }
		out.println("# jump to while test");
    	out.println("jmp    MJ_L"+index);
    	out.println("# end of while");
		out.println("MJ_L"+(index+2)+":");
        outWhileStatement(node);
    }

   public void inWhileStatement(WhileStatement node) {
     out.println("#### while statement");
   }

   public void inMeggyDelay(MeggyDelay node) {
      out.println("### Meggy.delay() call");
      out.println("# load delay parameter");
   }

   public void outMeggyDelay(MeggyDelay node) {
      out.println("# load a two byte expression off stack");
      out.println("pop    r24");
      out.println("pop    r25");
      out.println("call   _Z8delay_msj");
   }

   public void outIntegerExp(IntLiteral node) {
    out.println("# Load constant int "+ node.getIntValue());
    out.println("ldi    r24,lo8("+node.getIntValue()+")");
    out.println("ldi    r25,hi8("+node.getIntValue()+")");
    out.println("# push two byte expression onto stack");
    out.println("push   r25");
    out.println("push   r24");
   }

   public void outColorExp(ColorLiteral node) {
     out.println("# Color expression" + node.getLexeme());
     out.println("ldi    r22," + node.getIntValue());
     out.println("# push one byte expression onto stack");
     out.println("push   r22");
   }

   //places value of button in r24
   public void outButtonExp(ButtonLiteral node) {
	 String lexeme = node.getLexeme();
     out.println("lds    r24, "+"Button_"+ lexeme.substring(lexeme.lastIndexOf(".")+1));
   }

   public void outByteCast(ByteCast node) {
     out.println("# Casting int to byte by popping");
     out.println("# 2 bytes off stack and only pushing low order bits");
     out.println("# back on.  Low order bits are on top of stack.");
     out.println("pop    r24");
     out.println("pop    r25");
     out.println("push   r24");
   }

   public void outMeggySetPixel(MeggySetPixel node) {
      out.println("### Meggy.setPixel(x,y,color) call");
      out.println("# load a one byte expression off stack");
      out.println("pop    r20");
      out.println("# load a one byte expression off stack");
      out.println("pop    r22");
      out.println("# load a one byte expression off stack");
      out.println("pop    r24");
      out.println("call   _Z6DrawPxhhh");
      out.println("call   _Z12DisplaySlatev");
   }

   
   public void outPlusExp(PlusExp node)
   {
	   //right operand must be loaded first because it will be on top of stack (it is evaluated second)
	   promoteAndLoad(this.mCurrentST.getExpType(node.getRExp()), 2); //promote right operand if needed and load
	   promoteAndLoad(this.mCurrentST.getExpType(node.getLExp()), 1); //promote left operand if needed and load
       out.println("# Do add operation");
       out.println("add    r24, r18");
       out.println("adc    r25, r19");
       out.println("# push two byte expression onto stack");
       out.println("push   r25");
       out.println("push   r24");
   }
  
   public void outMinusExp(MinusExp node) {
	   //right operand must be loaded first because it will be on top of stack (it is evaluated second)
	   promoteAndLoad(this.mCurrentST.getExpType(node.getRExp()), 2); //promote right operand if needed and load
   	   promoteAndLoad(this.mCurrentST.getExpType(node.getLExp()), 1); //promote left operand if needed and load
       out.println("# Do INT sub operation");
       out.println("sub    r24, r18");
       out.println("sbc    r25, r19");
       out.println("# push hi order byte first");
       out.println("# push two byte expression onto stack");
       out.println("push   r25");
       out.println("push   r24");
   } 
   
   public void outNegExp(NegExp node) {
	 out.println("# neg int");
	 promoteAndLoad(this.mCurrentST.getExpType(node.getExp()), 1); //promote operand if needed and load
     out.println("ldi     r22, 0");
     out.println("ldi     r23, 0");
     out.println("sub     r22, r24");
     out.println("sbc     r23, r25");
     out.println("# push two byte expression onto stack");
     out.println("push   r23");
     out.println("push   r22");
   }

   public void outMulExp(MulExp node) {
	   out.println("# MulExp");
       out.println("# load a one byte expression off stack");
       out.println("pop    r18");
       out.println("# load a one byte expression off stack");
       out.println("pop    r22");
       out.println("# move low byte src into dest reg");
       out.println("mov    r24, r18");
       out.println("# move low byte src into dest reg");
       out.println("mov    r26, r22");

       out.println("# Do mul operation of two input bytes");
       out.println("muls   r24, r26");
       out.println("# push two byte expression onto stack");
       out.println("push   r1");
       out.println("push   r0");
       out.println("# clear r0 and r1, thanks Brendan!");
       out.println("eor    r0,r0");
       out.println("eor    r1,r1");
   }
 
   
   public void outEqualExp(EqualExp node) {
	   //save initial value of labelIndex and preincrement it to what will be the next unused label
	   int index = labelIndex;
	   labelIndex += 3;
	   out.println("# equality check expression");
	   //right operand must be loaded first because it will be on top of stack (it is evaluated second)
	   promoteAndLoad(this.mCurrentST.getExpType(node.getRExp()), 2); //promote right operand if needed and load
       promoteAndLoad(this.mCurrentST.getExpType(node.getLExp()), 1); //promote left operand if needed and load
       out.println("cp    r24, r18");
       out.println("cpc   r25, r19");
       out.println("breq MJ_L"+(index+1));
       out.println("# result is false");
       out.println("MJ_L"+index+":");
       out.println("ldi     r24, 0");
       out.println("jmp      MJ_L"+(index+2));
       out.println("# result is true");
       out.println("MJ_L"+(index+1)+":");
       out.println("ldi     r24, 1");
       out.println("# store result of equal expression");
       out.println("MJ_L"+(index+2)+":");
       out.println("# push one byte expression onto stack");
       out.println("push   r24");
   }

   
   public void outMeggyGetPixel(MeggyGetPixel node) {
      out.println("### Meggy.getPixel(x,y) call");
      out.println("# load a one byte expression off stack");
      out.println("pop    r22");
      out.println("# load a one byte expression off stack");
      out.println("pop    r24");
      out.println("call   _Z6ReadPxhh");
      out.println("# push one byte expression onto stack");
      out.println("push   r24");
   }
   
   public void visitMeggyCheckButton(MeggyCheckButton node)
    {
		//save initial value of labelIndex and preincrement it to what will be the next unused label
	    int index = labelIndex;
	    labelIndex += 3;
        inMeggyCheckButton(node);
		out.println("### MeggyCheckButton");
        out.println("call    _Z16CheckButtonsDownv");
        if(node.getExp() != null)
        {
            node.getExp().accept(this);
        }
	    //assumes button literal places value of button in r24
        out.println("# if button value is zero, push 0 else push 1");
     	out.println("tst    r24");
     	out.println("breq   MJ_L"+index);
     	out.println("MJ_L"+(index+1)+":");
     	out.println("ldi    r24, 1");
     	out.println("jmp    MJ_L"+(index+2));
     	out.println("MJ_L"+index+":");
     	out.println("MJ_L"+(index+2)+":");
     	out.println("# push one byte expression onto stack");
     	out.println("push   r24");
        outMeggyCheckButton(node);
    }
 
   public void outNotExp(NotExp node) {
	 out.println("# not operation");
     out.println("# load a one byte expression off stack");
     out.println("pop    r24");
     out.println("ldi     r22, 1");
     out.println("eor     r24,r22");
     out.println("# push one byte expression onto stack");
     out.println("push   r24");
   }

}
