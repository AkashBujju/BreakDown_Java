import java.util.List;
import java.util.ArrayList;

class VariableInfo {
	String name;
	String type;
	String raw_value;
	String scope;

	void display() {
		System.out.println("Name:" + name + ", Type:" + type + ", " + "Value:" + raw_value + ", Scope:" + scope);
	}
}

class FunctionInfo {
	String name;
	String return_type;
	List<String> arg_names = new ArrayList<>();
	List<String> arg_types = new ArrayList<>();
}

class StructInfo {
	String name;
	List<VariableInfo> vars = new ArrayList<>();

	boolean add(VariableInfo vi) {
		for(VariableInfo v: vars) {
			if(v.name.equals(vi.name))
				return false;
		}

		vars.add(vi);

		return true;
	}

	void display() {
		System.out.println("StructName: " + name);
		System.out.println("Members are: ");
		for(VariableInfo vi: vars)
			vi.display();
	}
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
	private List<RangeIndices> ri;
	List<SequenceInfo> sequence_infos;

	Info(List<SequenceInfo> sequence_infos, MyFile file, List<RangeIndices> ri) {
		this.sequence_infos = sequence_infos;
		this.ri = ri;
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

	private void update_line_number(String str, boolean increase) {
		int len = str.length();
		if(increase)
			current_char_index += len;
		else
			current_char_index -= len;

		current_line_number = file.get_line_number(current_char_index);
		if(current_line_number == -1)
			current_line_number = file.get_line_number(file.data.length() - 1);
	}

	void process() {
		current_char_index = 0;
		current_line_number = 1;

		// @Note: first process all the structs ....

		// structs
		for(int i = 0; i < num_sequences; ++i) {
			SequenceInfo s = sequence_infos.get(i);
			if(s.seq_type == SequenceType.STRUCT)
				process_struct(i);

			update_line_number(s.str, true);
		}

		// Checking and displaying errors and quitting...
		if(error_log.log.size() > 0) {
			error_log.show();
			return;
		}


		// @Tmp: Displying struct_infos
		for(StructInfo si: struct_infos) {
			si.display();
			System.out.println();
		}

		// function signature
		current_line_number = 1;
		for(int i = 0; i < num_sequences; ++i) {
			SequenceInfo s = sequence_infos.get(i);
			if(s.seq_type == SequenceType.FUNC)
				process_func_sig(i);

			update_line_number(s.str, true);
		}

		// Checking and displaying errors and quitting...
		if(error_log.log.size() > 0) {
			error_log.show();
			return;
		}

		//@Incomplete ....
	}

	private int process_struct(int index) {
		if((index - 1) < 0) {
			error_log.push("Missing name for struct", "::struct", current_line_number);
			return -1;
		}

		SequenceInfo expr_seq_info = sequence_infos.get(index - 1);
		update_line_number(expr_seq_info.str, false);
		if(expr_seq_info.seq_type != SequenceType.EXPRESSION) {
			error_log.push("Needed a 'name' before struct but found invalid string", expr_seq_info.str, current_line_number);
			return -1;
		}

		// Checking if struct name is valid ???
		if(!Util.is_valid_name(expr_seq_info.str)) {
			error_log.push("Name of struct '" + expr_seq_info.str + "' is not a valid name.", expr_seq_info.str, current_line_number);
			return -1;
		}

		if(index + 1 >= num_sequences) {
			error_log.push("Needed a '{' after ::struct but found nothing.", expr_seq_info.str, current_line_number);
			return -1;
		}

		SequenceInfo open_bracket_info = sequence_infos.get(index + 1);
		update_line_number(expr_seq_info.str, true);
		update_line_number("::struct", true);
		if(open_bracket_info.seq_type != SequenceType.OPEN_BRACKET) {
			error_log.push("'{' is missing after '" + expr_seq_info.str + "::struct", expr_seq_info.str + "::struct", current_line_number);
			return -1;
		}
		update_line_number(open_bracket_info.str, true);

		// Creating a StructInfo
		StructInfo si = new StructInfo();
		si.name = expr_seq_info.str;

		// getting all the variable declarations inside the struct
		int i = index + 2;
		while(true) {
			if(i >= num_sequences)
				break;

			SequenceInfo var_decl_info = sequence_infos.get(i);
			if(var_decl_info.seq_type == SequenceType.VAR_DECLARE_OR_DEFINE) {
				update_line_number(var_decl_info.str, true);
				String msg = var_decl_info.validate_syntax(ri);
				if(msg.equals("=")) {
					error_log.push("Only variable declaration/definition is allowed inside struct '" + expr_seq_info.str + "'", var_decl_info.str, current_line_number);
					break;
				}
				else if(msg.length() > 4) { // There is some error
					error_log.push(msg, var_decl_info.str, current_line_number);
					break;
				}

				// @Incomplete: The right-hand side ie. the expression should be a literal value .... or should be left empty.
				List<String> split_str = var_decl_info.split_str(ri);
				String name = split_str.get(0);
				String exp = "";
				String type = "";

				if(msg.equals(":="))
					exp = split_str.get(1);
				else if(msg.equals(":"))
					type = split_str.get(1);
				else if(msg.equals(":..=")) {
					type = split_str.get(1);
					exp = split_str.get(2);
				}

				// Note: Valdity of name is already checked
				// Adding the variables to struct_info
				VariableInfo vi = new VariableInfo();
				vi.name = name;
				if(type.equals(""))
					vi.type = "not_known";
				else
					vi.type = type;
				if(exp.equals(""))
					vi.raw_value = "not_known";
				else
					vi.raw_value = exp;
				vi.scope = "struct";

				if(!si.add(vi)) { // Variable name already in struct
					error_log.push("Redeclaration of name '" + name + "' inside struct '" + si.name + "'.", var_decl_info.str, current_line_number);

					break;
				}

				if((i + 1) < num_sequences) {
					SequenceInfo semi_colon_info = sequence_infos.get(i + 1);

					if(semi_colon_info.seq_type != SequenceType.SEMICOLON) {
						error_log.push("Missing ';' after declaration of variable inside struct '" + expr_seq_info.str + "'.", var_decl_info.str, current_line_number);
						break;
					}
					else {
						update_line_number(semi_colon_info.str, true);
						i += 1;
					}
				}
			}
			else if(var_decl_info.seq_type == SequenceType.CLOSED_BRACKET)
				break;
			else {
				error_log.push("Invalid Statement found inside struct '" + expr_seq_info.str + "'.", var_decl_info.str, current_line_number);
				break;
			}

			i += 1;
		}

		if(i < num_sequences) {
			SequenceInfo close_bracket_info = sequence_infos.get(i);
			if(close_bracket_info.seq_type != SequenceType.CLOSED_BRACKET) {
				error_log.push("Needed a '}' after the definition of struct '" + expr_seq_info.str + "'.", close_bracket_info.str, current_line_number);

				return -1;
			}
			update_line_number(close_bracket_info.str, true);
		}
		else {
			error_log.push("Missing '}' after definition of struct '" + expr_seq_info.str + "'.", expr_seq_info.str, current_line_number);
			return -1;
		}

		// Adding the StructInfo si to struct_infos
		struct_infos.add(si);

		return 0;
	}

	private int process_func_sig(int index) {
		if(index + 1 >= num_sequences) {
			error_log.push("Missing function signature after keyword 'func'.", "func", current_line_number);

			return -1;
		}

		SequenceInfo func_seq_info = sequence_infos.get(index + 1);
		update_line_number(func_seq_info.str, true);

		if(func_seq_info.seq_type != SequenceType.FUNC_NAME_ARGS) {
			error_log.push("Invalid function signature", func_seq_info.str, current_line_number);
			return -1;
		}

		String msg = func_seq_info.validate_syntax(ri);
		if(!msg.equals("none")) {
			error_log.push(msg, func_seq_info.str, current_line_number);
			return -1;
		}

		List<String> split_str = func_seq_info.split_str(ri);
		String func_name = split_str.get(0);
		String ret_type = split_str.get(split_str.size() - 1);

		for(int i = 1; i < split_str.size() - 1; i += 2) {
			String arg_name = split_str.get(i);
			String arg_type = split_str.get(i + 1);

			// arg_name's validity is already checked.. so skipping ahead ...
			// Checking if the types are available....

			// if primitive type ????
			boolean is_valid_primitive_type = Util.is_valid_primitive_type(arg_type, primitive_types);
			if(!is_valid_primitive_type) {
				error_log.push("Could not find type '" + arg_type + "'.", func_seq_info.str, current_line_number);

				return -1;
			}

			// @Incomplete: Check if its a user defined type ??????????
			// @Incomplete: Check if its a user defined type ??????????
		}

		return 0;
	}

	boolean hasStructName(String name) {
		for(StructInfo si: struct_infos)
			if(name.equals(si.name))
				return true;

		return false;
	}

	boolean addStrucInfo(StructInfo si) {
		boolean already_exists = hasStructName(si.name);
		if(already_exists == true)
			return false;

		struct_infos.add(si);
		return true;
	}

}
