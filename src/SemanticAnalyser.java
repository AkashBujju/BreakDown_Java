import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

/* @Note: It is not mandatory to check for 'break' and 'continue' statements
 * , since the syntax analyser checks it.
 */

class FuncNameArgs {
	String name;
	List<String> arg_types;
	String return_type;
}

class StructVars {
	List<VarDeclInfo> var_decl_infos;	
	List<String> actual_types;
	int max_vars = 0;
}

class BuiltInFunc {
	String name;
	int num_args = 0;

	BuiltInFunc(String name, int num_args) {
		this.name = name;
		this.num_args = num_args;
	}
}

public class SemanticAnalyser {
	List<Info> infos;	
	SymbolTable symbol_table;
	List<Integer> func_sig_indices;
	List<RangeIndices> quotes_range_indices;
	List<FuncNameArgs> func_name_args;
	List<BuiltInFunc> built_in_funcs;
	HashMap<String, StructVars> name_structvars_map;
	HashMap<Integer, FuncNameArgs> func_id_fna_map;
	ErrorLog error_log;
	String func_iden = "_func@";
	String var_iden = "_var@";

	SemanticAnalyser(List<Info> infos, List<RangeIndices> quotes_range_indices) {
		this.infos = infos;
		this.quotes_range_indices = quotes_range_indices;
		func_sig_indices = new ArrayList<>();
		symbol_table = new SymbolTable();
		func_name_args = new ArrayList<>();
		error_log = new ErrorLog();
		name_structvars_map = new HashMap<>();
		func_id_fna_map = new HashMap<>();
		built_in_funcs = new ArrayList<>();
	}

	void init_built_in_funcs() {
		built_in_funcs.add(new BuiltInFunc("make_objects", 2));
		built_in_funcs.add(new BuiltInFunc("free_objects", 1));
		built_in_funcs.add(new BuiltInFunc("print", 100));
	}

	public void start() throws FileNotFoundException {
		init_built_in_funcs();

		// Filling in func_name_args
		int count = 0;
		for(Info i: infos) {
			if(i.info_type == InfoType.FUNCTION) {
				func_sig_indices.add(count);
				FunctionInfo func_info = (FunctionInfo)(i);
				FuncNameArgs func_name_arg = new FuncNameArgs();
				func_name_arg.name = func_info.name;
				func_name_arg.return_type = func_info.return_type;
				String scope_name = "_" + func_info.id;

				List<String> var_arg_types = new ArrayList<>();
				for(VarDeclInfo var: func_info.var_args) {
					int res = eval_var_decl(var, scope_name);
					if(res == -1)
						return;

					String type = symbol_table.get_type(var.name, scope_name);
					var_arg_types.add(type);
				}

				func_name_arg.arg_types = var_arg_types;
				func_name_args.add(func_name_arg);
				func_id_fna_map.put(func_info.id, func_name_arg);
			}

			count += 1;
		}

		// Actual start
		for(int i = 0; i < infos.size(); ++i) {
			Info info = infos.get(i);	
			int error_res = 0;
			int count_errors = 0;

			if(info.info_type == InfoType.USE)
				error_res = eval_use((UseInfo)(info));
			else if(info.info_type == InfoType.VAR_DECL) // global variable
				error_res = eval_var_decl((VarDeclInfo)(info), "global");
			else if(info.info_type == InfoType.FUNCTION)
				error_res = eval_function((FunctionInfo)(info));
			else if(info.info_type == InfoType.STRUCT)
				error_res = eval_struct((StructInfo)(info), i);
			else {
				error_log.push("Invalid InfoType found.", info.get_info(), info.line_number);
			}

			if(error_res == -1)
				count_errors += 1;
			if(count_errors > 2)
				return;
		}

		// @Tmp
		Set<String> key_set = name_structvars_map.keySet();
		Iterator<String> it = key_set.iterator();
		while(it.hasNext()) {
			String struct_name = it.next();
			StructVars struct_vars = name_structvars_map.get(struct_name);

			System.out.println("StructName: " + struct_name);
			for(VarDeclInfo var_decl_info: struct_vars.var_decl_infos) {
				System.out.println("name: " + var_decl_info.name + ", type: " + var_decl_info.type);
			}
			System.out.println();
		}
	}

	// @NotKnown: From where does the filepath start from ??????
	// @NotKnown: From where does the filepath start from ??????
	// @NotKnown: From where does the filepath start from ??????
	// @NotKnown: From where does the filepath start from ??????
	private int eval_use(UseInfo use_info) throws FileNotFoundException {
		String filename = use_info.filename;
		try {
			Scanner s = new Scanner(new BufferedReader(new FileReader(filename)));
		}
		catch(FileNotFoundException e) {
			error_log.push("Could not find file: " + filename + ".", "use \"" + filename+ "\';", use_info.line_number);
			return -1;
		}

		return 0;
	}

	private int eval_info(Info info, String scope_name, String expected_return_type) {
		InfoType info_type = info.info_type;
		int res = -1;

		if(info_type == InfoType.VAR_DECL)
			res = eval_var_decl((VarDeclInfo)(info), scope_name);
		else if(info_type == InfoType.IF)
			res = eval_if_info((IfInfo)(info), scope_name + '_' + info.id, expected_return_type);
		else if(info_type == InfoType.ELSE_IF)
			res = eval_else_if_info((ElseIfInfo)(info), scope_name + '_' + info.id, expected_return_type);
		else if(info_type == InfoType.ELSE)
			res = eval_else_info((ElseInfo)(info), scope_name + '_' + info.id, expected_return_type);
		else if(info_type == InfoType.WHILE)
			res = eval_while_info((WhileInfo)(info), scope_name + '_' + info.id, expected_return_type);
		else if(info_type == InfoType.VAR_ASSIGN)
			res = eval_var_assign((VarAssignInfo)(info), scope_name);
		else if(info_type == InfoType.RETURN)
			res = eval_return((ReturnInfo)(info), scope_name, expected_return_type);
		else if(info_type == InfoType.EXPRESSION)
			res = eval_exp_info((ExpInfo)(info), scope_name);
		else if(info_type == InfoType.OTHER)
			return 0;

		return res;
	}

	private int eval_struct(StructInfo struct_info, int i) {
		List<VarDeclInfo> var_decl_infos = struct_info.var_decl_infos;
		String scope_name = "_" + struct_info.id;

		StructVars struct_vars = new StructVars();
		List<String> actual_types = new ArrayList<>();
		int max_vars = 0;

		for(VarDeclInfo var_decl_info: var_decl_infos) {
			int res = 0;
			res = eval_var_decl(var_decl_info, scope_name);
			String type = symbol_table.get_type(var_decl_info.name, scope_name);

			if(type.indexOf("@array@") != -1)
				actual_types.add(var_decl_info.type);
			else
				actual_types.add(type);

			// Checking if type is an array
			if(type.indexOf("@array@") != -1) {
				if(var_decl_info.type.equals("not_known")) {
					error_log.push("Array's 'type and size' needed for array '" + var_decl_info.name + "' inside struct '" + struct_info.name + "'", var_decl_info.name + " := " + var_decl_info.raw_value, var_decl_info.line_number);

					return -1;
				}
				else { // Array size has to be present
					int indexOf_open = var_decl_info.type.indexOf('[');
					int indexOf_close = var_decl_info.type.lastIndexOf(']');
					String arr_size_str = var_decl_info.type.substring(indexOf_open + 1, indexOf_close);

					if(arr_size_str.length() == 0) {
						error_log.push("Array bounds/size cannot be deduced inside struct '" + struct_info.name + "' for array '" + var_decl_info.name + "'", var_decl_info.name + " ... " + var_decl_info.raw_value, var_decl_info.line_number);

						return -1;
					}

					int arr_size = Integer.parseInt(arr_size_str);
					max_vars = max_vars + arr_size;
				}
			}
			else
				max_vars += 1;

			var_decl_info.type = type;

			if(res == -1)
				return -1;
		}

		symbol_table.add_type(struct_info.name);
		struct_vars.var_decl_infos = var_decl_infos;
		struct_vars.actual_types = actual_types;
		struct_vars.max_vars = max_vars;
		name_structvars_map.put(struct_info.name, struct_vars);

		return 0;
	}

	private int eval_return(ReturnInfo return_info, String scope_name, String expected_return_type) {
		String exp = return_info.exp;
		String exp_type = get_type_of_exp(exp, scope_name, return_info.line_number);

		if(exp_type.equals("not_known"))
			return -1;

		if(!exp_type.equals(expected_return_type)) {
			error_log.push("Deduced 'return' Type '" + exp_type + "' does not match with the expected 'return' Type '" + expected_return_type + "'.", "return " + exp, return_info.line_number);
			return -1;
		}

		return 0;
	}

	private void add_multiple_return_stat_error(int id, String exp, int line_number, String info_type) {
		error_log.push("Found multiple 'return' statements within the same scope of '" + info_type + "' with scope '" + "_" + id + "'.", "return " + exp, line_number);
	}

	private int eval_exp_info(ExpInfo exp_info, String scope_name) {
		String exp = exp_info.exp;
		String type = get_type_of_exp(exp, scope_name, exp_info.line_number);

		if(type.equals("not_known"))
			return -1;

		return 0;
	}

	private int eval_function(FunctionInfo func_info) {
		List<Info> infos = func_info.infos;
		int current_scope = 0;
		String scope_name = "_" + (func_info.id);
		int num_returns = 0;

		// @Note: Function args already evaluated.
		String return_type = func_id_fna_map.get(func_info.id).return_type;

		int infos_len = infos.size();
		for(int i = 0; i < infos_len; ++i) {
			Info info = infos.get(i);
			if(info.info_type == InfoType.RETURN)
				num_returns += 1;

			if(num_returns > 1) {
				ReturnInfo return_info = (ReturnInfo)(info);
				add_multiple_return_stat_error(func_info.id, return_info.exp, return_info.line_number, "function " + func_info.name);
				return -1;
			}

			int res = eval_info(info, scope_name, return_type);

			if(res == -1)
				return -1;
		}

		if(num_returns == 0) {
			error_log.push("Function '" + func_info.name + "' missing return statement.", "... ??? ... }", func_info.line_number);
			return -1;
		}

		return 0;
	}

	private int eval_else_info(Info info, String scope_name, String return_type) {
		ElseInfo else_info = (ElseInfo)(info);
		int num_returns = 0;

		List<Info> infos = else_info.infos;
		int len = infos.size();
		for(int i = 0; i < len; ++i) {
			Info current_info = infos.get(i);
			if(current_info.info_type == InfoType.RETURN) {
				ReturnInfo return_info = (ReturnInfo)(current_info);
				num_returns += 1;
				if(num_returns > 1) {
					add_multiple_return_stat_error(current_info.id, return_info.exp, return_info.line_number, "else");
					return -1;
				}
			}
			int res = eval_info(current_info, scope_name, return_type);

			if(res == -1)
				return -1;
		}

		return 0;
	}

	private int eval_else_if_info(Info info, String scope_name, String return_type) {
		ElseIfInfo else_if_info = (ElseIfInfo)(info);
		String exp = else_if_info.exp;
		int num_returns = 0;

		// Evaluting the type of exp and checking if its of type 'bool'.
		String exp_type = get_type_of_exp(exp, scope_name, else_if_info.line_number);
		if(!exp_type.equals("bool")) {
			error_log.push("The test condition '" + exp + "' in 'else if' has to be of type 'bool', but found '" + exp_type + "'.", exp, else_if_info.line_number);
			return -1;
		}

		List<Info> infos = else_if_info.infos;
		int len = infos.size();
		for(int i = 0; i < len; ++i) {
			Info current_info = infos.get(i);
			if(current_info.info_type == InfoType.RETURN) {
				ReturnInfo return_info = (ReturnInfo)(current_info);
				num_returns += 1;
				if(num_returns > 1) {
					add_multiple_return_stat_error(current_info.id, return_info.exp, return_info.line_number, "else if");
					return -1;
				}
			}
			int res = eval_info(current_info, scope_name, return_type);

			if(res == -1)
				return -1;
		}

		return 0;
	}

	private int eval_if_info(Info info, String scope_name, String return_type) {
		IfInfo if_info = (IfInfo)(info);
		String exp = if_info.exp;
		int num_returns = 0;

		// Evaluting the type of exp and checking if its of type 'bool'.
		String exp_type = get_type_of_exp(exp, scope_name, if_info.line_number);
		if(!exp_type.equals("bool")) {
			error_log.push("The test condition '" + exp + "' in 'if' has to be of type 'bool', but found '" + exp_type + "'.", exp, if_info.line_number);
			return -1;
		}

		List<Info> infos = if_info.infos;
		int len = infos.size();
		for(int i = 0; i < len; ++i) {
			Info current_info = infos.get(i);
			if(current_info.info_type == InfoType.RETURN) {
				ReturnInfo return_info = (ReturnInfo)(current_info);
				num_returns += 1;
				if(num_returns > 1) {
					add_multiple_return_stat_error(current_info.id, return_info.exp, return_info.line_number, "if");
					return -1;
				}
			}
			int res = eval_info(current_info, scope_name, return_type);

			if(res == -1)
				return -1;
		}

		return 0;
	}

	private int eval_while_info(Info info, String scope_name, String return_type) {
		WhileInfo while_info = (WhileInfo)(info);
		String exp = while_info.exp;
		int num_returns = 0;

		// Evaluting the type of exp and checking if its of type 'bool'.
		String exp_type = get_type_of_exp(exp, scope_name, while_info.line_number);
		if(!exp_type.equals("bool")) {
			error_log.push("The test condition '" + exp + "' in 'while' has to be of type 'bool', but found '" + exp_type + "'.", exp, while_info.line_number);
			return -1;
		}

		List<Info> infos = while_info.infos;
		int len = infos.size();
		for(int i = 0; i < len; ++i) {
			Info current_info = infos.get(i);
			if(current_info.info_type == InfoType.RETURN) {
				ReturnInfo return_info = (ReturnInfo)(current_info);
				num_returns += 1;
				if(num_returns > 1) {
					add_multiple_return_stat_error(current_info.id, return_info.exp, return_info.line_number, "while");
					return -1;
				}
			}
			int res = eval_info(current_info, scope_name, return_type);

			if(res == -1)
				return -1;
		}

		return 0;
	}

	// @Note: Don't use this function for exp's with split size == 1. It has to be size > 1.
	private String get_type_of_exp(String exp, String scope_name, int line_number) {
		List<String> split_value = Util.split_with_ops(exp);
		List<String> final_exp = new ArrayList<>();

		int len = split_value.size();
		int num_func_calls = 0;
		for(int i = 0; i < len; ++i) {
			String s = split_value.get(i);
			if(Util.is_operator(s)) {
				final_exp.add(s);
			}
			else {
				boolean if_func_call = Util.if_func_call(s);
				String type = "not_known";
				if(if_func_call) {
					num_func_calls += 1;
					type = get_type_of_func_call(s, line_number, scope_name);
					// We need to know if 's' is name of a function, so that we can append '_func@' to it.
					type = func_iden + type;
				}
				else {
					String var_type = symbol_table.get_type(s, scope_name);
					type = get_type_of_one_exp(s, line_number, scope_name);
					if(type.equals("not_known")) {
						error_log.push("Unknown Identifier '" + s + "' found.", s, line_number);
						return "not_known";
					}

					// We need to know if 's' is name of a variable, so that we can append '_var@' to it.
					if(!var_type.equals("not_known"))
						type = var_iden + type;
				}	

				final_exp.add(type);
			}
		}

		String final_type = "not_known";
		EvalExp eval_exp = null;

		if(num_func_calls == 1 && final_exp.size() == 1) {
			eval_exp = new EvalExp(final_exp, func_iden, var_iden);
			final_type = eval_exp.deduce_final_type_from_types(symbol_table, scope_name).type;
		}
		else {
			List<String> postfix_exp = InfixToPostFix.infixToPostFix(final_exp);
			eval_exp = new EvalExp(postfix_exp, func_iden, var_iden);
			MsgType msg_type = eval_exp.deduce_final_type_from_types(symbol_table, scope_name);
			if(!msg_type.msg.equals("none")) {
				error_log.push(msg_type.msg, exp, line_number);
				return "not_known";
			}

			final_type = msg_type.type;
		}

		return final_type;
	}

	private int eval_var_assign(VarAssignInfo var_assign_info, String scope_name) {
		String name = var_assign_info.var_name;
		String raw_value = var_assign_info.raw_value;
		int line_number = var_assign_info.line_number;
		String type = "";

		// Checking if name is an array call.
		int indexOf_open = name.indexOf('[');
		int indexOf_close = name.lastIndexOf(']');
		if(indexOf_open != -1 && indexOf_close != -1) {
			String arr_size_str = name.substring(indexOf_open + 1, indexOf_close);
			if(arr_size_str.length() == 0) {
				error_log.push("Missing expression for array index for variable '" + name + "'", name, line_number);

				return -1;
			}

			String arr_size_type = get_type_of_exp(arr_size_str, scope_name, line_number);
			if(!arr_size_type.equals("int")) {
				error_log.push("Array index should be of Type 'int', but found '" + arr_size_type + "'.", name, line_number);

				return -1;
			}

			String broken_name = name.substring(0, indexOf_open);
			type = symbol_table.get_type(broken_name, scope_name);
			if(type.equals("not_known")) {
				error_log.push("Unknown identifier '" + name + "' found.", name, line_number);
				return -1;
			}

			type = type.substring(0, type.indexOf("@array@"));
		}
		else {
			type = symbol_table.get_type(name, scope_name);
			if(type.indexOf("@array@") != -1) {
				error_log.push("Cannot modify lvalue '" + name + "'.", name + " = " + raw_value, line_number);
				return -1;
			}
		}

		if(type.equals("not_known")) {
			error_log.push("First occurance of variable '" + name + "'.", name + " = " + raw_value, line_number);

			return -1;
		}

		String final_type = get_type_of_exp(raw_value, scope_name, line_number);
		if(!type.equals(final_type)) {
			error_log.push("Variable '" + name + "' previously declared as Type '" + type + "', but being assigned to expression of Type '" + final_type + "'.", name + " = " + raw_value, line_number);

			return -1;
		}

		return 0;
	}

	private int eval_udt_decl(VarDeclInfo var_decl_info, String scope_name) {
		String raw_value = var_decl_info.raw_value;	
		String name = var_decl_info.name;
		String type = var_decl_info.type;
		int line_number = var_decl_info.line_number;

		StructVars struct_vars = name_structvars_map.get(type);
		List<String> split_raw_value = Util.split_array(raw_value);

		int split_len = split_raw_value.size();
		int struct_vars_len = struct_vars.var_decl_infos.size();
		if(split_len > struct_vars.max_vars) {
			error_log.push("Number of struct arguments on RHS ie. '" + split_len + "' exceeds the number of Max struct arguments ie. '" + struct_vars.max_vars + "' for struct '" + type + "'.", raw_value, line_number);

			return -1;
		}

		int i = 0;
		int split_value_index = 0;
		while(i < struct_vars_len && split_value_index < split_len) {
			String split_str = split_raw_value.get(split_value_index);
			VarDeclInfo vdi = struct_vars.var_decl_infos.get(i);
			String split_str_type = get_type_of_exp(split_str, scope_name, line_number);

			if(vdi.type.indexOf("@array@") != -1) {
				String actual_type = struct_vars.actual_types.get(i);				
				String arr_size_str = actual_type.substring(actual_type.indexOf('[') + 1, actual_type.lastIndexOf(']'));
				String array_type = vdi.type.substring(0, vdi.type.indexOf("@array@"));

				int _arr_size = Integer.parseInt(arr_size_str);
				int j = 0;
				while(j < _arr_size && split_value_index < split_len) {
					String arr_val_str = split_raw_value.get(split_value_index);
					String arr_type = get_type_of_exp(arr_val_str, scope_name, line_number);
					if(!arr_type.equals(array_type)) {
						error_log.push("Type mismatch at argument number '" + (split_value_index) + "' of expression '" + arr_val_str +  "', needed Type '" + array_type + "' for the array '" + vdi.name + "' but found Type '" + arr_type + "'.", raw_value, line_number);

						return -1;
					}

					j = j + 1;
					split_value_index += 1;
				}
				split_value_index -= 1;
			}

			else if(!vdi.type.equals(split_str_type)) {
				error_log.push("Type mismatch, needed '" + vdi.type + "', but given expression '" + split_str + "' of type '" + split_str_type + "' at argument number '" + split_value_index + "'.", raw_value, line_number);
				return -1;
			}

			if(split_str_type.indexOf("@array@") != -1) {
				String actual_type = split_str_type.substring(0, split_str_type.indexOf("@array@"));
				error_log.push("Cannot convert '" + split_str + "' of type '" + split_str_type + "' to '" + actual_type + "' at argument number '" + i + "'.", raw_value, line_number);
				return -1;
			}

			i = i + 1;
			split_value_index += 1;
		}

		symbol_table.add(name, type, scope_name);

		return 0;
	}

	private int eval_var_decl(VarDeclInfo var_decl_info, String scope_name) {
		String raw_value = var_decl_info.raw_value;
		String name = var_decl_info.name;
		String given_type_name = var_decl_info.type;
		int line_number = var_decl_info.line_number;
		boolean is_array = false;

		if(symbol_table.name_exists_in_scope(name, scope_name)) {
			error_log.push("Variable with name <" + name + "> already exists within current scope <" + scope_name + ">", name, line_number);
			return -1;
		}

		System.out.println("varname: " + name + ", raw_value: <" + raw_value + ">, scope_name: " + scope_name);

		// Checking if it's a user defined type.
		boolean userdefined_type = name_structvars_map.containsKey(var_decl_info.type);
		if(userdefined_type && !raw_value.equals("")) {
			int res = eval_udt_decl(var_decl_info, scope_name);
			if(res == -1)
				return res;

			String in_table_type = symbol_table.get_type(name, scope_name);
			System.out.println("IN_TABLE_TYPE <" + in_table_type + ">");
			System.out.println();

			return 0;
		}

		if(Util.is_typename_array(given_type_name))
			is_array = true;

		if(symbol_table.type_exists(name)) {
			error_log.push("Identifier '" + name + "' is a name of a Type and cannot be used.", name + " ... ", line_number);
			return -1;
		}

		if(raw_value.equals("")) { // It's just a declaration
			if(!symbol_table.type_exists(given_type_name)) {
				error_log.push("Type '" + given_type_name + "' does not exist.", name + ": " + given_type_name, line_number);
				return -1;
			}

			if(is_array) {
				int indexOf_open = given_type_name.indexOf('[');
				int indexOf_close = given_type_name.indexOf(']');
				if(indexOf_close == indexOf_open + 1) {
					error_log.push("Incomplete Type '" + given_type_name + "' found.", given_type_name, line_number);
					return -1;
				}

				given_type_name = Util.get_array_typename(given_type_name);
				// @Note: appending @array@ to type
				given_type_name += "@array@";
			}

			symbol_table.add(name, given_type_name, scope_name);
		}
		else {
			// Checking if raw_value is an array.
			String final_type = "not_known";
			boolean rhs_is_array = false;

			List<String> split_raw_value = Util.split_array(raw_value);
			if(split_raw_value.size() > 1 || is_array) { // rhs is an array
				List<String> split_types = new ArrayList<>();
				for(String s: split_raw_value) {
					split_types.add(get_type_of_exp(s, scope_name, line_number));
				}

				int arr_size = 0;
				if(is_array) {
					// getting the number inside [].
					int indexOf_open = given_type_name.indexOf('[');
					int indexOf_close = given_type_name.indexOf(']');

					if(indexOf_close != indexOf_open + 1)
						arr_size = Integer.parseInt(given_type_name.substring(indexOf_open + 1, indexOf_close));

					if(arr_size != 0 && arr_size < split_raw_value.size()) {
						error_log.push("Array size: '" + arr_size + "' does not match the number of elements in the array '" + name + "'.", name + ": " + given_type_name + " = " + raw_value, line_number);

						return -1;
					}
				}

				// All the types have to be the same
				String to_check_type = split_types.get(0);
				for(int i = 0; i < split_types.size(); ++i) {
					String rw = split_raw_value.get(i);
					String t = split_types.get(i);

					if(!t.equals(to_check_type)) {
						error_log.push("Not all expressions deduce to the same type in the array '" + name + "', " + rw + " -> '" + t + "'.", name + " ... = " + raw_value, line_number);
						return -1;
					}
				}

				rhs_is_array = true;

				final_type = to_check_type;
				// @Note: appending @array@ to type
				final_type += "@array@";
			}
			else {
				final_type = get_type_of_exp(raw_value, scope_name, line_number);
				if(final_type.indexOf("@array@") !=  -1) {
					error_log.push("Language dosen't allow pointers to arrays, ie. to '" + raw_value + "'.", raw_value, line_number);
					return -1;
				}
			}

			if(is_array) {
				if(!symbol_table.type_exists(given_type_name)) {
					error_log.push("Type '" + given_type_name + "' does not exist.", name + ": " + given_type_name, line_number);
					return -1;
				}
				given_type_name = Util.get_array_typename(given_type_name);
				// @Note: appending @array@ to type
				given_type_name += "@array@";
			}

			if(!given_type_name.equals("not_known") && !final_type.equals(given_type_name)) {
				error_log.push("Declared type '" + given_type_name + "' does not match with deduced type '" + final_type + "' in variable '" + name + "'.", name + ": " + given_type_name + " = " + raw_value, line_number);

				return -1;
			}

			if(final_type.equals("void")) {
				error_log.push("Cannot assign expression of Type 'void' to variable '" + name + "'.", var_decl_info.get_info(), line_number);
				return -1;
			}

			if(scope_name.equals("global"))
				symbol_table.add_global(var_decl_info.name, final_type);
			else
				symbol_table.add(var_decl_info.name, final_type, scope_name);
		}

		// Checking of the variable was added correctly.
		String in_table_type = symbol_table.get_type(name, scope_name);
		System.out.println("IN_TABLE_TYPE <" + in_table_type + ">");
		System.out.println();

		return  0;
	}

	// @Note: 's' should contain just one value(literal \ variable).
	String get_type_of_one_exp(String s, int line_number, String scope_name) {
		// Checking if it's a double literal. We don't wont that to get split.
		String tmp_type = Util.get_primitive_type(s);
		if(!tmp_type.equals("not_known"))
			return tmp_type;

		if(s.equals("void")) // @Hack: FIX IT
			return "void";

		List<String> li = Util.split_into_var_names(s);
		int len = li.size();

		if(len == 1) {
			String var = li.get(0);
			String type = get_iden_type(var, scope_name, line_number);
			if(type.indexOf("@array@") != -1 && var.indexOf('[') != -1)
				type = var_iden + type.substring(0, type.indexOf("@array@"));

			return type;
		}

		String current_op = "";
		String prev_type = "";
		String prev_var = "";
		String current_type = "";
		int num_vars = 0;
		boolean encountered_op = false;

		prev_var = li.get(0);
		prev_type = get_iden_type(prev_var, scope_name, line_number);
		if(prev_type.equals("not_known"))
			return "not_known";

		for(int i = 1; i < len; ++i) {
			String str = li.get(i);
			if(str.equals("^") || str.equals(".")) {
				current_op = str;	
				if(i == len || encountered_op) {
					error_log.push("Incomplete Call '" + current_op + "' found.", s, line_number);
					return "not_known";
				}

				encountered_op = true;
			}
			else {
				if(i - 1 < 0) {
					error_log.push("Invalid Identifier '" + current_op + "' found.", s, line_number);
					return "not_known";
				}

				String current_var = li.get(i);
				num_vars += 1;

				// Taking away the * and @array@ to just get the typename.
				String new_prev_type = Util.eat_pointers_and_array(prev_type);
				boolean is_udt = name_structvars_map.containsKey(new_prev_type);
				if(!is_udt) {
					error_log.push("Type '" + new_prev_type + "' does not have any data members.", s, line_number);
					return "not_known";
				}

				StructVars struct_vars = name_structvars_map.get(new_prev_type);
				// Taking away [] from current_var if exists
				boolean array_call = false;
				if(current_var.indexOf('[') != -1) {
					current_var = current_var.substring(0, current_var.indexOf('['));
					array_call = true;
				}

				current_type = get_vartype_from_struct(struct_vars, current_var);
				if(current_type.indexOf("@array@") != -1 && !array_call) {
					error_log.push("Array '" + current_var + "' cannot be referenced as a pointer.", s, line_number);

					return "not_known";
				}

				if(current_type.equals("not_known")) {
					error_log.push("Member '" + current_var + "' does not belong to struct '" + new_prev_type + "'.", s, line_number);
					return "not_known";
				}

				// If the last member is an array type, then take away the @array@.
				if(i == len - 1 && current_type.indexOf("@array@") != -1 && array_call) {
					current_type = current_type.substring(0, current_type.indexOf("@array@"));
				}

				boolean is_pointer_op = current_op.equals("^") ? true : false;
				if(is_pointer_op) {
					if(prev_type.indexOf("*") == -1) {
						error_log.push("Applying pointer operation '^' to non-pointer type '" + prev_type + "'.", s, line_number);
						return "not_known";
					}
				}
				else {
					if(prev_type.indexOf("*") != -1) {
						error_log.push("Applying non-pointer operation '.' to pointer type '" + prev_type + "'.", s, line_number);
						return "not_known";
					}
				}

				prev_type = current_type;
				encountered_op = false;
			}
		}

		return var_iden + current_type;
	}

	String get_type_of_built_in_func_call(String s, int line_number, String scope_name) {
		List<String> all_args = Util.get_func_args(s);
		String args = s.substring(s.indexOf('(') + 1, s.lastIndexOf(')'));
		String func_name = s.substring(0, s.indexOf('('));
		
		if(func_name.equals("make_objects")) {
			if(all_args.size() != 2) {
				push_func_invalid_error(func_name, all_args.size(), line_number);
				return "not_known";
			}

			String obj_type = all_args.get(0);
			if(!symbol_table.type_exists(obj_type)) {
				error_log.push("Type '" + obj_type + "' does not exist @ argument 1", func_name + "(" + obj_type + ", .... ", line_number);
				return "not_known";
			}

			String size_type = get_type_of_exp(all_args.get(1), scope_name, line_number);
			if(!size_type.equals("int")) {
				error_log.push("'size' must be of type 'int', but found '" + size_type + "'.", func_name + "( ...., " + size_type + ")", line_number);
				return "not_known";
			}

			return obj_type + "*";
		}
		else if(func_name.equals("free_objects")) {
			if(all_args.size() != 1) {
				push_func_invalid_error(func_name, all_args.size(), line_number);
				return "not_known";
			}
			String ptr_name = all_args.get(0);
			String type = get_type_of_exp(ptr_name, scope_name, line_number);

			if(type.lastIndexOf("*") == -1) {
				error_log.push("Needed expression of Type 'Type*' but found '" + type + "' @ argument 1.", func_name + "(" + ptr_name + ")", line_number);
				return "not_known";
			}

			return "void";
		}
		else if(func_name.equals("print")) {
			if(all_args.size() == 0) {
				push_func_invalid_error(func_name, all_args.size(), line_number);
				return "not_known";
			}

			String arg_1 = all_args.get(0);
			String arg_1_type = Util.get_primitive_type(arg_1);
			if(!arg_1_type.equals("string")) {
				error_log.push("Function 'print' needs it's 1st argument Type as 'string' literal.", func_name + "(" + arg_1 + ", ....", line_number);
				return "not_known";
			}

			// checking if rest of the arguments are primitive types
			int len = all_args.size();
			for(int i = 1; i < len; ++i) {
				String arg = all_args.get(i);
				String type = get_type_of_exp(arg, scope_name, line_number);

				if(!symbol_table.is_primitive_type(type)) {
					error_log.push("In Function 'print', only primitive 'Types' can be passed as arguments. But found argument '" + arg + "' with Type '" + type + "'.", "print(...., " + arg + ", ...", line_number);
					return "not_known";
				}
			}

			return "int";	
		}
		else if(func_name.equals("scan")) {

		}

		return "not_known";
	}

	String get_type_of_func_call(String s, int line_number, String scope_name) {
		List<String> all_args = Util.get_func_args(s);
		String args = s.substring(s.indexOf('(') + 1, s.lastIndexOf(')'));
		String func_name = s.substring(0, s.indexOf('('));
		boolean func_exists = func_with_num_args_exists(func_name, all_args.size());
		boolean in_built_func = built_in_func_exists(func_name, all_args.size());

		if(!func_exists && !in_built_func) {
			error_log.push("Function with name '" + func_name + "' taking '" + all_args.size() + "' arguments dosen't exist.", s, line_number);
			return "not_known";
		}

		if(in_built_func) {
			return get_type_of_built_in_func_call(s, line_number, scope_name);
		}

		List<String> new_args = new ArrayList<>();
		for(String arg: all_args) {
			Names_NA_Indices names_na_indices = Util.get_only_names_and_literals(arg, get_all_func_names());
			List<String> funcs = Util.get_all_func_calls(arg);
			String new_arg = arg;

			List<String> exps = names_na_indices.names;
			List<RangeIndices> ignore_indices = names_na_indices.range_indices;

			for(String exp: exps) {
				String type = get_type_of_one_exp(exp, line_number, scope_name);
				String in_table_type = symbol_table.get_type(exp, scope_name);

				if(!in_table_type.equals("not_known")) { 
					type = var_iden + type;
				}
				else {} // either the variable name does not exist, or it is a literal.

				Str_NA_Indices str_na_indices = Util.replace_in_str(new_arg, exp, type, ignore_indices);
				new_arg = str_na_indices.str;
				ignore_indices = str_na_indices.range_indices;
			}

			new_args.add(new_arg);
		}

		// @Note: new_arg needs to be enclosed by function name and parenthesis.
		// @Note: new_arg needs to be enclosed by function name and parenthesis.
		StringBuffer tmp_func_call = new StringBuffer(func_name + "(");
		int new_args_len = new_args.size();
		for(int i = 0; i < new_args_len; ++i) {
			String new_arg = new_args.get(i);
			tmp_func_call.append(new_arg);

			if(i != new_args_len - 1)
				tmp_func_call.append(",");
		}
		tmp_func_call.append(")");

		String final_func_type = iter_eval_type_util_end(tmp_func_call.toString(), line_number, scope_name);

		return final_func_type;
	}

	// Returns the return_type of func, with all the function calls evaluated and expressions evaluated.
	// Returns the return_type of func, with all the function calls evaluated and expressions evaluated.
	String iter_eval_type_util_end(String func, int line_number, String scope_name) {
		String final_str =  func;
		String inner_arg = func.substring(func.indexOf('(') + 1, func.lastIndexOf(')'));
		List<String> all_funcs = Util.get_all_func_calls(inner_arg);
		HashMap<String, String> hm = new HashMap<>();

		for(String f: all_funcs)
			hm.put(f, "not_known");

		for(int i = all_funcs.size() - 1; i >= 0; --i) {
			String f = all_funcs.get(i);
			String value = hm.get(f);
			if(value == null) {
				hm.put(f, "not_known");
				value = "not_known";
			}

			String type = "not_known";
			if(value.equals("not_known")) {
				if(!Util.if_func_call(f))
					continue;
				type = get_type_of_one_func_call(f, line_number, scope_name);
				type = func_iden + type;
				hm.put(f, type);

				for(int j = 0; j < all_funcs.size(); ++j) {
					String j_ele = all_funcs.get(j);

					Str_NA_Indices str_na_indices = Util.replace_in_str(j_ele, f, type, new ArrayList<RangeIndices>());
					j_ele = str_na_indices.str;

					all_funcs.set(j, j_ele);

					str_na_indices = Util.replace_in_str(inner_arg, f, type, new ArrayList<RangeIndices>());
					inner_arg = str_na_indices.str;
					if(!Util.contains_func_call(inner_arg)) {
						i = -1;
						break;
					}
				}
			}
		}

		StringBuffer final_func_call = new StringBuffer(func.substring(0, func.indexOf('(')));
		final_func_call.append("(" + inner_arg + ")");

		return get_type_of_one_func_call(final_func_call.toString(), line_number, scope_name);
	}

	// @Note: Here the types of arguments should have already been deduced.
	String get_type_of_one_func_call(String s, int line_number, String scope_name) {
		String in_arg = s.substring(s.indexOf('(') + 1, s.lastIndexOf(')'));
		String name = s.substring(0, s.indexOf('('));
		List<String> arg_types = Util.get_func_args(s);

		// @Note: The arguments may have operations still left to perform on them.
		for(int i = 0; i < arg_types.size(); ++i) {
			String arg = arg_types.get(i);
			List<String> exps = Util.split_with_ops_the_types(arg);
			List<String> postfix = InfixToPostFix.infixToPostFix(exps);
			EvalExp eval_exp = new EvalExp(postfix, func_iden, var_iden);
			MsgType msg_type = eval_exp.deduce_final_type_from_types(symbol_table, scope_name);

			if(!msg_type.msg.equals("none")) {
				error_log.push(msg_type.msg, s, line_number);
				return "not_known";
			}

			arg_types.set(i, msg_type.type);
		}

		FuncNameArgs func_name_arg = get_func_name_with_args(name, arg_types);
		if(func_name_arg == null) {
			String msg = "Function with name '" + name + "' and argument types " + arg_types + " does not exist";
			error_log.push(msg, s, line_number);
			return "not_known";
		}

		return func_name_arg.return_type;
	}

	boolean built_in_func_exists(String func_name, int num_args) {
		for(BuiltInFunc bf: built_in_funcs) {
			if(func_name.equals(bf.name))
				return true;
		}

		return false;
	}

	FuncNameArgs get_func_name_with_args(String name, List<String> types) {
		for(FuncNameArgs func_name_arg: func_name_args) {
			if(name.equals(func_name_arg.name)) {
				List<String> arg_types = func_name_arg.arg_types;

				if(types.size() == arg_types.size()) {
					boolean all_match = true;
					for(int i = 0; i < types.size(); ++i) {
						String t1 = types.get(i);
						String t2 = arg_types.get(i);
						if(!t1.equals(t2)) {
							all_match = false;
							break;
						}
					}

					if(all_match)
						return func_name_arg;
				}
			}
		}

		return null;
	}

	boolean func_with_num_args_exists(String name, int num_args) {
		for(Integer i: func_sig_indices) {
			FunctionInfo func_info = (FunctionInfo)(infos.get(i));	
			if(func_info.name.equals(name) && num_args == func_info.var_args.size())
				return true;
		}

		return false;
	}

	List<String> get_all_func_names() {
		List<String> li = new ArrayList<>();
		for(FuncNameArgs func_name_arg: func_name_args) {
			String name = func_name_arg.name;
			if(!li.contains(name))
				li.add(name);
		}

		return li;
	}

	String get_vartype_from_struct(StructVars struct_vars, String varname) {
		int len = struct_vars.var_decl_infos.size();
		for(int i = 0; i < len; ++i) {
			VarDeclInfo var_decl_info = struct_vars.var_decl_infos.get(i);
			if(varname.equals(var_decl_info.name)) {
				String type = var_decl_info.type;
				return type;
			}
		}

		return "not_known";
	}

	String get_iden_type(String s, String scopename, int line_number) {
		String type = "";

		boolean is_array = s.indexOf('[') != -1 && s.indexOf(']') != -1 ? true : false;
		if(is_array) {
			type = get_array_call_type(s, scopename, line_number);
			return type;
		}

		type = Util.get_primitive_type(s);
		if(!type.equals("not_known"))
			return type;

		type = symbol_table.get_type(s, scopename);
		if(!type.equals("not_known"))
			return type;

		error_log.push("Unknown Identifier '" + s + "' found.", s, line_number);
		return "not_known";
	}

	String get_array_call_type(String s, String scope_name, int line_number) {
		int indexOf_open = s.indexOf('[');
		int indexOf_close = s.lastIndexOf(']');
		String arr_size_str = "";

		arr_size_str = s.substring(indexOf_open + 1, indexOf_close);
		if(arr_size_str.length() == 0) {
			error_log.push("Missing array index [?] .", s, line_number);
			return "not_known";
		}

		String arr_size_type = get_type_of_exp(arr_size_str, scope_name, line_number);
		if(!arr_size_type.equals("int")) {
			error_log.push("Array index should be of Type 'int', but found '" + arr_size_type + "'.", " ... " + s + " ... ", line_number);

			return "not_known";
		}

		String var_name = s.substring(0, indexOf_open);
		String type = symbol_table.get_type(var_name, scope_name);
		if(type.equals("not_known")) {
			error_log.push("Unknown identifier '" + var_name + "' found.", var_name, line_number);
			return "not_known";
		}

		return type;
	}

	void push_func_invalid_error(String func_name, int args_size, int line_number) {
		error_log.push("Function with name '" + func_name + "' and arguments size of '" + args_size + "' does not exist.", func_name, line_number);
	}
}
