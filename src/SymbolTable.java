import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

class SymbolVar {
	String name;
	String type;

	SymbolVar(String name, String type) {
		this.name = name;
		this.type = type;
	}
}

public class SymbolTable {
	HashMap<String, String> global_name_scope_map = new HashMap<>();
	HashMap<String, SymbolVar> scopeNameAndVarName_var_map = new HashMap<>(); // @Note: First String is the (name of variable + name_of_func_scope + current_scope)
	HashMap<String, Boolean> all_types = new HashMap<>();

	SymbolTable() {
		all_types.put("int_8", true); all_types.put("uint_8", true);
		all_types.put("int_16", true); all_types.put("uint_16", true);
		all_types.put("int_32", true); all_types.put("uint_32", true);
		all_types.put("int_64", true); all_types.put("uint_64", true);
		all_types.put("int", true); all_types.put("uint", true);
		all_types.put("double", true);
		all_types.put("char", true);
		all_types.put("bool", true);
	}

	void add_type(String typename) {
		all_types.put(typename, true);
	}

	boolean name_exists_in_scope(String name, String func_scope_name, int current_scope) {
		if(func_scope_name.equals("global")) {
			String type = global_name_scope_map.get(name);
			if(type == null)
				return false;
			return true;
		}

		String search_name = name + func_scope_name + current_scope;
		SymbolVar sv = scopeNameAndVarName_var_map.get(search_name);
		if(sv == null)
			return false;

		return true;
	}

	boolean add_global(String name, String type) {
		boolean contains = global_name_scope_map.containsKey(name);
		if(contains == true)
			return false;

		global_name_scope_map.put(name, type);

		return true;
	}

	boolean add(String name, String type, String func_scope_name, int current_scope) {
		// @Incomplete: Check if name already exists within current_scope.
		if(name_exists_in_scope(name, func_scope_name, current_scope))
			return false;

		SymbolVar sv = new SymbolVar(name, type);
		String search_name = name + func_scope_name + current_scope;
		scopeNameAndVarName_var_map.put(search_name, sv);

		return true;
	}

	// @Note: get_type tries all scopes until max_scope and checks the global scope too.
	String get_type(String name, String func_scope_name, int max_scope) {
		int current_scope_index = max_scope;
		String type = "not_known";
		while(current_scope_index != -1) {
			String search_name = name + func_scope_name + current_scope_index;
			SymbolVar sv = scopeNameAndVarName_var_map.get(search_name);
			if(sv == null)
				current_scope_index -= 1;
			else
				return sv.type;
		}

		type = global_name_scope_map.get(name);
		if(type != null)
			return type;

		return "not_known";
	}
}
