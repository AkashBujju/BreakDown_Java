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
	List<SequenceInfo> sequence_infos;

	Info(List<SequenceInfo> sequence_infos, MyFile file) {
		this.sequence_infos = sequence_infos;
		this.file = file;

		func_infos = new ArrayList<>();
		struct_infos = new ArrayList<>();
		symbol_table = new SymbolTable();
		error_log = new ErrorLog();
	}

	private void update_line_number() {
		current_line_number = file.get_line_number(current_char_index);
	}

	void process() {
		current_char_index = 0;
		current_line_number = 1;

		// First func ( name, args name, arg types, return_types )
		for(SequenceInfo s: sequence_infos) {
			if(s.seq_type == SequenceType.FUNC_NAME_ARGS) {
				String msg = s.validate_syntax();
				if(!msg.equals("none")) {
					error_log.push(msg, current_line_number);
				}

			}
			current_char_index += s.str.length();
			update_line_number();
		}

		// Checking and displaying errors and quitting...
		if(error_log.log.size() > 0) {
			error_log.show();

			return;
		}

		//@Incomplete ....
	}
}
