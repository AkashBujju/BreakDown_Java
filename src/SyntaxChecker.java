import java.util.List;
import java.util.ArrayList;

/*
	primitive_types = new ArrayList<>();
	primitive_types.add("int_8");primitive_types.add("uint_8");
	primitive_types.add("int_16");primitive_types.add("uint_16");
	primitive_types.add("int_32");primitive_types.add("uint_32");
	primitive_types.add("int_64");primitive_types.add("uint_64");
	primitive_types.add("double");
	primitive_types.add("bool");
	primitive_types.add("char");
*/

public class SyntaxChecker {
	private static int num_sequences;
	private static int current_char_index = -1;
	private static int current_line_number = 0;
	private static ErrorLog error_log;

	static ErrorLog validate_syntax(List<SequenceInfo> sequence_infos, MyFile file, List<RangeIndices> ri) {
		current_char_index = -1;
		current_line_number = 0;
		error_log = new ErrorLog();
		num_sequences = sequence_infos.size();

		// start
		current_char_index = 0;
		current_line_number = 1;

		// structs
		for(int i = 0; i < num_sequences; ++i) {
			SequenceInfo s = sequence_infos.get(i);
			int res = -2;

			switch(s.seq_type) {
				case STRUCT:
					res = validate_struct(i, sequence_infos, ri, file);
					break;
				case FUNC:
					res = validate_func(i, sequence_infos, ri, file);
					break;
				case VAR_STAT:
					// global variables....
					break;
			}

			if(res == -1)
				return error_log;
			else if(res != -2) { // valid res value
				i = res;
			}
		}

		return error_log;
	}

	private static void update_line_number(String str, boolean increase, MyFile file) {
		int len = str.length();
		if(increase)
			current_char_index += len;
		else
			current_char_index -= len;

		current_line_number = file.get_line_number(current_char_index);
		if(current_line_number == -1)
			current_line_number = file.get_line_number(file.data.length() - 1);
	}

	private static int validate_struct(int index, List<SequenceInfo> sequence_infos, List<RangeIndices> ri, MyFile file) {
		if((index - 1) < 0) {
			error_log.push("Missing name for struct", "::struct", current_line_number);
			return -1;
		}

		SequenceInfo expr_seq_info = sequence_infos.get(index - 1);
		update_line_number(expr_seq_info.str, false, file);
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
		update_line_number(expr_seq_info.str, true, file);
		update_line_number("::struct", true, file);
		if(open_bracket_info.seq_type != SequenceType.OPEN_BRACKET) {
			error_log.push("'{' is missing after '" + expr_seq_info.str + "::struct", expr_seq_info.str + "::struct", current_line_number);
			return -1;
		}
		update_line_number(open_bracket_info.str, true, file);

		// getting all the variable declarations inside the struct
		int i = index + 2;
		int num_variables = 0;
		while(true) {
			if(i >= num_sequences)
				break;

			SequenceInfo var_decl_info = sequence_infos.get(i);
			if(var_decl_info.seq_type == SequenceType.VAR_STAT) {
				update_line_number(var_decl_info.str, true, file);
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

				if((i + 1) < num_sequences) {
					SequenceInfo semi_colon_info = sequence_infos.get(i + 1);

					if(semi_colon_info.seq_type != SequenceType.SEMICOLON) {
						error_log.push("Missing ';' after declaration of variable inside struct '" + expr_seq_info.str + "'.", var_decl_info.str, current_line_number);
						break;
					}
					else {
						update_line_number(semi_colon_info.str, true, file);
						i += 1;
					}
				}

				num_variables += 1;
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
			update_line_number(close_bracket_info.str, true, file);
		}
		else {
			error_log.push("Missing '}' after definition of struct '" + expr_seq_info.str + "'.", expr_seq_info.str, current_line_number);
			return -1;
		}

		return index + (2 + num_variables * 2);
	}

	private static int validate_func(int index, List<SequenceInfo> sequence_infos, List<RangeIndices> ri, MyFile file) {
		if(index + 1 >= num_sequences) {
			error_log.push("Missing function signature after keyword 'func'.", "func", current_line_number);

			return -1;
		}

		SequenceInfo func_seq_info = sequence_infos.get(index + 1);
		update_line_number(func_seq_info.str, true, file);

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
		}

		// Checking for {
		if((index + 2) >= num_sequences) {
			error_log.push("Missing '{' after function '" + func_name + "'.", func_seq_info.str, current_line_number);

			return -1;
		}

		SequenceInfo open_brac = sequence_infos.get(index + 2);
		update_line_number(open_brac.str, true, file);
		if(open_brac.seq_type != SequenceType.OPEN_BRACKET) {
			error_log.push("Needed '{' after function '" + func_name + "'. but found '" + open_brac.str + "'.", func_seq_info.str, current_line_number);

			return -1;
		}

		int i = index + 3;
		int open_bracket_count = 0;
		int closed_bracket_count = 0;
		for(; i < num_sequences; ++i) {
			SequenceInfo si = sequence_infos.get(i);
			if(si.seq_type == SequenceType.CLOSED_BRACKET) {
				closed_bracket_count += 1;
				if(closed_bracket_count > open_bracket_count) // have to exit function
					break;
			}

			if(si.seq_type == SequenceType.VAR_STAT) {
				int res = validate_var_stat(i, sequence_infos, ri, file);
				if(res == -1)
					return -1;
				
				i = res;
			}
			else if(si.seq_type == SequenceType.IF) {
				int res = validate_ifs(i, sequence_infos, ri, file);
				if(res == -1)
					return -1;

				i = res;
			}
		}

		if(i >= num_sequences) {
			error_log.push("Needed '}' after function '" + func_name + ".'", func_seq_info.str, current_line_number);

			return -1;
		}

		// @Incomplete: This only works for function with empty body...
		// @Incomplete: This only works for function with empty body...
		// @Incomplete: This only works for function with empty body...
		// @Incomplete: This only works for function with empty body...
		return index + 1; // tmp
	}

	private static int validate_ifs(int index, List<SequenceInfo> sequence_infos, List<RangeIndices> ri, MyFile file) {
		update_line_number("if", true, file);
		System.out.println("line_number: " + current_line_number);
		if(index + 1 >= num_sequences) {
			error_log.push("Missing expression after 'if'", "if", current_line_number);

			return -1;
		}

		// @Note: An if condition cannot be an expression for another if.
		SequenceInfo exp_info = sequence_infos.get(index + 1);
		update_line_number(exp_info.str, true, file);
		if(exp_info.seq_type != SequenceType.EXPRESSION) {
			error_log.push("Needed an expression after 'if' but found '" + exp_info.str + "'.", exp_info.str, current_line_number);

			return -1;
		}

		// Checking for {
		if(index + 2 >= num_sequences) {
			error_log.push("Missing '{' after expression in 'if'", exp_info.str, current_line_number);

			return -1;
		}

		SequenceInfo open_brac = sequence_infos.get(index + 2);
		update_line_number(open_brac.str, true, file);
		if(open_brac.seq_type != SequenceType.OPEN_BRACKET) {
			error_log.push("Needed a '{' after expression in 'if' but found '" + open_brac.str + "'.", open_brac.str, current_line_number);

			return -1;
		}

		// @Incomplete ....

		return 0; // tmp
	}

	private static int validate_var_stat(int index, List<SequenceInfo> sequence_infos, List<RangeIndices> ri, MyFile file) {
		SequenceInfo si = sequence_infos.get(index);
		update_line_number(si.str, true, file);

		if((index + 1) >= num_sequences) {
			error_log.push("Missing ';' after '" + si.str + "'.", si.str, current_line_number);

			return -1;
		}

		String msg = si.validate_syntax(ri);
		if(msg.length() > 4) {
			error_log.push(msg, si.str, current_line_number);
			return -1;	
		}

		SequenceInfo si_2 = sequence_infos.get(index + 1);
		update_line_number(si_2.str, true, file);
		if(si_2.seq_type != SequenceType.SEMICOLON) {
			error_log.push("Needed ';' after '" + si.str + "' but found '" + 
					si_2.str + ".", si_2.str, current_line_number);
		}
		
		return index + 1;
	}
}
