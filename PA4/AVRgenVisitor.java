package ast_visitors;
import java.io.PrintWriter;

import ast.node.*;
import ast.visitor.DepthFirstVisitor;
import java.util.*;
import symtable.*;

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
	 int regHigh = num+1; //register # for high bits
	 int regLow = num; //register # for low bits

     if(nodeType == Type.BYTE || nodeType == Type.COLOR || nodeType == Type.BOOL) {
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

   public void inMainClass(MainClass node) {
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

   public void outMainClass(MainClass node) {
    out.println("/* epilogue start */");
    out.println("endLabel:");
    out.println("jmp endLabel");
    out.println("ret");
    out.println(".size   main, .-main");
   }

   public void outProgram(Program node) {
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
     promoteAndLoad(this.mCurrentST.getExpType(node.getExp()), 24); //promote operand if needed and load
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
	   promoteAndLoad(this.mCurrentST.getExpType(node.getRExp()), 18); //promote right operand if needed and load
	   promoteAndLoad(this.mCurrentST.getExpType(node.getLExp()), 24); //promote left operand if needed and load
       out.println("# Do add operation");
       out.println("add    r24, r18");
       out.println("adc    r25, r19");
       out.println("# push two byte expression onto stack");
       out.println("push   r25");
       out.println("push   r24");
   }
  
   public void outMinusExp(MinusExp node) {
	   //right operand must be loaded first because it will be on top of stack (it is evaluated second)
	   promoteAndLoad(this.mCurrentST.getExpType(node.getRExp()), 18); //promote right operand if needed and load
   	   promoteAndLoad(this.mCurrentST.getExpType(node.getLExp()), 24); //promote left operand if needed and load
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
	 promoteAndLoad(this.mCurrentST.getExpType(node.getExp()), 24); //promote operand if needed and load
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
	   promoteAndLoad(this.mCurrentST.getExpType(node.getRExp()), 18); //promote right operand if needed and load
       promoteAndLoad(this.mCurrentST.getExpType(node.getLExp()), 24); //promote left operand if needed and load
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
	//PA4 code gen starts here!
	static String currentClass = null; //name of current class

	public void inTopClassDecl(TopClassDecl node)
    {
		//keep symbol table updated with what scope we're in
		this.mCurrentST.enterScope(node.getName());
		currentClass = node.getName();
		//no code generated for class decl
    }

    public void outTopClassDecl(TopClassDecl node)
    {
		//keep symbol table updated with what scope we're in
        this.mCurrentST.exitScope();
		currentClass = null;
		//no code generated for class decl
    }

	

	public void visitMethodDecl(MethodDecl node)
    {
		MethodSTE method; //will hold the verified STE for the called method        
		this.mCurrentST.enterScope(node.getName());
		method = (MethodSTE) this.mCurrentST.getCurrentScope();
	
        inMethodDecl(node, method);
        if(node.getType() != null)
        {
            node.getType().accept(this);
        }
        {
            List<Formal> copy = new ArrayList<Formal>(node.getFormals());
            for(Formal e : copy)
            {
                e.accept(this);
            }
        }
        {
            List<VarDecl> copy = new ArrayList<VarDecl>(node.getVarDecls());
            for(VarDecl e : copy)
            {
                e.accept(this);
            }
        }
        {
            List<IStatement> copy = new ArrayList<IStatement>(node.getStatements());
            for(IStatement e : copy)
            {
                e.accept(this);
            }
        }
        if(node.getExp() != null)
        {
            node.getExp().accept(this);
        }
        outMethodDecl(node, method);
		this.mCurrentST.exitScope();
    }

	public void inMethodDecl(MethodDecl node, MethodSTE ste)
    {
		out.println(".text");
		out.println(".global   " + currentClass + "_" + node.getName());
    	out.println(".type   " + currentClass + "_" + node.getName() + ", @function");
		out.println(currentClass + "_" + node.getName() + ":");
    	out.println("	push   r29");
    	out.println("	push   r28");
    	out.println("	# make space for locals and params");
		out.println("ldi    r30, 0");
		LinkedList<Type> Params = ste.getSig().getArguments();
		//handle implied this param
		out.println("push   r30");
		out.println("push   r30");	
		for(int i = 0; i < Params.size(); i++) {
			for(int j = 0; j < Params.get(i).getAVRTypeSize(); j++)
				out.println("push   r30");
		}
		out.println("# Copy stack pointer to frame pointer");
    	out.println("in     r28,__SP_L__");
    	out.println("in     r29,__SP_H__");
    	out.println("# save off parameters");
		VarSTE var; //will hold the verified STE for the referenced var
		//handle implied this param
		out.println("std    Y + 2, r25");
    	out.println("std    Y + 1, r24");
		for(int i = 0; i < Params.size(); i++) {
			var = (VarSTE) this.mCurrentST.lookup(node.getFormals().get(i).getName());
			for(int j = Params.get(i).getAVRTypeSize()-1; j >= 0; j--) {
				out.println("std    Y + " + (var.getOffset()+j) + ", " + "r" + ((22+j)-(2*i)) );
			}
		}
		out.println("/* done with function " + currentClass + "_" + node.getName() + " prologue */");
    }

    public void outMethodDecl(MethodDecl node, MethodSTE ste)
    {
		out.println("/* epilogue start for " + currentClass + "_" + node.getName() + " */");
		if(ste.getSig().getReturnType().equals(Type.VOID))
    		out.println("# no return value");
		else {
			out.println("# handle return value");
    		out.println("# load a " + ste.getSig().getReturnType().getAVRTypeSize() + " byte expression off stack");
			for(int i=0; i < ste.getSig().getReturnType().getAVRTypeSize(); i++) {
				out.println("pop    r" + (24+i) );
			}
		}
    	out.println("# pop space off stack for parameters and locals");
		LinkedList<Type> Params = ste.getSig().getArguments();
    	//handle implied this param
		out.println("pop   r30");
		out.println("pop  r30");	
		for(int i = 0; i < Params.size(); i++) {
			for(int j = 0; j < Params.get(i).getAVRTypeSize(); j++)
				out.println("pop   r30");
		}
    	out.println("# restoring the frame pointer");
    	out.println("pop    r28");
    	out.println("pop    r29");
    	out.println("ret");
    	out.println(".size " + currentClass + "_" + node.getName() + ", .-" + currentClass + "_" + node.getName());
    }

	public void outIdLiteral(IdLiteral node)
    {
		VarSTE var; //will hold the verified STE for the referenced var
		var = (VarSTE) this.mCurrentST.lookup(node.getLexeme());
		out.println(" # load a " + var.getVarType().getAVRTypeSize() + " byte variable from base+offset");
		for(int i=var.getVarType().getAVRTypeSize()-1; i >= 0; i--) {
			out.println("ldd    r" + (24+i) + "," + " " + var.getBase() + " + " + (var.getOffset()+i) );
		}
		out.println("# push " + var.getVarType().getAVRTypeSize() + " byte expression onto stack");
		for(int i=var.getVarType().getAVRTypeSize()-1; i >= 0; i--) {
			out.println("push    r" + (24+i) );
		}
    }

	public void visitCallExp(CallExp node)
    {
		MethodSTE method; //will hold the verified STE for the called method
        //check for undefined method or class
		if(node.getExp() instanceof ThisLiteral) {
			method = (MethodSTE) this.mCurrentST.lookup(node.getId());
		} else {
			STE savedScope = this.mCurrentST.getCurrentScope();
			this.mCurrentST.enterScope(((NewExp) node.getExp()).getId());
			method = (MethodSTE) this.mCurrentST.lookup(node.getId());
			if(savedScope == null)
				this.mCurrentST.enterScope(null);
			else
				this.mCurrentST.enterScope(savedScope.getName());
		} 
	
        inCallExp(node);
        if(node.getExp() != null)
        {
            node.getExp().accept(this);
        }
        {
            List<IExp> copy = new ArrayList<IExp>(node.getArgs());
            for(IExp e : copy)
            {
                e.accept(this);
            }
        }
        outCallExp(node, method);
		out.println("# handle return value");
    	out.println("# push " + method.getSig().getReturnType().getAVRTypeSize() + " byte expression onto stack");
		for(int i=method.getSig().getReturnType().getAVRTypeSize()-1; i >= 0; i--) {
			out.println("push    r" + (24+i) );
		}
    }

	public void visitCallStatement(CallStatement node)
    {
		MethodSTE method; //will hold the verified STE for the called method
        //check for undefined method or class
		if(node.getExp() instanceof ThisLiteral) {
			method = (MethodSTE) this.mCurrentST.lookup(node.getId());
		} else {
			STE savedScope = this.mCurrentST.getCurrentScope();
			this.mCurrentST.enterScope(((NewExp) node.getExp()).getId());
			method = (MethodSTE) this.mCurrentST.lookup(node.getId());
			if(savedScope == null)
				this.mCurrentST.enterScope(null);
			else
				this.mCurrentST.enterScope(savedScope.getName());
		} 

        inCallStatement(node);
        if(node.getExp() != null)
        {
            node.getExp().accept(this);
        }
        {
            List<IExp> copy = new ArrayList<IExp>(node.getArgs());
            for(IExp e : copy)
            {
                e.accept(this);
            }
        }
        outCallStatement(node, method);
    }

	public void outCallStatement(CallStatement node, MethodSTE ste)
    {	       
	   //promoteAndLoad(type, regLow); -need to call this whenever type is int
		out.println("#### function call");
    	out.println("# put parameter values into appropriate registers");
		LinkedList<Type> Params = ste.getSig().getArguments();
		for(int i = Params.size()-1; i >= 0; i--) {
			if(Params.get(i).equals(Type.INT)) {
				promoteAndLoad(Type.BYTE, ((24-2*Params.size())+(2*((Params.size()-1)-i))));
			} else {
				for(int j = Params.get(i).getAVRTypeSize()-1; j >= 0; j--) {
					out.println("pop   r" + (((24-2*Params.size())+j)+(2*((Params.size()-1)-i))) );
				}
			}
		}
		//handle implied this param
		out.println("pop r24");
    	out.println("pop r25");
		if(node.getExp() instanceof ThisLiteral) {
			out.println("call    " + currentClass + "_" + ste.getName());
		} else { 
    		out.println("call    " + ((NewExp) node.getExp()).getId() + "_" + ste.getName());
		}
    }

    public void outCallExp(CallExp node, MethodSTE ste)
    {
        //promoteAndLoad(type, regLow); -need to call this whenever type is int
		out.println("#### function call");
    	out.println("# put parameter values into appropriate registers");
		LinkedList<Type> Params = ste.getSig().getArguments();
		for(int i = Params.size()-1; i >= 0; i--) {
			if(Params.get(i).equals(Type.INT)) {
				promoteAndLoad(Type.BYTE, ((24-2*Params.size())+(2*((Params.size()-1)-i))));
			} else {
				for(int j = Params.get(i).getAVRTypeSize()-1; j >= 0; j--) {
					out.println("pop   r" + (((24-2*Params.size())+j)+(2*((Params.size()-1)-i))) );
				}
			}
		}
		//handle implied this param
		out.println("pop r24");
    	out.println("pop r25");
		if(node.getExp() instanceof ThisLiteral) {
			out.println("call    " + currentClass + "_" + ste.getName());
		} else { 
    		out.println("call    " + ((NewExp) node.getExp()).getId() + "_" + ste.getName());
		}
    }

	public void outThisExp(ThisLiteral node) {
		out.println("# loading the implicit \"this\"");
    	out.println("# load a two byte variable from base+offset");
    	out.println("ldd    r31, Y + 2");
    	out.println("ldd    r30, Y + 1");
    	out.println("# push two byte expression onto stack");
    	out.println("push   r31");
    	out.println("push   r30");
	}

	public void outNewExp(NewExp node) {
		out.println("# NewExp");
    	out.println("ldi    r24, lo8(0)");
    	out.println("ldi    r25, hi8(0)");
    	out.println("# allocating object of size 0 on heap");
    	out.println("call    malloc");
    	out.println("# push object address");
    	out.println("# push two byte expression onto stack");
    	out.println("push   r25");
    	out.println("push   r24");
	} 	
	
	public void outLtExp(LtExp node)
    {
		int index = labelIndex;
	    labelIndex += 3;
       	out.println("# less than expression");
    	//right operand must be loaded first because it will be on top of stack (it is evaluated second)
	   	promoteAndLoad(this.mCurrentST.getExpType(node.getRExp()), 18); //promote right operand if needed and load
	   	promoteAndLoad(this.mCurrentST.getExpType(node.getLExp()), 24); //promote left operand if needed and load
    	out.println("cp    r24, r18");
    	out.println("cpc   r25, r19");
    	out.println("brlt MJ_L"+(index+1));

    	out.println("# load false");
		out.println("MJ_L" + index + ":");
    	out.println("ldi     r24, 0");
    	out.println("jmp      MJ_L"+(index+2));

    	out.println("# load true");
		out.println("MJ_L" + (index+1) + ":");
    	out.println("ldi    r24, 1");

    	out.println("# push result of less than");
		out.println("MJ_L" + (index+2) + ":");
    	out.println("# push one byte expression onto stack");
    	out.println("push   r24");
    }

}
