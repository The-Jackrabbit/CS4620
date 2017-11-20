/**
 * This ast walker generates dot output for the AST.  
 *
 * 6/06 - Modified from Brian Richard's ParserTest.Java.
 *        Michelle Strout
 */
package ast_visitors;

import java.io.PrintWriter;
import java.util.Stack;
import java.util.HashMap; 
import ast.visitor.DepthFirstVisitor;
import symtable.*;
import ast.node.*;

public class DotSymbolTableVisitor extends DepthFirstVisitor {
   private int nodeCount = 0;
   /** Constructor takes a PrintWriter, and stores in instance var. */
   public DotSymbolTableVisitor(PrintWriter out) {
      
	}
	
	public String printArrow(int a, int x, int b, int y, java.io.PrintWriter out) {
		// Prints graph arrow of the form:
		// a:<fx> -> b:<fy>;\n
		String arrow = Integer.toString(a) + ":";
		arrow += "<f" + Integer.toString(x) + "> -> ";
		arrow += Integer.toString(b) + ":";
		arrow += "<f" + Integer.toString(y) + ">;\n";
		out.print(arrow);

		return arrow;
	}
	
	public void dotScopeOutput(Scope currentScope, java.io.PrintWriter out, int nodeCount, int parentVal, String parentType) {
		int scopeCount = 1;
		int rootVal = this.nodeCount;
		HashMap<String,STE> scopes = currentScope.getHashMap();
		if (parentVal != -1 && (parentType.equals("class") || parentType.equals("method")) ) {
			int offset = 4;
			if (parentType == "method") {offset = 3;}
			String arr = this.printArrow(parentVal, offset, this.nodeCount, 0, out);
			// System.out.println("Printing: " + arr);
		}
		// "2 [label=" <f0> Scope | <f1> mDict\[rain\] | <f2> mDict\[inBounds\] "];"
			out.print(rootVal); 
			out.print(" [label=\"<f0> Scope | "); 
			for (HashMap.Entry<String, STE> entry : scopes.entrySet()) {
				out.print("<f" + Integer.toString(scopeCount) + "> mDict\\[" + entry.getKey() + "\\]");
				if (scopeCount < scopes.size()) {
					out.print(" | ");
				}
				scopeCount++;
			}
			out.println("\"];");
			scopeCount = 1;
			this.nodeCount++;
		//

		for (HashMap.Entry<String, STE> entry : scopes.entrySet()) {
			// "3:<f3> -> 4:<f0>;" points scope to its parent method or class
			if (entry.getValue() instanceof VarSTE) {
				this.dotVarSTEOutput((VarSTE) entry.getValue(), out, this.nodeCount, rootVal);
				this.nodeCount++;
			}
			else if (entry.getValue() instanceof MethodSTE) {
				if (parentVal != -1) {
					String arr = this.printArrow(parentVal + 1, scopeCount, this.nodeCount, 0, out);
					System.out.println("Printing: " + arr);
					
				}
				this.dotMethodSTEOutput((MethodSTE) entry.getValue(), out, this.nodeCount, rootVal);
				//this.printArrow(rootVal, 3, this.nodeCount, 0, out);
				rootVal = this.nodeCount;
				this.nodeCount++;
				
				Scope steScope = entry.getValue().getScope();
				this.dotScopeOutput(steScope, out, this.nodeCount, rootVal, "method");	
				
			}
			else if (entry.getValue() instanceof ClassSTE) {
				
				this.printArrow(parentVal + 1, scopeCount, this.nodeCount, 0, out);
				
				this.dotClassSTEOutput((ClassSTE) entry.getValue(), out, this.nodeCount, rootVal);
				// this.printArrow(parentVal, 4, this.nodeCount, 0, out);
				rootVal = this.nodeCount;
				this.nodeCount++;
				Scope steScope = entry.getValue().getScope();
				this.dotScopeOutput(steScope, out, this.nodeCount, rootVal, "class");
				
			}
			scopeCount++;
		}
		
	}

	public void dotVarSTEOutput(VarSTE ste, java.io.PrintWriter out, int nodeCount, int parentVal) {
		/* EXPECTED DOT GEN
			4:<f3> -> 7:<f0>;  
			7 [label=" <f0> VarSTE | <f1> mName = y| <f2> mType = BYTE| <f3> mBase = INVALID| <f4> mOffset = 0"];
		*/
		out.print(Integer.toString(parentVal) + ":<f" + Integer.toString(nodeCount - parentVal) + "> -> " + Integer.toString(nodeCount) + ":<f0>;\n");
		out.print(nodeCount); // this is fine
		out.print(" [ label=\" <f0> VarSTE"); 
		out.print(" | <f1> mName = " + ste.getName()); 
		out.print(" | <f2> mType = " + ste.getVarType()); 
		out.print(" | <f3> mBase = " + ste.getBase()); 
		out.print(" | <f4> mOffset = " + ste.getOffset()); 
		
		out.println("\" ];");
		
		//this.nodeCount++;
	}
	
	public void dotMethodSTEOutput(MethodSTE ste, java.io.PrintWriter out, int nodeCount, int parentVal) {
		/* EXPECTED DOT GEN
			3 [label=" <f0> MethodSTE | 
				<f1> mName = bluedot| 
				<f2> mSignature = (BYTE, BYTE) returns class_null;| 
				<f3> mScope "];
			3:<f3> -> 4:<f0>;
		*/
		String node = Integer.toString(nodeCount);
		node += " [label=\" <f0> MethodSTE";
		node += " | <f1> mName = " + ste.getName();
		node += " | <f2> mSignature = " + ste.getSig();
		node += " | <f3> mScope\" ];\n";
		out.print(node);  
		
   }

	public void dotClassSTEOutput(ClassSTE ste, java.io.PrintWriter out, int nodeCount, int parentVal) {
		/* EXPECTED DOT GEN
			1 [label=" <f0> ClassSTE | <f1> mName = Simple| <f2> mMain = false| <f3> mSuperClass = null| <f4> mScope "];
			1:<f4> -> 2:<f0>;
		*/
		out.print(nodeCount); // this is fine
		out.print(" [ label=\" <f0> ClassSTE"); 
		out.print(" | <f1> mName = " + ste.getName()); 
		out.print(" | <f2> mMain = false"); 
		out.print(" | <f3> mSuperClass = null"); 
		out.print(" | <f4> mScope"); 
		
		out.println("\" ];");
	}
	

   
}
