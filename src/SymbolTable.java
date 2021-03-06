import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

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
	HashMap<String, Boolean> primitive_types = new HashMap<>();

	SymbolTable() {
		primitive_types.put("int_8", true); primitive_types.put("uint_8", true);
		primitive_types.put("int_16", true); primitive_types.put("uint_16", true);
		primitive_types.put("int_32", true); primitive_types.put("uint_32", true);
		primitive_types.put("int_64", true); primitive_types.put("uint_64", true);
		primitive_types.put("int", true); primitive_types.put("uint", true);
		primitive_types.put("double", true);
		primitive_types.put("char", true);
		primitive_types.put("bool", true);

		all_types.put("string", true);

		Set<String> key_set = primitive_types.keySet();
		Iterator<String> it = key_set.iterator();
		while(it.hasNext()) {
			all_types.put(it.next(), true);
		}
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
	
	boolean is_primitive_type(String typename) {
		// Taking away the [] and *
		int indexOf_arr = typename.indexOf('[');
		if(indexOf_arr != -1)
			typename = typename.substring(0, indexOf_arr);

		int indexOf_star = typename.indexOf('*');
		if(indexOf_star != -1)
			typename = typename.substring(0, indexOf_star);

		return primitive_types.containsKey(typename);
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
				else
					return "not_known";
			}
			else
				return sv.type;
		}

		return "not_known";
	}

	void show_all() {
		System.out.println();
		System.out.println("SYMBOL TABLE");
		System.out.println("-----------------------------");
		Set<String> keyset = scopeNameAndVarName_var_map.keySet();
		Iterator<String> it = keyset.iterator();
		while(it.hasNext()) {
			String key = it.next();
			SymbolVar symbol_var = scopeNameAndVarName_var_map.get(key);
			System.out.println("name: " + symbol_var.name + ", type: " + symbol_var.type + ", scope_name: " + symbol_var.scope_name);
		}
		System.out.println("-----------------------------");
		System.out.println();
	}
}
