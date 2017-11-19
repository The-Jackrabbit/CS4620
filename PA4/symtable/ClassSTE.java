package symtable;
import java.util.*;

public class ClassSTE extends STE {
	
	public VarSTE(String Name) {
		super(Name, new Scope());
	}
}
