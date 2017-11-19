package symtable;
import java.util.*;

public class Scope {
	//maps STE method or variable names to the STEs they enclose
	private HashMap<String,STE> Enclosing = new HashMap<String,STE>();

	public Scope() {}

	public void setEnclosing(String Name, STE ste)
    {
    	this.Enclosing.put(Name, ste);
    }
    
    public STE getEnclosing(String Name)
    {
    	return this.Enclosing.get(Name);
    }
}
