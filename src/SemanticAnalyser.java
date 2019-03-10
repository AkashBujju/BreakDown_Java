import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;
import java.util.HashMap;

public class SemanticAnalyser {
	List<Info> infos;	
	SymbolTable symbol_table;
	List<String> errors;
	List<Integer> func_sig_indices;
	List<RangeIndices> quotes_range_indices;

	SemanticAnalyser(List<Info> infos, List<RangeIndices> quotes_range_indices) {
		this.infos = infos;
		this.quotes_range_indices = quotes_range_indices;
		errors = new ArrayList<>();
		func_sig_indices = new ArrayList<>();
		symbol_table = new SymbolTable();

		int count = 0;
		for(Info i: infos) {
			if(i.info_type == InfoType.FUNCTION) {
				func_sig_indices.add(count);
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

		System.out.println("\nsplit_value: ");
		for(String s: split_value) {
			System.out.println("< " + s + " >");
			String type = get_type(s);
		}

		return  ""; // @tmp
	}

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
		if(if_func_call) {
			return get_type_of_func_call(s);
		}
		return "not_known";
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
		List<String> all_funcs = Util.get_all_func_calls(inner_args);
		HashMap<String, String> exp_type_map = new HashMap<>();

		for(String func: all_funcs) {
			exp_type_map.put(func, "not_known");
			String in_arg = func.substring(func.indexOf('(') + 1, func.lastIndexOf(')'));
			boolean is_func = Util.contains_func_call(in_arg);
			if(!is_func) {
				// Convert the exp into postfix and get it's type.
				List<String> in_list = Util.split_with_ops(in_arg);
				List<String> out_list = InfixToPostFix.infixToPostFix(in_list);
				exp_type_map.put(in_arg, "not_known");
				System.out.println("postfix: <" + out_list + ">");
			}
		}

		/*
			System.out.println("Args: ");
			for(String str: args) {
			System.out.println(str);
			}
			System.out.println();
			*/

		return "not_known";
	}

	boolean func_with_num_args_exists(String name, int num_args) {
		for(Integer i: func_sig_indices) {
			FunctionInfo func_info = (FunctionInfo)(infos.get(i));	
			if(func_info.name.equals(name) && num_args == func_info.var_args.size())
				return true;
		}

		return false;
	}
}
