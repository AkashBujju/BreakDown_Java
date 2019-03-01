import java.util.List;
import java.util.ArrayList;

class VariableInfo {
	String name;
	String type;
	String raw_value;
	String scope;

	void display() {
		System.out.println("Name:" + name + ", Type:" + type + ", " + "Value:" + raw_value + ", Scope:" + scope);
	}
}

class FunctionInfo {
	String name;
	String return_type;
	List<String> arg_names = new ArrayList<>();
	List<String> arg_types = new ArrayList<>();

	void display() {
		System.out.println("FunctionName: " + name);
		System.out.println("ReturnType: " + return_type);
		for(int i = 0; i < arg_names.size(); ++i) {
			System.out.println("<" + arg_names.get(i) + "> <" + arg_types.get(i) + ">");
		}
		System.out.println();
	}
}

class StructInfo {
	String name;
	List<VariableInfo> vars = new ArrayList<>();

	boolean add(VariableInfo vi) {
		for(VariableInfo v: vars) {
			if(v.name.equals(vi.name))
				return false;
		}

		vars.add(vi);

		return true;
	}

	void display() {
		System.out.println("StructName: " + name);
		System.out.println("Members are: ");
		for(VariableInfo vi: vars)
			vi.display();
	}
}

public class SymbolTable {
	List<VariableInfo> var_infos;	

	SymbolTable() {
		var_infos = new ArrayList<>();
	}

	void add(String name, String type, String raw_value, String scope) {
		VariableInfo var_info = new VariableInfo();
		var_info.name = name;
		var_info.type = type;
		var_info.raw_value = raw_value;
		var_info.scope = scope;

		var_infos.add(var_info);
	}

	// @Incomplete .....
}
