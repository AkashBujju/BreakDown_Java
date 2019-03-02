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
	private int num_sequences;
	private HashMap<Integer, Boolean> visited_ids;
	private HashMap<Integer, Integer> id_line;
	private List<RangeIndices> ri;
	private SequenceInfo[] sequence_infos;
	private MyFile file;
	private ErrorLog error_log;

	SyntaxChecker(SequenceInfo[] sequence_infos, MyFile file, List<RangeIndices> ri, HashMap<Integer, Integer> id_line) {
		this.sequence_infos = sequence_infos;
		this.id_line = id_line;
		this.file = file;
		this.ri = ri;

		num_sequences = sequence_infos.length;
		error_log = new ErrorLog();
		visited_ids = new HashMap<>();
	}

	ErrorLog validate_syntax() {
		for(int i = 0; i < num_sequences; ++i)
			visited_ids.put(i, false);

		for(int i = 0; i < num_sequences; ++i) {
			SequenceInfo s = sequence_infos[i];
			visited_ids.put(s.id, true);
			int res = -2;

			switch(s.seq_type) {
				case STRUCT:
					res = validate_struct(i);
					break;
				case FUNC:
					res = validate_func(i);
					break;
				case VAR_STAT:
					res = validate_var_stat(i);
					break;
			}

			if(res == -1)
				return error_log;
			else if(res != -2) { // valid res value
				i = res;
			}
		}

		Iterator it = visited_ids.keySet().iterator();
		while(it.hasNext()) {
			Integer i = (Integer)(it.next());
			System.out.println("id: " + i + ", vtd: " + visited_ids.get(i));
		}

		return error_log;
	}

	private int validate_struct(int index) {
		SequenceInfo struct_info = sequence_infos[index];
		if((index - 1) < 0) {
			error_log.push("Missing name for struct", "::struct", id_line.get(struct_info.id));
			return -1;
		}

		SequenceInfo expr_seq_info = sequence_infos[index - 1];
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

		SequenceInfo open_bracket_info = sequence_infos[index + 1];
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

			SequenceInfo var_decl_info = sequence_infos[i];
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
					SequenceInfo semi_colon_info = sequence_infos[i + 1];
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
			SequenceInfo close_bracket_info = sequence_infos[i];
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

	private int validate_func(int index) {
		SequenceInfo func_info = sequence_infos[index];
		if(index + 1 >= num_sequences) {
			error_log.push("Missing function signature after keyword 'func'.", "func", id_line.get(func_info.id));

			return -1;
		}

		SequenceInfo func_seq_info = sequence_infos[index + 1];
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

		SequenceInfo open_brac = sequence_infos[index + 2];
		if(open_brac.seq_type != SequenceType.OPEN_BRACKET) {
			error_log.push("Needed '{' after function '" + func_name + "'. but found '" + open_brac.str + "'.", func_seq_info.str, id_line.get(func_seq_info.id));

			return -1;
		}
		visited_ids.put(open_brac.id, true);
		
		int i = index + 3;
		SequenceInfo si = null;
		for(; i < num_sequences; ++i) {
			si = sequence_infos[i];
			if(si.seq_type == SequenceType.CLOSED_BRACKET) {
				boolean vtd = visited_ids.get(si.id);
				if(vtd)
					continue;
				else
					break;
			}

			else if(si.seq_type == SequenceType.WHILE) {
				i = validate_while(i);
			}
			else if(si.seq_type == SequenceType.VAR_STAT) {
				i = validate_var_stat(i);
			}
			else if(si.seq_type == SequenceType.IF) {
				i = validate_ifs(i);
			}
			else {
				error_log.push("Invalid statement '" + si.str + "' found", si.str, id_line.get(si.id));

				return -1;
			}

			if(i == -1)
				return -1;

			visited_ids.put(si.id, true);
		}

		if(i >= num_sequences) {
			error_log.push("Missing '}' at line '" + id_line.get(si.id) + ".'", si.str, id_line.get(si.id));

			return -1;
		}

		return index + 1; // tmp;
	}

	private int validate_ifs(int index) {
		SequenceInfo if_info = sequence_infos[index];
		visited_ids.put(if_info.id, true);
		
		if(index + 1 >= num_sequences) {
			error_log.push("Missing expression after 'if'", "if", id_line.get(if_info.id));

			return -1;
		}

		// @Note: An if condition cannot be an expression for another if.
		SequenceInfo exp_info = sequence_infos[index + 1];
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

		SequenceInfo open_brac = sequence_infos[index + 2];
		if(open_brac.seq_type != SequenceType.OPEN_BRACKET) {
			error_log.push("Needed a '{' after expression in 'if' but found '" + open_brac.str + "'.", open_brac.str, id_line.get(open_brac.id));

			return -1;
		}
		visited_ids.put(open_brac.id, true);

		SequenceInfo si = null;
		int i = index + 3;
		for(; i < num_sequences; ++i) {
			si = sequence_infos[i];
			if(si.seq_type == SequenceType.IF) {
				 i = validate_ifs(i);
			}
			else if(si.seq_type == SequenceType.WHILE) {
				i = validate_while(i);
			}
			else if(si.seq_type == SequenceType.VAR_STAT) {
				i = validate_var_stat(i);
			}
			else if(si.seq_type == SequenceType.CLOSED_BRACKET) {
				boolean vtd = visited_ids.get(si.id);
				if(!vtd) {
					visited_ids.put(si.id, true);
					break;
				}
			}
			else {
				error_log.push("Invalid Statement '" + si.str + "' found.", si.str, id_line.get(si.id));

				return -1;
			}

			if(i == -1)
				return -1;

			visited_ids.put(si.id, true);
		}

		if(i >= num_sequences) {
			error_log.push("'if' is missing '}'.", si.str, id_line.get(si.id));
			return -1;
		}

		return i;
	}

	private int validate_while(int index) {
		SequenceInfo while_info = sequence_infos[index];
		visited_ids.put(while_info.id, true);

		if((index + 1) >= num_sequences) {
			error_log.push("Missing expression after '" + while_info.str + "'", while_info.str, id_line.get(while_info.id));

			return -1;
		}

		SequenceInfo exp_info = sequence_infos[index + 1];
		if(exp_info.seq_type != SequenceType.EXPRESSION) {
			error_log.push("Needed expression after 'while' but found '" + exp_info.str + "'.", exp_info.str, id_line.get(exp_info.id));

			return -1;
		}
		visited_ids.put(exp_info.id, true);

		if((index + 2) >= num_sequences) {
			error_log.push("Missing '{' after 'while'", exp_info.str, id_line.get(exp_info.id));

			return -1;
		}

		SequenceInfo open_brac_info = sequence_infos[index + 2];
		if(open_brac_info.seq_type != SequenceType.OPEN_BRACKET) {
			error_log.push("Needed '{' after 'while' but found '" + open_brac_info.str + "'.", open_brac_info.str, id_line.get(open_brac_info.id));

			return -1;
		}
		visited_ids.put(open_brac_info.id, true);

		int i = index + 3;
		SequenceInfo si = null;
		for(; i < num_sequences; ++i) {
			si = sequence_infos[i];
			if(si.seq_type == SequenceType.WHILE) {
				i = validate_while(i);
			}
			else if(si.seq_type == SequenceType.IF) {
				 i = validate_ifs(i);
			}
			else if(si.seq_type == SequenceType.VAR_STAT) {
				i = validate_var_stat(i);
			}
			else if(si.seq_type == SequenceType.CLOSED_BRACKET) {
				boolean vtd = visited_ids.get(si.id);
				if(!vtd) {
					visited_ids.put(si.id, true);
					break;
				}
			}
			else {
				error_log.push("Invalid Statement '" + si.str + "' found.", si.str, id_line.get(si.id));

				return -1;
			}

			if(i == -1)
				return -1;

			visited_ids.put(si.id, true);
		}

		if(i >= num_sequences) {
			error_log.push("'if' is missing '}'.", si.str, id_line.get(si.id));
			return -1;
		}
		
		return i;
	}

	private int validate_var_stat(int index) {
		SequenceInfo si = sequence_infos[index];
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

		SequenceInfo si_2 = sequence_infos[index + 1];
		visited_ids.put(si_2.id, true);
		if(si_2.seq_type != SequenceType.SEMICOLON) {
			error_log.push("Needed ';' after '" + si.str + "' but found '" + 
					si_2.str + "'.", si_2.str, id_line.get(si_2.id));
		}
		
		return index + 1;
	}
}
