import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

class SymbolVar {
	String name;
	String type;
	String scope_name;

	SymbolVar(String name, String type, String scope_name) {
		this.name = name;
		this.type = type;
		this.scope_name = scope_name;
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
		all_types.put("string", true);
		all_types.put("bool", true);
	}

	void add_type(String typename) {
		all_types.put(typename, true);
	}

	boolean type_exists(String typename) {
		// Taking away the [] and *
		int indexOf_arr = typename.indexOf('[');
		if(indexOf_arr != -1)
			typename = typename.substring(0, indexOf_arr);

		int indexOf_star = typename.indexOf('*');
		if(indexOf_star != -1)
			typename = typename.substring(0, indexOf_star);

		return all_types.containsKey(typename);
	}

	boolean name_exists_in_scope(String name, String scope_name) {
		if(scope_name.equals("global")) {
			String type = global_name_scope_map.get(name);
			if(type == null)
				return false;
			return true;
		}

		SymbolVar sv = scopeNameAndVarName_var_map.get(scope_name + name);
		if(sv == null)
			return false;

		System.out.println("yes");

		return true;
	}

	boolean add_global(String name, String type) {
		boolean contains = global_name_scope_map.containsKey(name);
		if(contains == true)
			return false;

		global_name_scope_map.put(name, type);

		return true;
	}

	boolean add(String name, String type, String scope_name) {
		if(name_exists_in_scope(name, scope_name))
			return false;

		SymbolVar sv = new SymbolVar(name, type, scope_name);
		scopeNameAndVarName_var_map.put(scope_name + name, sv);

		return true;
	}

	// Checking from the inner most scope and expanding out untill 'global' scope.
	String get_type(String name, String scope_name) {
		String type = "not_known";

		if(scope_name.equals("global")) {
			type = global_name_scope_map.get(name);
			if(type != null)
				return type;
			return "not_known";
		}

		while(!scope_name.equals("")) {
			SymbolVar sv = scopeNameAndVarName_var_map.get(scope_name + name);
			if(sv == null) {
				int index_of_underscore = scope_name.lastIndexOf('_');
				if(index_of_underscore != -1)
					scope_name = scope_name.substring(0, index_of_underscore);
			}
			else
				return sv.type;
		}

		return "not_known";
	}
}
