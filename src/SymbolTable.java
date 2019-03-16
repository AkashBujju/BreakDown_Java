import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

class Var {
	String name;
	String type;
	String raw_value;
	String scope;

	Var(String name, String type, String raw_value) {
		this.name = name;
		this.type = type;
		this.raw_value = raw_value;
		this.scope = name + "@" + scope;
	}

	Var(String name, String type, String raw_value, String scope) {
		this.name = name;
		this.type = type;
		this.raw_value = raw_value;
		this.scope = scope;
	}
}

public class SymbolTable {
	List<VarDeclInfo> vars_info = new ArrayList<>();
	HashMap<String, String> scope_type_map = new HashMap<>();
	HashMap<String, String> global_name_scope_map = new HashMap<>();
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

	boolean type_exists(String typename) {
		// Taking away * in the typename( if existing ).

		StringBuffer sb = new StringBuffer(typename);
		int len = typename.length();

		for(int i = len - 1; i >= 0; --i) {
			char c = sb.charAt(i);
			if(Util.is_char_alpha_digit_underscore(c))
				break;
			else
				sb = sb.deleteCharAt(i);
		}

		typename = sb.toString();
		return all_types.containsKey(typename);
	}

	boolean add(String name, String type, String raw_value, String scope) {
		boolean contains = scope_type_map.containsKey(scope);
		if(contains)
			return false;

		Var var_1 = new Var(name, type, raw_value, scope);
		scope_type_map.put(scope, type);
		return true;
	}

	// Use this for global variables
	boolean add(String name, String type, String raw_value) {
		boolean contains = global_name_scope_map.containsKey(name);
		if(contains)
			return false;

		Var var_1 = new Var(name, type, raw_value);
		global_name_scope_map.put(name, var_1.scope);
		scope_type_map.put(var_1.scope, type);
		return true;
	}

	// @Note: get_type tries all scopes until max_scope.
	String get_type(String name, String func_scope_name, int max_scope) {
		int current_scope_index = max_scope;
		while(current_scope_index != -1) {
			String scope_name = func_scope_name + current_scope_index;
			String type = scope_type_map.get(scope_name);
			if(type == null)
				current_scope_index -= 1;
			else
				return type;
		}

		String global_scope = global_name_scope_map.get(name);
		if(global_scope == null)
			return "not_known";

		String type = scope_type_map.get(global_scope);
		return type;
	}
}
