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
	String func_iden, var_iden;

	EvalExp(List<String> postfix, String func_iden, String var_iden) {
		this.postfix = postfix;
		this.func_iden = func_iden;
		this.var_iden = var_iden;

		System.out.println("postfix: " + postfix);
	}

	MsgType deduce_final_type(SymbolTable symbol_table, String scope_name) {
		String final_type = "";

		if(postfix.size() == 0)
			return new MsgType("none", "void");

		if(postfix.size() == 1) {
			String var_name = postfix.get(0);
			String type = Util.get_primitive_type(var_name);

			if(type.equals("not_known")) {
				String var_type = symbol_table.get_type(var_name, scope_name);
				if(!var_type.equals("not_known"))
					return new MsgType("none", var_type);
				else { // Variable not found ...
					return new MsgType("Identifier '" + var_name + "' not found.", "not_known");
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
					if(st.isEmpty()) {
						return new MsgType("Invalid expression: " + postfix + " found.", "not_known");
					}
					String right_char = st.pop();

					String right_type = Util.get_primitive_type(right_char);

					// Checking if the variable exists in the symbol table
					if(right_type.equals("not_known")) {
						String var_type = symbol_table.get_type(right_char, scope_name);
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

					if(st.isEmpty()) {
						return new MsgType("Invalid expression: " + postfix + " found.", "not_known");
					}
					String left_char = st.pop();

					if((s.equals("+") || s.equals("-")) && st.size() == 0) { // unary operation with + or - .
						String left_type = Util.get_primitive_type(left_char);
						if(left_type.equals("not_known")) {
							String var_type = symbol_table.get_type(left_char, scope_name);
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
						if(st.isEmpty()) {
							return new MsgType("Invalid expression: " + postfix + " found.", "not_known");
						}
						String right_char = st.pop();

						String left_type = Util.get_primitive_type(left_char);
						String right_type = Util.get_primitive_type(right_char);

						if(left_type.equals("not_known")) {
							String var_type = symbol_table.get_type(left_char, scope_name);
							if(!var_type.equals("not_known"))
								left_type = var_type;
							else { // checking in literal_type_map
								String res = literal_type_map.get(left_char);
								if(res != null)
									left_type = res;
							}
						}
						if(right_type.equals("not_known")) {
							String var_type = symbol_table.get_type(right_char, scope_name);
							if(!var_type.equals("not_known"))
								right_type = var_type;
							else { // checking in literal_type_map
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

		// Checking if any element in the postfix has been left out. If Yes, then it's an invalid expression.
		for(String s: postfix) {
			int contains = st.search(s);
			if(contains != -1)
				if(!st.isEmpty()) {
					return new MsgType("Invalid expression " + postfix + " found", "not_known");
				}
		}

		// Displaying literal_type_map
		//	System.out.println("literal_type_map: ");
		//	Set<String> key_set_3 = literal_type_map.keySet();
		//	Iterator<String> it_2 = key_set_3.iterator();
		//	while(it_2.hasNext()) {
		//		String key = it_2.next();
		//		System.out.println(key + ": " + literal_type_map.get(key));
		//	}
		//	System.out.println();

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
				literal_type_map.put(key, res_type);
			}
			else { // binary operation
				String right_literal = li.get(0);
				String left_literal = li.get(2);

				String right_type = literal_type_map.get(right_literal);
				String left_type = literal_type_map.get(left_literal);

				if(right_literal.equals("not_known"))
					return new MsgType("Identifier '" + right_literal + "' not found", "not_known");
				else if(left_literal.equals("not_known"))
					return new MsgType("Identifier '" + left_literal + "' not found", "not_known");

				boolean is_operation_valid = Util.validate_operation(right_type, left_type, li.get(1));
				if(!is_operation_valid) {
					return new MsgType("Cannot apply " + "'" + li.get(1) + "'" + " to <" + right_literal + "> of type <" + right_type + "> and <" + left_literal + "> of type <" + left_type +">.", "not_known");
				}

				String res_type = Util.add_types(right_type, left_type, li.get(1));
				literal_type_map.put(key, res_type);
			}
		}

		final_type = literal_type_map.get("@" + _sz + "@");

		//	System.out.println("Literal_Types: ");
		//	Set<String> key_set_3 = literal_type_map.keySet();
		//	Iterator<String> it_2 = key_set_3.iterator();
		//	while(it_2.hasNext()) {
		//		String key = it_2.next();
		//		System.out.println(key + ": " + literal_type_map.get(key));
		//	}
		//	System.out.println();
		//

		if(final_type == null)
			final_type = "not_known";

		return new MsgType("none", final_type);
	}

	MsgType deduce_final_type_from_types(SymbolTable symbol_table, String scope_name) {
		String final_type = "not_known";
		if(postfix.size() == 1) {
			String type = postfix.get(0);

			boolean is_var = type.indexOf(var_iden) == -1 ? false : true;
			boolean is_func = type.indexOf(func_iden) == -1 ? false : true;
			if(is_var)
				type = type.substring(5); // length of _var@ is 4.
			else if(is_func)
				type = type.substring(6); // length of _var@ is 5.
			return new MsgType("none", type);
		}

		for(String s: postfix) {
			boolean op = Util.is_operator(s);
			List<String> exp_ops_list = new ArrayList<>();

			if(op) {
				String t = "";

				if(s.equals(">>") || s.equals("<<") || s.equals("!")) { // unary operation on >> , << or !
					if(st.isEmpty()) {
						return new MsgType("Invalid expression: " + postfix + " found.", "not_known");
					}
					String right_type = st.pop();

					boolean is_var = right_type.indexOf(var_iden) == -1 ? false : true;
					boolean is_func = right_type.indexOf(func_iden) == -1 ? false : true;

					if(is_var)
						right_type = right_type.substring(5); // length of _var@ is 4.
					else if(is_func)
						right_type = right_type.substring(6); // length of _var@ is 5.

					if(right_type.equals("not_known"))
						return new MsgType("Type 'not_known' found.", "not_known");

					else if(s.equals(">>") && (right_type.charAt(0) == '@' || !is_var || is_func))
						return new MsgType("Cannot apply '>>' to literals  or expressions or functions. ie: type <" + right_type + ">.", "not_known");

					t = "@" + (t_list.size() + 1) + "@";
					literal_type_map.put(t, "not_known");

					exp_ops_list.add(s);
					exp_ops_list.add(right_type);
				}
				else { // binary operation
					// @Note: +, - can be used both as a unary and binary operator.
					if(st.size() == 0)
						return new MsgType("Incomplete expression found.", "not_known");

					if(st.isEmpty()) {
						return new MsgType("Invalid expression: " + postfix + " found.", "not_known");
					}
					String left_type = st.pop();

					boolean is_left_var = left_type.indexOf(var_iden) == -1 ? false : true;
					boolean is_left_func = left_type.indexOf(func_iden) == -1 ? false : true;
					if(is_left_var)
						left_type = left_type.substring(5); // length of _var@ is 4.
					else if(is_left_func)
						left_type = left_type.substring(6); // length of _func@ is 5.

					if((s.equals("+") || s.equals("-")) && st.size() == 0) { // unary operation with + or - .
						if(left_type.equals("not_known"))
							return new MsgType("Type 'not_known' found.", "not_known");

						boolean valid_operation = Util.validate_operation(left_type, s);
						if(!valid_operation)
							return new MsgType("Unary operations involving '+' or '-', can be done only on 'int' or 'double'.", "not_known");

						t = "@" + (t_list.size() + 1) + "@";
						literal_type_map.put(t, "not_known");

						exp_ops_list.add(s);
						exp_ops_list.add(left_type);
					}
					else { // binary operation
						if(st.isEmpty()) {
							return new MsgType("Invalid expression: " + postfix + " found.", "not_known");
						}
						String right_type = st.pop();

						boolean is_right_var = right_type.indexOf(var_iden) == -1 ? false : true;
						boolean is_right_func = right_type.indexOf(func_iden) == -1 ? false : true;

						if(is_right_var)
							right_type = right_type.substring(5); // length of _var@ is 4.
						else if(is_right_func)
							right_type = right_type.substring(6); // length of _func@ is 4.

						if(left_type.equals("not_known"))
							return new MsgType("Type 'not_known' found.", "not_known");

						t = "@" + (t_list.size() + 1) + "@";
						literal_type_map.put(t, "not_known");

						exp_ops_list.add(right_type);
						exp_ops_list.add(s);
						exp_ops_list.add(left_type);
					}
				}

				t_exp_map.put(t, exp_ops_list);
				t_list.add(t);
				st.push(t);
			}
			else {
				st.push(s);
			}
		}

		for(String s: postfix) {
			int contains = st.search(s);
			if(contains != -1)
				if(!st.isEmpty()) {
					return new MsgType("Invalid expression " + postfix + " found", "not_known");
				}
		}

		// Deducing final types of t's
		Set<String> key_set_1 = t_exp_map.keySet();
		int _sz = key_set_1.size();
		for(int i = 0; i < _sz; ++i) {
			String key = "@" + (i+1) + "@";
			List<String> li = t_exp_map.get(key);

			if(li.size() == 2) { // unary operation
				String operator = li.get(0);
				String right_type = li.get(1);

				if(right_type.charAt(0) == '@')  { // get the type from literal_type_map.
					String res = literal_type_map.get(right_type);
					if(res == null)
						return new MsgType("Type of '" + right_type + "' not found.", "not_known");
					right_type = res;
				}

				boolean is_valid_operation = Util.validate_operation(right_type, operator);
				if(!is_valid_operation) {
					return new MsgType("Operator '" + operator + "' cannot be applied on type '" + right_type + "'.", "not_known");
				}

				String res_type = Util.add_types(right_type, operator);
				literal_type_map.put(key, res_type);

				// System.out.println("new_type: " + res_type + ", key: " + key);
				//	System.out.println("is_valid_operation: " + is_valid_operation);
				//	System.out.println("operator: " + operator + ", right_type: " + right_type);
			}
			else { // binary operation
				String left_type = li.get(0);
				String operator = li.get(1);
				String right_type = li.get(2);

				if(left_type.charAt(0) == '@') {
					String res = literal_type_map.get(left_type);
					if(res == null)
						return new MsgType("Type of '" + left_type + "' not found.", "not_known");
					left_type = res;
				}
				if(right_type.charAt(0) == '@') {
					String res = literal_type_map.get(right_type);
					if(res == null)
						return new MsgType("Type of '" + right_type + "' not found.", "not_known");
					right_type = res;
				}

				boolean valid_operation = Util.validate_operation(left_type, right_type, operator);
				if(!valid_operation)
					return new MsgType("Operator '" + operator + "' cannot be applied on Type '" + left_type + "' and Type '" + right_type + "'.", "not_known");

				String res_type = Util.add_types(left_type, right_type, operator);
				literal_type_map.put(key, res_type);
			}
		}

		final_type = literal_type_map.get("@" + _sz + "@");
		if(final_type == null)
			final_type = "not_known";

		return new MsgType("none", final_type);
	}
}
