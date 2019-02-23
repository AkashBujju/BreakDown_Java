import java.util.List;
import java.util.ArrayList;

class VariableInfo {
	String name;
	String type;
	String raw_value;
	String scope;
}

class FunctionInfo {
	String name;
	List<String> var_names;
	List<String> var_types;
	String return_type;
}

class StructInfo {
	String name;
	List<String> var_names;
	List<String> var_types;
}

public class Info {
	private List<FunctionInfo> func_infos;
	private List<StructInfo> struct_infos;
	private SymbolTable symbol_table;
	private ErrorLog error_log;
	private int current_char_index = -1;
	private int current_line_number = 0;
	private MyFile file;
	private int num_sequences;
	private List<String> primitive_types;
	List<SequenceInfo> sequence_infos;

	Info(List<SequenceInfo> sequence_infos, MyFile file) {
		this.sequence_infos = sequence_infos;
		this.file = file;

		func_infos = new ArrayList<>();
		struct_infos = new ArrayList<>();
		symbol_table = new SymbolTable();
		error_log = new ErrorLog();

		primitive_types = new ArrayList<>();
		primitive_types.add("int_8");primitive_types.add("uint_8");
		primitive_types.add("int_16");primitive_types.add("uint_16");
		primitive_types.add("int_32");primitive_types.add("uint_32");
		primitive_types.add("int_64");primitive_types.add("uint_64");
		primitive_types.add("double");
		primitive_types.add("bool");
		primitive_types.add("char");

		num_sequences = sequence_infos.size();
	}

	private void update_line_number(String str) {
		int len = str.length();
		current_char_index += len;
		current_line_number = file.get_line_number(current_char_index);
	}

	void process() {
		current_char_index = 0;
		current_line_number = 1;

		// @Note: first process all the structs ....

		// structs

		// func ( name, args name, arg types, return_types )
		for(int i = 0; i < num_sequences; ++i) {
			SequenceInfo s = sequence_infos.get(i);

			if(s.seq_type == SequenceType.FUNC) {
				i = process_func_sig(i);
			}

			update_line_number(s.str);
		}

		// Checking and displaying errors and quitting...
		if(error_log.log.size() > 0) {
			error_log.show();
			return;
		}

		//@Incomplete ....
	}

	private int process_func_sig(int index) {

		if(index + 1 >= num_sequences) {
			error_log.push("Missing function signature after keyword 'func'.", "func", current_line_number);

			return index + 1;
		}

		SequenceInfo func_seq_info = sequence_infos.get(index + 1);
		update_line_number(func_seq_info.str);

		if(func_seq_info.seq_type != SequenceType.FUNC_NAME_ARGS) {
			error_log.push("Invalid function signature", func_seq_info.str, current_line_number);
			return index + 1;
		}

		String msg = func_seq_info.validate_syntax();
		if(!msg.equals("none")) {
			error_log.push(msg, func_seq_info.str, current_line_number);
			return index + 1;
		}

		List<String> split_str = func_seq_info.split_func_name_args();
		String func_name = split_str.get(0);
		String ret_type = split_str.get(split_str.size() - 1);

		for(int i = 1; i < split_str.size() - 1; i += 2) {
			String arg_name = split_str.get(i);
			String arg_type = split_str.get(i + 1);

			// arg_name's validity is already checked.. so skipping ahead ...
			// Checking if the types are available....

			// if primitive type ????
			boolean is_valid_primitive_type = Util.is_valid_type(arg_type, primitive_types);
			if(!is_valid_primitive_type) {
				error_log.push("Could not find type '" + arg_type + "'.", func_seq_info.str, current_line_number);
				
				return index + 1;
			}

			// @Incomplete: Check if its a user defined type ??????????
			// @Incomplete: Check if its a user defined type ??????????
		}

		return index + 1;
	}
}
