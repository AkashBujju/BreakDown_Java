import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

public class EvalExp {
	private List<String> postfix;
	private Stack<String> st = new Stack<>();
	private List<String> t_list = new ArrayList<>();
	private Map<String, String> literal_type_map = new HashMap<>();
	private Map<String, List<String>> t_exp_map = new HashMap<>();

	EvalExp(List<String> postfix) {
		this.postfix = postfix;
	}

	String deduce_final_type(SymbolTable symbol_table, String func_scope_name, int max_scope) {
		String final_type = "";

		if(postfix.size() == 0)
			return "void";

		if(postfix.size() == 1) {
			String var = postfix.get(0);
			String type = Util.get_primitive_type(var);

			if(type.equals("not_known")) {
				// if type is not present, then check if the variable name exists in
				// the symbol table.
				String var_type = symbol_table.get_type(postfix.get(0), func_scope_name, max_scope);
				// @Incomplete: add error statements
				if(!var_type.equals(""))
					return var_type;
			}

			return type;
		}

		for(String s: postfix) {
			boolean op = Util.is_operator(s);
			if(op) {
				String t, f;
				List<String> exp_ops_list = new ArrayList<>();

				if(s.equals(">>") || s.equals("<<") || s.equals("!")) {
					String right_char = st.pop();
					String right_type = Util.get_primitive_type(right_char);

					// >> is a unary operator
					if((s.equals(">>") || s.equals("<<")) && st.size() == 1) {
						System.out.println("Cannot apply " + s + " as a binary operator.");
						right_type = "not_known";
					}

					// Checking if the variable exists in the symbol table
					else if(right_type.equals("not_known")) {
						String var_type = symbol_table.get_type(right_char, func_scope_name, max_scope);
						if(!var_type.equals(""))
							right_type = var_type;
					}

					// @Note: If >> is used, it should only be for one variable, as pointers
					// cannot point to literals like 1,2,3.14 or "Strings" ot multiple variables
					else if(s.equals(">>") && right_char.charAt(0) == '@') {
						System.out.println("Cannot apply >> to literal types or expressions. ie: " + right_char);
						right_type = "not_known";
					}

					// if >> or << is being applied to literals.
					else if((s.equals(">>")) || s.equals("<<")) {
						System.out.println("Cannot apply " + s + " to literals ie. " + right_char);
						right_type = "not_known";
					}

					f = s + right_char;
					literal_type_map.put(right_char, right_type);
					t = "@" + (t_list.size() + 1) + "@";
					literal_type_map.put(t, "not_known");

					exp_ops_list.add(s);
					exp_ops_list.add(right_char);
				}
				else {
					String left_char = st.pop();
					String right_char = st.pop();
					f = right_char + s + left_char;

					String left_type = Util.get_primitive_type(left_char);
					String right_type = Util.get_primitive_type(right_char);

					// Checking if it's a variable name in the symbol_table
					// @Incomplete: Show error if variable_name is not found.
					if(left_type.equals("not_known")) {
						String var_type = symbol_table.get_type(left_char, func_scope_name, max_scope);
						if(!var_type.equals(""))
							left_type = var_type;
					}
					if(right_type.equals("not_known")) {
						String var_type = symbol_table.get_type(right_char, func_scope_name, max_scope);
						if(!var_type.equals(""))
							right_type = var_type;
					}


					literal_type_map.put(right_char, right_type);
					literal_type_map.put(left_char, left_type);

					// @Note: The String t can be something unique, because t0, t1 ... can even be a variable name.
					// @Note: The String t can be something unique, because t0, t1 ... can even be a variable name.

					t = "@" + (t_list.size() + 1) + "@";
					literal_type_map.put(t, "not_known");

					exp_ops_list.add(right_char);
					exp_ops_list.add(s);
					exp_ops_list.add(left_char);
				}

				t_exp_map.put(t, exp_ops_list);
				t_list.add(t);
				st.push(t);

			} else {
				st.push(s);
			}
		}

		// Deducing types of all t s.
		Set<String> key_set_2 = t_exp_map.keySet();
		int _sz = key_set_2.size();
		for(int i = 0; i < _sz; ++i) {
			String key = "@" + (i+1) + "@";
			List<String> li = t_exp_map.get(key);

			if(li.size() == 2) { // unary operation
				String right_literal = li.get(1);
				String op = li.get(0);
				String right_type = literal_type_map.get(right_literal);
				boolean is_operation_valid = Util.validate_operation(right_type, op);
				if(!is_operation_valid) {
					System.out.println("Cannot apply " + "'" + li.get(0) + "'" + " to " + right_literal + "<"
							+ right_type + ">");
					return "not_known";
				}

				String res_type = Util.add_types(right_type, op);
				literal_type_map.replace(key, res_type);
			}
			else { // binary operation
				String right_literal = li.get(0);
				String left_literal = li.get(2);

				String right_type = literal_type_map.get(right_literal);
				String left_type = literal_type_map.get(left_literal);

				boolean is_operation_valid = Util.validate_operation(right_type, left_type, li.get(1));
				if(!is_operation_valid) {
					System.out.println("Cannot apply " + "'" + li.get(1) + "'" + " to " + right_literal + "<"
							+ right_type + "> and " + left_literal + "<" + left_type +">.");
					return "not_known";
				}

				String res_type = Util.add_types(right_type, left_type, li.get(1));
				literal_type_map.replace(key, res_type);
			}

		}

		final_type = literal_type_map.get("@" + _sz + "@");

		/*
		System.out.println("Literal_Types: ");
		Set<String> key_set_3 = literal_type_map.keySet();
		Iterator<String> it_2 = key_set_3.iterator();
		while(it_2.hasNext()) {
			String key = it_2.next();
			System.out.println(key + ": " + literal_type_map.get(key));
		}
		System.out.println();
		*/

		return final_type;
	}

}
