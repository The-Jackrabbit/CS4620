package symtable;
import java.util.*;

public class VarSTE extends STE {
	private Type varType; //holds type of variable
	private char Base; //holds base that offset is relative to, Y (for stack fp) or Z (for heap pointer)
	private int Offset; //holds offset in bytes from base. Variable is located at Base+Offset at runtime
	
	public VarSTE(String Name, STE EnclosedBy, Type varType, char Base, int Offset) {
		super(Name, EnclosedBy);
		this.varType = varType;
		this.Base = Base;
		this.Offset = Offset;
	}

	public Type getVarType() {
		return varType;
	}
	
	public char getBase() {
		return Base;
	}

	public int getOffset() {
		return Offset;
	}
}
