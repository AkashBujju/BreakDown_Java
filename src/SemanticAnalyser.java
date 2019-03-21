import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

class FuncNameArgs {
	String name;
	List<String> arg_types;
	String return_type;
}

public class SemanticAnalyser {
	List<Info> infos;	
	SymbolTable symbol_table;
	List<Integer> func_sig_indices;
	List<RangeIndices> quotes_range_indices;
	List<FuncNameArgs> func_name_args;
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

		int count = 0;
		for(Info i: infos) {
			if(i.info_type == InfoType.FUNCTION) {
				func_sig_indices.add(count);
				FunctionInfo func_info = (FunctionInfo)(i);
				FuncNameArgs func_name_arg = new FuncNameArgs();
				func_name_arg.name = func_info.name;
				func_name_arg.return_type = func_info.return_type;

				List<String> var_arg_types = new ArrayList<>();
				for(VarDeclInfo var: func_info.var_args)
					var_arg_types.add(var.type);

				func_name_arg.arg_types = var_arg_types;
				func_name_args.add(func_name_arg);

				/*
				System.out.println("name: " + func_name_arg.name);
				System.out.println("return_type: " + func_name_arg.return_type);
				System.out.println("arg_types: " + func_name_arg.arg_types);
				System.out.println();
				*/
			}

			count += 1;
		}
	}

	public void start() throws FileNotFoundException {
		for(int i = 0; i < infos.size(); ++i) {
			Info info = infos.get(i);	
			int error_res = 0;
			int count_errors = 0;

			if(info.info_type == InfoType.USE)
				error_res = eval_use((UseInfo)(info));
			else if(info.info_type == InfoType.VAR_DECL) // global variable
				error_res = eval_var_decl((VarDeclInfo)(info), "global", 0);
			else if(info.info_type == InfoType.FUNCTION)
				error_res = eval_function((FunctionInfo)(info));

			if(error_res == -1)
				count_errors += 1;
			if(count_errors > 2)
				return;
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

	private int eval_info(Info info, String func_scope_name, int current_scope) {
		InfoType info_type = info.info_type;
		int res = -1;
		if(info_type == InfoType.VAR_DECL)
			res = eval_var_decl((VarDeclInfo)(info), func_scope_name, current_scope);
		else if(info_type == InfoType.IF)
			res = eval_if_info((IfInfo)(info), func_scope_name, current_scope);
		else if(info_type == InfoType.WHILE)
			res = eval_while_info((WhileInfo)(info), func_scope_name, current_scope);

		return res;
	}

	private int eval_if_info(Info info, String func_scope_name, int current_scope) {
		IfInfo if_info = (IfInfo)(info);
		String exp = if_info.exp;

		// Evaluting the type of exp and checking if its of type 'bool'.
		String exp_type = get_type_of_exp(exp, func_scope_name, current_scope, if_info.exp_line_number);
		if(!exp_type.equals("bool")) {
			error_log.push("The test condition '" + exp + "' in 'if' has to be of type 'bool', but found '" + exp_type + "'.", exp, if_info.exp_line_number);
			return -1;
		}

		List<Info> infos = if_info.infos;
		int len = infos.size();
		for(int i = 0; i < len; ++i) {
			Info current_info = infos.get(i);
			int res = eval_info(current_info, func_scope_name, current_scope + 1);

			if(res == -1)
				return -1;
		}

		return 0;
	}

	private int eval_while_info(Info info, String func_scope_name, int current_scope) {
		WhileInfo while_info = (WhileInfo)(info);
		String exp = while_info.exp;

		// Evaluting the type of exp and checking if its of type 'bool'.
		String exp_type = get_type_of_exp(exp, func_scope_name, current_scope, while_info.exp_line_number);
		if(!exp_type.equals("bool")) {
			error_log.push("The test condition '" + exp + "' in 'while' has to be of type 'bool', but found '" + exp_type + "'.", exp, while_info.exp_line_number);
			return -1;
		}

		List<Info> infos = while_info.infos;
		int len = infos.size();
		for(int i = 0; i < len; ++i) {
			Info current_info = infos.get(i);
			int res = eval_info(current_info, func_scope_name, current_scope + 1);

			if(res == -1)
				return -1;
		}

		return 0;
	}

	// @Note: Don't use this function for exp's with split size == 1. It has to be big.
	private String get_type_of_exp(String exp, String func_scope_name, int current_scope, int line_number) {
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
					type = get_type_of_func_call(s, line_number, func_scope_name, current_scope);
					// We need to know if 's' is name of a function, so that we can append '_func@' to it.
					type = func_iden + type;
				}
				else {
					String var_type = symbol_table.get_type(s, func_scope_name, current_scope);
					type = get_type_of_exp(s, line_number, false, func_scope_name, current_scope);

					// We need to know if 's' is name of a variable, so that we can append '_var@' to it.
					if(!var_type.equals("not_known")) {
						type = var_iden + type;
					}
				}	

				final_exp.add(type);
			}
		}

		String final_type = "not_known";
		EvalExp eval_exp = null;

		if(num_func_calls == 1 && final_exp.size() == 1) {
			eval_exp = new EvalExp(final_exp, func_iden, var_iden);
			final_type = eval_exp.deduce_final_type_from_types(symbol_table, func_scope_name, current_scope).type;
		}
		else {
			List<String> postfix_exp = InfixToPostFix.infixToPostFix(final_exp);
			eval_exp = new EvalExp(postfix_exp, func_iden, var_iden);
			MsgType msg_type = eval_exp.deduce_final_type_from_types(symbol_table, func_scope_name, current_scope);
			if(!msg_type.msg.equals("none")) {
				error_log.push(msg_type.msg, exp, line_number);
				return "not_known";
			}

			final_type = msg_type.type;
		}

		return final_type;
	}

	private int eval_function(FunctionInfo func_info) {
		List<Info> infos = func_info.infos;
		String func_scope_name = func_info.scope_name;
		int current_scope = 0;

		// @Note: The 'current_scope' must be accordingly increased and decreased when entering / leaving a scope.
		// @Note: The 'current_scope' must be accordingly increased and decreased when entering / leaving a scope.
		int infos_len = infos.size();
		for(int i = 0; i < infos_len; ++i) {
			Info info = infos.get(i);
			int res = eval_info(info, func_scope_name, current_scope);

			if(res == -1)
				return -1;
		}

		return 0;
	}

	private int eval_var_decl(VarDeclInfo var_decl_info, String func_scope_name, int current_scope) {
		String raw_value = var_decl_info.raw_value;
		String name = var_decl_info.name;

		// Checking if the variable name is not the name of any type.
		if(symbol_table.name_exists_in_scope(name, func_scope_name, current_scope)) {
			String type = symbol_table.get_type(name, func_scope_name, current_scope);
			error_log.push("Variable with name <" + name + "> already exists within current scope <" + (func_scope_name + current_scope) + ">", name, var_decl_info.line_number);
			return -1;
		}

		System.out.println("varname: " + name + ", raw_value: " + raw_value + ", scope_name: " + (func_scope_name + current_scope));
		String final_type = get_type_of_exp(raw_value, func_scope_name, current_scope, var_decl_info.line_number);

		if(func_scope_name.equals("global"))
			symbol_table.add_global(var_decl_info.name, final_type);
		else
			symbol_table.add(var_decl_info.name, final_type, func_scope_name, current_scope);

		// Checking of the variable was added correctly.
		String in_table_type = symbol_table.get_type(name, func_scope_name, current_scope);
		System.out.println("IN_TABLE_TYPE <" + in_table_type + ">");
		System.out.println();

		return  0;
	}

	String get_type_of_exp(String s, int line_number, boolean contains_only_types, String func_scope_name, int current_scope) {
		List<String> in_list = Util.split_with_ops(s);
		List<String> out_list = InfixToPostFix.infixToPostFix(in_list);

		EvalExp eval_exp = new EvalExp(out_list, func_iden, var_iden);
		MsgType msg_type = null;
		if(contains_only_types)
			msg_type = eval_exp.deduce_final_type_from_types(symbol_table, func_scope_name, current_scope);
		else
			msg_type = eval_exp.deduce_final_type(symbol_table, func_scope_name, current_scope);

		if(msg_type.msg.equals("none"))
			return msg_type.type;
		else
			error_log.push(msg_type.msg, s, line_number);

		return msg_type.type;
	}

	String get_type_of_func_call(String s, int line_number, String func_scope_name, int current_scope) {
		List<String> all_args = Util.get_func_args(s);
		String args = s.substring(s.indexOf('(') + 1, s.lastIndexOf(')'));
		String func_name = s.substring(0, s.indexOf('('));
		boolean func_exists = func_with_num_args_exists(func_name, all_args.size());
		if(!func_exists) {
			error_log.push("Function with name '" + func_name + "' taking '" + all_args.size() + "' arguments dosen't exist.", s, line_number);
			return "not_known";
		}

		List<String> new_args = new ArrayList<>();
		for(String arg: all_args) {
			Names_NA_Indices names_na_indices = Util.get_only_names_and_literals(arg, get_all_func_names());
			List<String> funcs = Util.get_all_func_calls(arg);
			String new_arg = arg;

			List<String> exps = names_na_indices.names;
			List<RangeIndices> ignore_indices = names_na_indices.range_indices;

			for(String exp: exps) {
				String type = get_type_of_exp(exp, line_number, false, func_scope_name, current_scope);
				String in_table_type = symbol_table.get_type(exp, func_scope_name, current_scope);

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

		String final_func_type = iter_eval_type_util_end(tmp_func_call.toString(), line_number, func_scope_name, current_scope);

		return final_func_type;
	}

	// Returns the return_type of func, with all the function calls evaluated and expressions evaluated.
	// Returns the return_type of func, with all the function calls evaluated and expressions evaluated.
	String iter_eval_type_util_end(String func, int line_number, String func_scope_name, int current_scope) {
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
				type = get_type_of_one_func_call(f, line_number, func_scope_name, current_scope);
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

		return get_type_of_one_func_call(final_func_call.toString(), line_number, func_scope_name, current_scope);
	}

	// @Note: Here the types of arguments should have already been deduced.
	String get_type_of_one_func_call(String s, int line_number, String func_scope_name, int current_scope) {
		String in_arg = s.substring(s.indexOf('(') + 1, s.lastIndexOf(')'));
		String name = s.substring(0, s.indexOf('('));
		List<String> arg_types = Util.get_func_args(s);

		// @Note: The arguments may have operations still left to perform on them.
		for(int i = 0; i < arg_types.size(); ++i) {
			String arg = arg_types.get(i);
			List<String> exps = Util.split_with_ops_the_types(arg);
			List<String> postfix = InfixToPostFix.infixToPostFix(exps);
			EvalExp eval_exp = new EvalExp(postfix, func_iden, var_iden);
			MsgType msg_type = eval_exp.deduce_final_type_from_types(symbol_table, func_scope_name, current_scope);

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
}
