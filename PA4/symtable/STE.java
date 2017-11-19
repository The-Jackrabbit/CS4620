package symtable;
import java.util.*;

public class STE {
	private String Name; //name of symbol this STE is for
	private STE EnclosedBy; //points to STE enclosing this STE. If enclosing scope is the global scope, EnclosedBy == null
	private Scope scope; //scope for the ste.  null for VarSTEs
	
	public STE(String Name, Scope scope) {
		this.Name = Name;
		this.scope = scope;
	}
	
	public Scope getScope() {
		return scope;
	}

	public String getName() {
		return Name;
	}

	public STE getEnclosedBy() {
		return EnclosedBy;
	}

	public void setEnclosedBy(STE EnclosedBy) {
		this.EnclosedBy = EnclosedBy;
	}
}
