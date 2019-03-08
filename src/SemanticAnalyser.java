import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

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

	public void start() throws FileNotFoundException {
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
		System.out.println("inner_args: " + inner_args);
		List<String> all_funcs = Util.get_all_func_calls(inner_args);

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
			if(func_info.name.equals(name) && num_args == func_info.var_names.size())
				return true;
		}

		return false;
	}
}
