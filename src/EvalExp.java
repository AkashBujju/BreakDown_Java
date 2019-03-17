import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

class MsgType {
	String msg;
	String type;

	MsgType(String msg, String type) {
		this.msg = msg;
		this.type = type;
	}
}

public class EvalExp {
	private List<String> postfix;
	private Stack<String> st = new Stack<>();
	private List<String> t_list = new ArrayList<>();
	private Map<String, String> literal_type_map = new HashMap<>();
	private Map<String, List<String>> t_exp_map = new HashMap<>();

	EvalExp(List<String> postfix) {
		this.postfix = postfix;
		System.out.println("postfix: " + postfix);
	}

	MsgType deduce_final_type(SymbolTable symbol_table, String func_scope_name, int max_scope) {
		String final_type = "";

		if(postfix.size() == 0)
			return new MsgType("none", "void");

		if(postfix.size() == 1) {
			String var = postfix.get(0);
			String type = Util.get_primitive_type(var);

			if(type.equals("not_known")) {
				String var_type = symbol_table.get_type(postfix.get(0), func_scope_name, max_scope);
				if(!var_type.equals("not_known"))
					return new MsgType("none", var_type);
				else { // Variable not found ...
					return new MsgType("Identifier '" + var + "' not found.", "not_known");
				}
			}

			return new MsgType("none", type);
		}

		for(String s: postfix) {
			boolean op = Util.is_operator(s);
			List<String> exp_ops_list = new ArrayList<>();
			if(op) {
				String t = "";

				if(s.equals(">>") || s.equals("<<") || s.equals("!")) { // unary operation on >> , << or !
					String right_char = st.pop();
					String right_type = Util.get_primitive_type(right_char);

					// >> is a unary operator
					if((s.equals(">>") || s.equals("<<")) && st.size() == 1) {
						return new MsgType("Cannot apply '<< and >> and !' as a binary operator.", "not_known");
					}

					// Checking if the variable exists in the symbol table
					else if(right_type.equals("not_known")) {
						String var_type = symbol_table.get_type(right_char, func_scope_name, max_scope);
						if(!var_type.equals(""))
							right_type = var_type;
					}

					// @Note: If >> is used, it should only be for one variable, as pointers
					// cannot point to literals like 1,2,3.14 or "Strings" or multiple variables
					else if(s.equals(">>") && right_char.charAt(0) == '@') {
						return new MsgType("Cannot apply '>> and <<' to literals  or expressions. ie: " + right_char, "not_known");
					}

					// if >> or << is being applied to literals.
					else if((s.equals(">>")) || s.equals("<<")) {
						return new MsgType("Cannot apply '>> and <<' to literals ie. " + right_char, "not_known");
					}

					// Cannot apply >> to expressions
					if(right_char.charAt(0) == '@' && s.equals(">>")) {
						return new MsgType("Operator '>>' can only be applied to lvalues and not: <" + right_char + ">.", "not_known");
					}

					boolean type_already_in_map = literal_type_map.containsKey(right_char);
					if(!type_already_in_map) {
						literal_type_map.put(right_char, right_type);
					}

					t = "@" + (t_list.size() + 1) + "@";
					String res_type = "not_known";

					if(right_char.charAt(0) != '@') {
						boolean is_valid_op = Util.validate_operation(right_type, s);
						res_type = Util.add_types(right_type, s);
						exp_ops_list.add(t);
					}
					else  {
						exp_ops_list.add(s);
						exp_ops_list.add(right_char);
					}

					literal_type_map.put(t, res_type);
				}
				else { // binary operation
					// @Note: +, - can be used both as a unary and binary operator.
					if(st.size() == 0)
						return new MsgType("Incomplete expression found.", "not_known");

					String left_char = st.pop();

					if((s.equals("+") || s.equals("-")) && st.size() == 0) { // unary operation with + or - .
						String left_type = Util.get_primitive_type(left_char);
						if(left_type.equals("not_known")) {
							String var_type = symbol_table.get_type(left_char, func_scope_name, max_scope);
							if(!var_type.equals(""))
								left_type = var_type;
						}

						if(left_type.equals("int") || left_type.equals("double")) {
							literal_type_map.put(left_char, left_type);
							t = "@" + (t_list.size() + 1) + "@";
							literal_type_map.put(t, left_type);
						}

						exp_ops_list.add(s);
						exp_ops_list.add(left_char);
					}
					else {
						String right_char = st.pop();

						String left_type = Util.get_primitive_type(left_char);
						String right_type = Util.get_primitive_type(right_char);

						if(left_type.equals("not_known")) {
							String var_type = symbol_table.get_type(left_char, func_scope_name, max_scope);
							if(!var_type.equals("not_known"))
								left_type = var_type;
							else { // checking in literal_type_map {
								String res = literal_type_map.get(left_char);
								if(res != null)
									left_type = res;
							}
						}
						if(right_type.equals("not_known")) {
							String var_type = symbol_table.get_type(right_char, func_scope_name, max_scope);
							if(!var_type.equals("not_known"))
								right_type = var_type;
							else { // checking in literal_type_map {
								String res = literal_type_map.get(right_char);
								if(res != null)
									right_type = res;
							}
						}

						literal_type_map.put(right_char, right_type);
						literal_type_map.put(left_char, left_type);

						t = "@" + (t_list.size() + 1) + "@";
						literal_type_map.put(t, "not_known");

						exp_ops_list.add(right_char);
						exp_ops_list.add(s);
						exp_ops_list.add(left_char);
					}
				}

				t_exp_map.put(t, exp_ops_list);
				t_list.add(t);
				st.push(t);

			} else {
				st.push(s);
			}
		}

		/*
		// Displaying literal_type_map
		System.out.println("literal_type_map: ");
		Set<String> key_set_3 = literal_type_map.keySet();
		Iterator<String> it_2 = key_set_3.iterator();
		while(it_2.hasNext()) {
			String key = it_2.next();
			System.out.println(key + ": " + literal_type_map.get(key));
		}
		System.out.println();
		*/

		// Deducing types of all t s.
		Set<String> key_set_2 = t_exp_map.keySet();
		int _sz = key_set_2.size();
		for(int i = 0; i < _sz; ++i) {
			String key = "@" + (i+1) + "@";
			List<String> li = t_exp_map.get(key);

			if(li.size() == 1) { // t == @number@
				String res_type = literal_type_map.get(li.get(0));
				literal_type_map.put(key, res_type);	
			}
			else if(li.size() == 2) { // unary operation
				String right_literal = li.get(1);
				String op = li.get(0);
				String right_type = literal_type_map.get(right_literal);

				if(right_type.equals("not_known"))
					return new MsgType("Identifier '" + right_literal + "' not found.", "not_known");

				boolean is_operation_valid = Util.validate_operation(right_type, op);
				if(!is_operation_valid) {
					return new MsgType("Cannot apply " + "'" + li.get(0) + "'" + " to <" + right_literal + "> of type <" + right_type + ">", "not_known");
				}

				String res_type = Util.add_types(right_type, op);
				literal_type_map.replace(key, res_type);
			}
			else { // binary operation
				String right_literal = li.get(0);
				String left_literal = li.get(2);

				String right_type = literal_type_map.get(right_literal);
				String left_type = literal_type_map.get(left_literal);

				if(right_type.equals("not_known")) {
					// Checking if the type has already been deduced.
					if(symbol_table.type_exists(right_literal))
						right_type = right_literal;
					else
						return new MsgType("Identifier '" + right_literal + "' not found", "not_known");
				}
				else if(left_type.equals("not_known")) {
					// Checking if the type has already been deduced.
					if(symbol_table.type_exists(left_literal))
						left_type = left_literal;
					else
						return new MsgType("Identifier '" + left_literal + "' not found", "not_known");
				}

				boolean is_operation_valid = Util.validate_operation(right_type, left_type, li.get(1));
				if(!is_operation_valid) {
					return new MsgType("Cannot apply " + "'" + li.get(1) + "'" + " to <" + right_literal + "> of type <" + right_type + "> and <" + left_literal + "> of type <" + left_type +">.", "not_known");
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

		return new MsgType("none", final_type);
			}
		}
