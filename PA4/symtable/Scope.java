package symtable;
import java.util.*;

public class Scope {
	//maps STE method or variable names to their STEs
	private HashMap<String,STE> enclosed = new HashMap<String,STE>();

	public Scope() {}

	public void setEnclosed(String Name, STE ste)
    {
    	this.enclosed.put(Name, ste);
    }
    
    public STE getEnclosed(String Name)
    {
    	return this.enclosed.get(Name);
    }
}
