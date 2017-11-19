package symtable;
import java.util.*;

public class ClassSTE extends STE {
	private Scope scope; //points to the Scope object for this class, which will point to the enclosed methods and variables
	
	public VarSTE(String Name, Scope scope) {
		super(Name);
		this.scope = scope;
	}

	public char getScope() {
		return scope;
	}
}
