package symtable;
import java.util.*;

public class STE {
	private String Name; //name of symbol this STE is for
	private STE EnclosedBy; //points to STE enclosing this STE
	
	public STE(String Name, STE EnclosedBy) {
		this.Name = Name;
		this.EnclosedBy = EnclosedBy;
	}

	public String getName() {
		return Name;
	}

	public STE getEnclosedBy() {
		return EnclosedBy;
	}
}
