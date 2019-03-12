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
	List<String> errors;
	List<Integer> func_sig_indices;
	List<RangeIndices> quotes_range_indices;
	List<FuncNameArgs> func_name_args;

	SemanticAnalyser(List<Info> infos, List<RangeIndices> quotes_range_indices) {
		this.infos = infos;
		this.quotes_range_indices = quotes_range_indices;
		errors = new ArrayList<>();
		func_sig_indices = new ArrayList<>();
		symbol_table = new SymbolTable();
		func_name_args = new ArrayList<>();

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

	private void init_all_func_scopes() {
		for(Integer i: func_sig_indices)
			init_func_scope(i);
	}

	private void init_func_scope(int index) {
		FunctionInfo func_info = (FunctionInfo)(infos.get(index));
		List<Info> in_infos = func_info.infos;
		String scope_name = func_info.scope_name;
		List<String> scope_names = new ArrayList<>();

		int num_scopes = get_num_scopes(in_infos, 0);
		for(int i = 0; i <= num_scopes; ++i) {
			scope_names.add(scope_name + i);
		}

		FunctionInfo new_func_info = func_info;
		new_func_info.scope_names = scope_names;
		infos.set(index, new_func_info);
	}

	int get_num_scopes(List<Info> i, int count) {
		for(Info info: i) {
			if(info.info_type == InfoType.IF) {
				IfInfo if_info = (IfInfo)(info);
				List<Info> ii = if_info.infos;
				count = get_num_scopes(ii, count + 1);
			}
			else if(info.info_type == InfoType.ELSE_IF) {
				ElseIfInfo if_info = (ElseIfInfo)(info);
				List<Info> ii = if_info.infos;
				count = get_num_scopes(ii, count + 1);
			}
			else if(info.info_type == InfoType.WHILE) {
				WhileInfo if_info = (WhileInfo)(info);
				List<Info> ii = if_info.infos;
				count = get_num_scopes(ii, count + 1);
			}
			else if(info.info_type == InfoType.ELSE) {
				ElseInfo if_info = (ElseInfo)(info);
				List<Info> ii = if_info.infos;
				count = get_num_scopes(ii, count + 1);
			}
		}

		return count;
	}

	public void start() throws FileNotFoundException {
		init_all_func_scopes();

		for(int i = 0; i < infos.size(); ++i) {
			Info info = infos.get(i);	
			String msg = "";
			if(info.info_type == InfoType.USE)
				msg = eval_use((UseInfo)(info));
			else if(info.info_type == InfoType.VAR_DECL)
				msg = eval_var_decl((VarDeclInfo)(info));
			else if(info.info_type == InfoType.FUNCTION)
				msg = eval_function((FunctionInfo)(info));

			if(!msg.equals("")) {
				errors.add(msg);
			}
		}

		for(String s: errors) {
			System.out.println(s);
			System.out.println();
		}
	}

	private String eval_function(FunctionInfo func_info) {

		return "";
	}

	private String eval_var_decl(VarDeclInfo var_decl_info) {
		// @Incomplete: Check if the variable exists in the symbol table ???	

		String raw_value = var_decl_info.raw_value;
		List<String> split_value = Util.split_with_ops(raw_value);
		List<String> final_exp = new ArrayList<>();

		System.out.println("\nsplit_value: ");
		for(String s: split_value) {
			if(Util.is_operator(s)) {
				final_exp.add(s);
			}
			else {
				System.out.println("< " + s + " >");
				String type = get_type(s);
				final_exp.add(type);
			}
		}

		return  ""; // @tmp
	}

	// @NotKnown: From where does the filepath start from ??????
	// @NotKnown: From where does the filepath start from ??????
	// @NotKnown: From where does the filepath start from ??????
	// @NotKnown: From where does the filepath start from ??????
	private String eval_use(UseInfo use_info) throws FileNotFoundException {
		String filename = use_info.filename;
		try {
			Scanner s = new Scanner(new BufferedReader(new FileReader(filename)));
		}
		catch(FileNotFoundException e) {
			return ErrorLog.get_msg("Could not find file: " + filename + ".", "use \"" + filename+ "\';", use_info.line_number);
		}

		return "";
	}

	String get_type(String s) {
		boolean if_func_call = Util.if_func_call(s);
		String type = "not_known";

		if(if_func_call)
			type = get_type_of_func_call(s);
		else
			type = get_type_of_exp(s);

		return type;
	}

	String get_type_of_exp(String s) {
		List<String> in_list = Util.split_with_ops(s);
		List<String> out_list = InfixToPostFix.infixToPostFix(in_list);

		EvalExp eval_exp = new EvalExp(out_list);
		String type = eval_exp.deduce_final_type(symbol_table, "", 0);
		// System.out.println("s: " + s + ", type: " + type);

		return type;
	}

	String get_type_of_func_call(String s) {
		List<String> args = Util.get_func_args(s);
		String func_name = s.substring(0, s.indexOf('('));
		boolean func_exists = func_with_num_args_exists(func_name, args.size());
		if(!func_exists) {
			System.out.println("Function with name '" + func_name + "' taking '" + args.size() + "' arguments dosen't exist.");
			return "not_known";
		}

		int index_of_open_paren = s.indexOf('(');
		int index_of_close_paren = s.lastIndexOf(')');
		String inner_args = s.substring(index_of_open_paren + 1, index_of_close_paren);

		System.out.println("inner_arg: " + inner_args);
		List<String> all_exps = Util.get_only_exp(inner_args, get_all_func_names());
		/*
		for(String exp: all_exps) {
			System.out.println("<" + exp + ">");
		}
		System.out.println();
		*/

		String new_str = inner_args;
		HashMap<String, String> exp_type_map = new HashMap<>();
		for(String exp: all_exps) {
			String type = get_type_of_exp(exp);
			exp_type_map.put(exp, type);

			new_str = Util.replace_in_str(new_str, exp, type);	
		}

		System.out.println("exps: " + all_exps);
		System.out.println("new_str <" + new_str + ">");

		List<String> all_funcs = Util.get_all_func_calls(new_str);
		System.out.println("FUNCS: ");
		for(String func: all_funcs) {
			System.out.println(func);
		}
		System.out.println();

		for(String func: all_funcs) {
			String in_args = func.substring(func.indexOf('(') + 1, func.indexOf(')'));
			if(!Util.contains_func_call(in_args)) {
				String type = get_type_of_one_func_call(func);
				exp_type_map.put(func, type);
				new_str = Util.replace_in_str(new_str, func, type);
			}
		}

		System.out.println("new_str <" + new_str + ">");

		/*
		System.out.println();
		System.out.println("exp_type_map: ");
		Set<String> keyset = exp_type_map.keySet();
		Iterator<String> it = keyset.iterator();
		while(it.hasNext()) {
			String key = it.next();
			System.out.println(key + " : " + exp_type_map.get(key));
		}
		*/

		return "not_known";
	}

	// @Note: Here the types of arguments should have already been deduced.
	String get_type_of_one_func_call(String s) {
		String in_arg = s.substring(s.indexOf('(') + 1, s.lastIndexOf(')'));
		String name = s.substring(0, s.indexOf('('));
		List<String> arg_types = Util.my_split(in_arg, ',');

		FuncNameArgs func_name_arg = get_func_name_with_args(name, arg_types);
		if(func_name_arg == null)
			return "not_known";

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
		for(FuncNameArgs func_name_arg: func_name_args)
			li.add(func_name_arg.name);

		return li;
	}
}
