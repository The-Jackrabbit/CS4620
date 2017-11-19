package symtable;
import java.util.*;

public class MethodSTE extends STE {
	private Type.Signature Sig; //holds signature of method, in the form: "(Type, Type, ...) RETURNS Type" -spacing is exact
	
	public MethodSTE(String Name, Type.Signature Sig) {
		super(Name, new Scope());
		this.Sig = Sig;
	}

	public Type.Signature getSig() {
		return Sig;
	}
}
