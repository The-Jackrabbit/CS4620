package symtable;
import java.util.*;

public class ClassSTE extends STE {
	private Scope scope; //points to the Scope object for this class, which will point to the enclosed methods and variables
	
	public VarSTE(String Name) {
		super(Name);
		this.scope = new Scope();
	}

	public char getScope() {
		return scope;
	}
}
