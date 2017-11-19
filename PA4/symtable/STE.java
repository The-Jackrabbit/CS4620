package symtable;
import java.util.*;

public class STE {
	private String Name; //name of symbol this STE is for
	private STE EnclosedBy; //points to STE enclosing this STE. If enclosing scope is the global scope, EnclosedBy == null
	
	public STE(String Name) {
		this.Name = Name;
	}

	public String getName() {
		return Name;
	}

	public STE getEnclosedBy() {
		return EnclosedBy;
	}

	public STE setEnclosedBy(STE EnclosedBy) {
		this.EnclosedBy = EnclosedBy;
	}
}
