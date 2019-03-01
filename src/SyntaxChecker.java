import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

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
	private static HashMap<Integer, Boolean> visited_ids;
	private static ErrorLog error_log;

	static ErrorLog validate_syntax(List<SequenceInfo> sequence_infos, MyFile file, List<RangeIndices> ri, HashMap<Integer, Integer> id_line) {
		error_log = new ErrorLog();
		num_sequences = sequence_infos.size();
		visited_ids = new HashMap<>();

		for(int i = 0; i < num_sequences; ++i)
			visited_ids.put(i, false);

		// structs
		for(int i = 0; i < num_sequences; ++i) {
			SequenceInfo s = sequence_infos.get(i);
			visited_ids.put(s.id, true);
			int res = -2;

			switch(s.seq_type) {
				case STRUCT:
					res = validate_struct(i, sequence_infos, ri, file, id_line);
					break;
				case FUNC:
					res = validate_func(i, sequence_infos, ri, file, id_line);
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

	private static int validate_struct(int index, List<SequenceInfo> sequence_infos, List<RangeIndices> ri, MyFile file, HashMap<Integer, Integer> id_line) {
		SequenceInfo struct_info = sequence_infos.get(index);
		if((index - 1) < 0) {
			error_log.push("Missing name for struct", "::struct", id_line.get(struct_info.id));
			return -1;
		}

		SequenceInfo expr_seq_info = sequence_infos.get(index - 1);
		visited_ids.put(expr_seq_info.id, true);
		if(expr_seq_info.seq_type != SequenceType.EXPRESSION) {
			error_log.push("Needed a 'name' before struct but found invalid string", expr_seq_info.str, id_line.get(expr_seq_info.id));
			return -1;
		}

		// Checking if struct name is valid ???
		if(!Util.is_valid_name(expr_seq_info.str)) {
			error_log.push("Name of struct '" + expr_seq_info.str + "' is not a valid name.", expr_seq_info.str, id_line.get(expr_seq_info.id));
			return -1;
		}

		if(index + 1 >= num_sequences) {
			error_log.push("Needed a '{' after ::struct but found nothing.", expr_seq_info.str, id_line.get(expr_seq_info.id));
			return -1;
		}

		SequenceInfo open_bracket_info = sequence_infos.get(index + 1);
		visited_ids.put(open_bracket_info.id, true);
		if(open_bracket_info.seq_type != SequenceType.OPEN_BRACKET) {
			error_log.push("'{' is missing after '" + expr_seq_info.str + "::struct", expr_seq_info.str + "::struct", id_line.get(open_bracket_info.id));
			return -1;
		}

		// getting all the variable declarations inside the struct
		int i = index + 2;
		int num_variables = 0;
		while(true) {
			if(i >= num_sequences)
				break;

			SequenceInfo var_decl_info = sequence_infos.get(i);
			visited_ids.put(var_decl_info.id, true);
			if(var_decl_info.seq_type == SequenceType.VAR_STAT) {
				String msg = var_decl_info.validate_syntax(ri);
				if(msg.equals("=")) {
					error_log.push("Only variable declaration/definition is allowed inside struct '" + expr_seq_info.str + "'", var_decl_info.str, id_line.get(var_decl_info.id));
					break;
				}
				else if(msg.length() > 4) { // There is some error
					error_log.push(msg, var_decl_info.str, id_line.get(var_decl_info.id));
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
					visited_ids.put(semi_colon_info.id, true);

					if(semi_colon_info.seq_type != SequenceType.SEMICOLON) {
						error_log.push("Missing ';' after declaration of variable inside struct '" + expr_seq_info.str + "'.", var_decl_info.str, id_line.get(semi_colon_info.id));
						break;
					}
					else {
						i += 1;
					}
				}

				num_variables += 1;
			}
			else if(var_decl_info.seq_type == SequenceType.CLOSED_BRACKET)
				break;
			else {
				error_log.push("Invalid Statement found inside struct '" + expr_seq_info.str + "'.", var_decl_info.str, id_line.get(var_decl_info.id));
				break;
			}

			i += 1;
		}

		if(i < num_sequences) {
			SequenceInfo close_bracket_info = sequence_infos.get(i);
			if(close_bracket_info.seq_type != SequenceType.CLOSED_BRACKET) {
				error_log.push("Needed a '}' after the definition of struct '" + expr_seq_info.str + "'.", close_bracket_info.str, id_line.get(close_bracket_info.id));

				return -1;
			}
			visited_ids.put(close_bracket_info.id, true);
		}
		else {
			error_log.push("Missing '}' after definition of struct '" + expr_seq_info.str + "'.", expr_seq_info.str, id_line.get(expr_seq_info.id));
			return -1;
		}

		return index + (2 + num_variables * 2);
	}

	private static int validate_func(int index, List<SequenceInfo> sequence_infos, List<RangeIndices> ri, MyFile file, HashMap<Integer, Integer> id_line) {
		SequenceInfo func_info = sequence_infos.get(index);
		if(index + 1 >= num_sequences) {
			error_log.push("Missing function signature after keyword 'func'.", "func", id_line.get(func_info.id));

			return -1;
		}

		SequenceInfo func_seq_info = sequence_infos.get(index + 1);
		visited_ids.put(func_seq_info.id, true);

		if(func_seq_info.seq_type != SequenceType.FUNC_NAME_ARGS) {
			error_log.push("Invalid function signature", func_seq_info.str, id_line.get(func_seq_info.id));
			return -1;
		}

		String msg = func_seq_info.validate_syntax(ri);
		if(!msg.equals("none")) {
			error_log.push(msg, func_seq_info.str, id_line.get(func_seq_info.id));
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
			error_log.push("Missing '{' after function '" + func_name + "'.", func_seq_info.str, id_line.get(func_seq_info.id));

			return -1;
		}

		SequenceInfo open_brac = sequence_infos.get(index + 2);
		visited_ids.put(open_brac.id, true);
		if(open_brac.seq_type != SequenceType.OPEN_BRACKET) {
			error_log.push("Needed '{' after function '" + func_name + "'. but found '" + open_brac.str + "'.", func_seq_info.str, id_line.get(func_seq_info.id));

			return -1;
		}
		
		int i = index + 3;
		SequenceInfo si = null;
		for(; i < num_sequences; ++i) {
			si = sequence_infos.get(i);
			if(si.seq_type == SequenceType.CLOSED_BRACKET) {
				boolean vtd = visited_ids.get(si.id);
				if(vtd)
					continue;
				else
					break;
			}

			if(si.seq_type == SequenceType.VAR_STAT) {
				int res = validate_var_stat(i, sequence_infos, ri, file, id_line);
				if(res == -1)
					return -1;
				
				i = res;
			}
			else if(si.seq_type == SequenceType.IF) {
				int res = validate_ifs(i, sequence_infos, ri, file, id_line);
				if(res == -1)
					return -1;

				i = res;
			}

			visited_ids.put(si.id, true);
		}

		if(i >= num_sequences) {
			error_log.push("Missing '}' at line '" + id_line.get(si.id) + ".'", func_seq_info.str, id_line.get(si.id));

			return -1;
		}

		return index + 1; // tmp;
	}

	private static int validate_ifs(int index, List<SequenceInfo> sequence_infos, List<RangeIndices> ri, MyFile file, HashMap<Integer, Integer> id_line) {
		SequenceInfo if_info = sequence_infos.get(index);
		visited_ids.put(if_info.id, true);
		
		if(index + 1 >= num_sequences) {
			error_log.push("Missing expression after 'if'", "if", id_line.get(if_info.id));

			return -1;
		}

		// @Note: An if condition cannot be an expression for another if.
		SequenceInfo exp_info = sequence_infos.get(index + 1);
		if(exp_info.seq_type != SequenceType.EXPRESSION) {
			error_log.push("Needed an expression after 'if' but found '" + exp_info.str + "'.", exp_info.str, id_line.get(exp_info.id));

			return -1;
		}
		visited_ids.put(exp_info.id, true);

		// Checking for {
		if(index + 2 >= num_sequences) {
			error_log.push("Missing '{' after expression in 'if'", exp_info.str, id_line.get(exp_info.id));

			return -1;
		}

		SequenceInfo open_brac = sequence_infos.get(index + 2);
		if(open_brac.seq_type != SequenceType.OPEN_BRACKET) {
			error_log.push("Needed a '{' after expression in 'if' but found '" + open_brac.str + "'.", open_brac.str, id_line.get(open_brac.id));

			return -1;
		}
		visited_ids.put(open_brac.id, true);

		for(int i = index + 3; i < num_sequences; ++i) {
			SequenceInfo si = sequence_infos.get(i);
			if(si.seq_type == SequenceType.CLOSED_BRACKET) {
				boolean vtd = visited_ids.get(si.id);
				if(!vtd) {
					visited_ids.put(si.id, true);
					break;
				}
			}
		}

		return index + 3;
	}

	private static int validate_var_stat(int index, List<SequenceInfo> sequence_infos, List<RangeIndices> ri, MyFile file, HashMap<Integer, Integer> id_line) {
		SequenceInfo si = sequence_infos.get(index);
		visited_ids.put(si.id, true);

		if((index + 1) >= num_sequences) {
			error_log.push("Missing ';' after '" + si.str + "'.", si.str, id_line.get(si.id));

			return -1;
		}

		String msg = si.validate_syntax(ri);
		if(msg.length() > 4) {
			error_log.push(msg, si.str, id_line.get(si.id));
			return -1;	
		}

		SequenceInfo si_2 = sequence_infos.get(index + 1);
		visited_ids.put(si_2.id, true);
		if(si_2.seq_type != SequenceType.SEMICOLON) {
			error_log.push("Needed ';' after '" + si.str + "' but found '" + 
					si_2.str + ".", si_2.str, id_line.get(si_2.id));
		}
		
		return index + 1;
	}
}
