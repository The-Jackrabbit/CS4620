package symtable;
import java.util.*;

public class MethodSTE extends STE {
	private String Signature; //holds signature of method, in the form: "(Type, Type, ...) RETURNS Type" -spacing is exact
	private Scope scope; //points to the Scope object for this method, which will point to the enclosed methods and variables
	
	public VarSTE(String Name, String Signature) {
		super(Name);
		this.Signature = Signature;
		this.scope = new Scope();
	}

	public String getSignature() {
		return Signature;
	}
	
	public char getScope() {
		return scope;
	}
}
