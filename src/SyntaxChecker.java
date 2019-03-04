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
	private HashMap<Integer, Integer> id_char_index;
	private List<RangeIndices> ri;
	private SequenceInfo[] sequence_infos;
	private MyFile file;
	private ErrorLog error_log;
	private boolean inside_while = false;
	private boolean encountered_use = false;
	private boolean encountered_func = false;
	private boolean encountered_struct = false;
	private boolean encountered_enum = false;
	private boolean encountered_global_var = false;
	private boolean show_invalid_stats = true;

	SyntaxChecker(SequenceInfo[] sequence_infos, MyFile file, List<RangeIndices> ri, HashMap<Integer, Integer> id_line, HashMap<Integer, Integer> id_char_index) {
		this.sequence_infos = sequence_infos;
		this.id_line = id_line;
		this.file = file;
		this.ri = ri;
		this.id_char_index = id_char_index;

		num_sequences = sequence_infos.length;
		error_log = new ErrorLog();
		visited_ids = new HashMap<>();
	}

	ErrorLog validate_syntax() {
		for(int i = 0; i < num_sequences; ++i)
			visited_ids.put(i, false);

		for(int i = 0; i < num_sequences; ++i) {
			SequenceInfo s = sequence_infos[i];
			int res = -2;

			if(s.seq_type == SequenceType.STRUCT) {
				if(encountered_func) {
					error_log.push("Cannot define 'struct' after defining 'functions'.", s.str, id_line.get(s.id));
					show_invalid_stats = false;
					break;
				}
				res = validate_struct(i);
				encountered_struct = true;
			}
			else if(s.seq_type == SequenceType.FUNC) {
				res = validate_func(i);
				encountered_func = true;
			}
			else if(s.seq_type == SequenceType.VAR_STAT) {
				if(encountered_func) {
					error_log.push("Global variable '" + s.str + "' is declared in an invalid position.", s.str, id_line.get(s.id));
					show_invalid_stats = false;
					break;
				}
				res = validate_var_stat(i);
				encountered_global_var = true;
			}
			else if(s.seq_type == SequenceType.ENUM) {
				if(encountered_func) {
					error_log.push("enum '" + s.str + "' has to declared before any functions.", s.str, id_line.get(s.id));
					show_invalid_stats = false;
					break;
				}
				res = validate_enum(i);
				encountered_enum = true;
			}
			else if(s.seq_type == SequenceType.USE) {
				if(encountered_func || encountered_global_var || encountered_enum || encountered_struct) {
					error_log.push("'use' statement has to be before any of the other statements.", s.str, id_line.get(s.id));
					show_invalid_stats = false;
					break;
				}
				res = validate_use(i);
				encountered_use = true;
			}

			if(res == -1)
				return error_log;
			else if(res != -2) { // valid res value
				i = res;
			}
		}

		if(!show_invalid_stats)
			return error_log;

		Iterator it = visited_ids.keySet().iterator();
		while(it.hasNext()) {
			Integer i = (Integer)(it.next());
			boolean vtd = visited_ids.get(i);
			if(!vtd) {
				SequenceInfo si = sequence_infos[i];
				error_log.push("Invalid statement '" + si.str + "' found.", si.str, id_line.get(si.id));
			}
		}

		return error_log;
	}

	private int validate_struct(int index) {
		SequenceInfo struct_info = sequence_infos[index];
		if((index - 1) < 0) {
			error_log.push("Missing name for struct", "::struct", id_line.get(struct_info.id));
			return -1;
		}
		visited_ids.put(struct_info.id, true);

		SequenceInfo expr_seq_info = sequence_infos[index - 1];
		visited_ids.put(expr_seq_info.id, true);
		if(expr_seq_info.seq_type != SequenceType.EXPRESSION) {
			error_log.push("Needed a 'name' before struct but found invalid string", expr_seq_info.str, id_line.get(expr_seq_info.id));
			return -1;
		}

		String valid_exp = expr_seq_info.validate_syntax(ri, id_char_index.get(expr_seq_info.id));
		if(!valid_exp.equals("none")) {
			error_log.push(valid_exp, expr_seq_info.str, id_line.get(expr_seq_info.id));
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
				String msg = var_decl_info.validate_syntax(ri, id_char_index.get(var_decl_info.id));
				if(msg.equals("=")) {
					error_log.push("Only variable declaration/definition is allowed inside struct '" + expr_seq_info.str + "'", var_decl_info.str, id_line.get(var_decl_info.id));
					break;
				}
				else if(msg.length() > 4) { // There is some error
					error_log.push(msg, var_decl_info.str, id_line.get(var_decl_info.id));
					break;
				}

				// @Incomplete: The right-hand side ie. the expression should be a literal value .... or should be left empty.
				List<String> split_str = var_decl_info.split_str(ri, id_char_index.get(var_decl_info.id));
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
		visited_ids.put(func_info.id, true);

		SequenceInfo func_seq_info = sequence_infos[index + 1];
		visited_ids.put(func_seq_info.id, true);

		if(func_seq_info.seq_type != SequenceType.FUNC_NAME_ARGS) {
			error_log.push("Invalid function signature", func_seq_info.str, id_line.get(func_seq_info.id));
			return -1;
		}

		String msg = func_seq_info.validate_syntax(ri, id_char_index.get(func_seq_info.id));
		if(!msg.equals("none")) {
			error_log.push(msg, func_seq_info.str, id_line.get(func_seq_info.id));
			return -1;
		}

		List<String> split_str = func_seq_info.split_str(ri, id_char_index.get(func_seq_info.id));
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

		boolean recv_after_if = false;

		int i = index + 3;
		SequenceInfo si = null;
		for(; i < num_sequences; ++i) {
			si = sequence_infos[i];
			if(si.seq_type == SequenceType.CLOSED_BRACKET) {
				boolean vtd = visited_ids.get(si.id);
				if(vtd)
					continue;
				else
					visited_ids.put(si.id, true);
				break;
			}
			else if(si.seq_type == SequenceType.WHILE) {
				i = validate_while(i);
				recv_after_if = false;
			}
			else if(si.seq_type == SequenceType.EXPRESSION) {
				i = validate_exp(i);
				recv_after_if = false;
			}
			else if(si.seq_type == SequenceType.VAR_STAT) {
				i = validate_var_stat(i);
				recv_after_if = false;
			}
			else if(si.seq_type == SequenceType.IF) {
				i = validate_ifs(i);
				recv_after_if = true;
			}
			else if(si.seq_type == SequenceType.RETURN) {
				i = validate_return(i);
				recv_after_if = false;
			}
			else if(si.seq_type == SequenceType.ELSE) {
				if(recv_after_if == false) {
					error_log.push("@else if: 'else if' can only be followed after an 'if'.", si.str, id_line.get(si.id));

					return -1;
				}

				SequenceInfo si_2 = null;
				if(i + 1 < num_sequences)
					si_2 = sequence_infos[i + 1];
				if(si_2 != null && si_2.seq_type == SequenceType.IF)
					i = validate_else_if(i);
				else  {
					i = validate_else(i);
					recv_after_if = false;
				}
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
			error_log.push("Missing '}' for '" + si.str + "' which is at line " + id_line.get(si.id), si.str, id_line.get(si.id));

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

		String valid_exp = exp_info.validate_syntax(ri, id_char_index.get(exp_info.id));
		if(!valid_exp.equals("none")) {
			error_log.push(valid_exp, exp_info.str, id_line.get(exp_info.id));
			return -1;
		}

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

		boolean recv_after_if = false;

		SequenceInfo si = null;
		int i = index + 3;
		for(; i < num_sequences; ++i) {
			si = sequence_infos[i];
			if(si.seq_type == SequenceType.IF) {
				i = validate_ifs(i);
				recv_after_if = true;
			}
			else if(si.seq_type == SequenceType.ELSE) {
				if(recv_after_if == false) {
					error_log.push("'else' or 'else if' can only be followed after an 'if'.", si.str, id_line.get(si.id));

					return -1;
				}

				SequenceInfo si_2 = null;
				if(i + 1 < num_sequences)
					si_2 = sequence_infos[i + 1];
				if(si_2 != null && si_2.seq_type == SequenceType.IF)
					i = validate_else_if(i);
				else  {
					i = validate_else(i);
					recv_after_if = false;
				}
			}
			else if(si.seq_type == SequenceType.RETURN) {
				i = validate_return(i);
			}
			else if(si.seq_type == SequenceType.EXPRESSION) {
				i = validate_exp(i);
				recv_after_if = false;
			}
			else if(si.seq_type == SequenceType.WHILE) {
				i = validate_while(i);
				recv_after_if = false;
			}
			else if(si.seq_type == SequenceType.VAR_STAT) {
				i = validate_var_stat(i);
				recv_after_if = false;
			}
			else if(si.seq_type == SequenceType.BREAK && inside_while) {
				i = validate_break(i);
			}
			else if(si.seq_type == SequenceType.CONTINUE && inside_while) {
				i = validate_continue(i);
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

		String valid_exp = exp_info.validate_syntax(ri, id_char_index.get(exp_info.id));
		if(!valid_exp.equals("none")) {
			error_log.push(valid_exp, exp_info.str, id_line.get(exp_info.id));
			return -1;
		}

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

		inside_while = true;
		boolean recv_after_if = false;

		int i = index + 3;
		SequenceInfo si = null;
		for(; i < num_sequences; ++i) {
			si = sequence_infos[i];
			if(si.seq_type == SequenceType.WHILE) {
				i = validate_while(i);
				recv_after_if = false;
			}
			else if(si.seq_type == SequenceType.IF) {
				i = validate_ifs(i);
				recv_after_if = true;
			}
			else if(si.seq_type == SequenceType.EXPRESSION) {
				i = validate_exp(i);
				recv_after_if = false;
			}
			else if(si.seq_type == SequenceType.ELSE) {
				if(recv_after_if == false) {
					error_log.push("@while: 'else' or 'else if' can only be followed after an 'if'.", si.str, id_line.get(si.id));

					return -1;
				}

				SequenceInfo si_2 = null;
				if(i + 1 < num_sequences)
					si_2 = sequence_infos[i + 1];
				if(si_2 != null && si_2.seq_type == SequenceType.IF)
					i = validate_else_if(i);
				else {
					i = validate_else(i);
					recv_after_if = false;
				}
			}
			else if(si.seq_type == SequenceType.VAR_STAT) {
				i = validate_var_stat(i);
				recv_after_if = false;
			}
			else if(si.seq_type == SequenceType.BREAK) {
				i = validate_break(i);
			}
			else if(si.seq_type == SequenceType.CONTINUE) {
				i = validate_continue(i);
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

		inside_while = false;

		return i;
	}

	private int validate_break(int index) {
		SequenceInfo si = sequence_infos[index];

		if(index + 1 >= num_sequences) {
			error_log.push("Needed ';' after 'break'", si.str, id_line.get(si.id));
			return -1;
		}
		visited_ids.put(si.id, true);

		SequenceInfo semi_colon_info = sequence_infos[index + 1];
		if(semi_colon_info.seq_type != SequenceType.SEMICOLON) {
			error_log.push("Needed ';' after 'break' but found '" + semi_colon_info.str + "'.", semi_colon_info.str, id_line.get(semi_colon_info.id));
			return -1;
		}
		visited_ids.put(semi_colon_info.id, true);

		return index + 1;
	}

	private int validate_continue(int index) {
		SequenceInfo si = sequence_infos[index];

		if(index + 1 >= num_sequences) {
			error_log.push("Needed ';' after 'continue'", si.str, id_line.get(si.id));
			return -1;
		}
		visited_ids.put(si.id, true);

		SequenceInfo semi_colon_info = sequence_infos[index + 1];
		if(semi_colon_info.seq_type != SequenceType.SEMICOLON) {
			error_log.push("Needed ';' after 'continue' but found '" + semi_colon_info.str + "'.", semi_colon_info.str, id_line.get(semi_colon_info.id));
			return -1;
		}
		visited_ids.put(semi_colon_info.id, true);

		return index + 1;
	}

	private int validate_var_stat(int index) {
		SequenceInfo si = sequence_infos[index];
		visited_ids.put(si.id, true);

		if((index + 1) >= num_sequences) {
			error_log.push("Missing ';' after '" + si.str + "'.", si.str, id_line.get(si.id));

			return -1;
		}

		String msg = si.validate_syntax(ri, id_char_index.get(si.id));
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

	private int validate_use(int index) {
		SequenceInfo use_info = sequence_infos[index];

		if(index + 1 >= num_sequences) {
			error_log.push("Missing \"filename\" after 'use'.", use_info.str, id_line.get(use_info.id));

			return -1;
		}
		visited_ids.put(use_info.id, true);

		SequenceInfo filename_info = sequence_infos[index + 1];
		if(filename_info.seq_type != SequenceType.EXPRESSION) {
			error_log.push("Needed \"filename\" expression after 'use' but found '" + filename_info.str + "'.", filename_info.str, id_line.get(filename_info.id));

			return -1;
		}

		String filename = filename_info.str;
		if(filename.charAt(0) != '\"' || filename.charAt(filename.length() - 1) != '\"') {
			error_log.push("Enclose 'filename' inside double quotes.", filename_info.str, id_line.get(filename_info.id));

			return -1;
		}
		visited_ids.put(filename_info.id, true);

		if(index + 2 >= num_sequences) {
			error_log.push("Missing ';' after '" + use_info.str + " " + filename_info.str + "'.", filename_info.str, id_line.get(filename_info.id));

			return -1;
		}

		SequenceInfo semicolon_info = sequence_infos[index + 2];
		if(semicolon_info.seq_type != SequenceType.SEMICOLON) {
			error_log.push("Needed ';' after '" + filename_info.str + "' but found '" + semicolon_info.str + "'.", semicolon_info.str, id_line.get(semicolon_info.id));

			return -1;
		}
		visited_ids.put(semicolon_info.id, true);

		return index + 2;
	}

	private int validate_enum(int index) {
		SequenceInfo enum_seq = sequence_infos[index];
		visited_ids.put(enum_seq.id, true);

		if(index - 1 < 0) {
			error_log.push("Enum missing 'name' before '::enum'.", enum_seq.str,id_line.get(enum_seq.id));

			return -1;
		}

		SequenceInfo enum_name_seq = sequence_infos[index - 1];
		if(enum_name_seq.seq_type != SequenceType.EXPRESSION) {
			error_log.push("'" + enum_name_seq.str + "' cannot be a name of an 'enum'.", enum_name_seq.str, id_line.get(enum_name_seq.id));

			return -1;
		}

		if(!Util.is_valid_name(enum_name_seq.str)) {
			error_log.push("'" + enum_name_seq.str + "' is not a valid name for an 'enum'.", enum_name_seq.str, id_line.get(enum_name_seq.id));

			return -1;
		}
		visited_ids.put(enum_name_seq.id, true);

		if(index + 1 >= num_sequences) {
			error_log.push("Missing '{' after '::enum'", enum_seq.str, id_line.get(enum_seq.id));

			return -1;
		}

		SequenceInfo open_brac = sequence_infos[index + 1];
		if(open_brac.seq_type != SequenceType.OPEN_BRACKET) {
			error_log.push("Needed '{' after '::enum' but found '" + open_brac.str + "'.", open_brac.str, id_line.get(open_brac.id));

			return -1;
		}
		visited_ids.put(open_brac.id, true);

		if(index + 2 >= num_sequences) {
			error_log.push("Needed enum constants inside enum '" + enum_name_seq.str + "'.", enum_name_seq.str + "::enum", id_line.get(enum_seq.id));

			return -1;
		}

		SequenceInfo value_seq = sequence_infos[index + 2];
		if(value_seq.seq_type != SequenceType.EXPRESSION) {
			error_log.push("Invalid constants found inside enum '" + enum_name_seq.str + "'.", value_seq.str, id_line.get(value_seq.id));

			return -1;
		}

		String value = value_seq.str;
		List<String> split_value = Util.my_split(value, ',');
		for(String s: split_value) {
			if(!Util.is_all_caps(s)) {
				error_log.push("'" + s + "' is not a valid enum constant name(Only A to Z is valid).", s, id_line.get(value_seq.id));

				return -1;
			}
		}
		visited_ids.put(value_seq.id, true);

		if(index + 3 >= num_sequences) {
			error_log.push("Needed '}' after definition of enum '" + enum_name_seq.str + "'.", enum_name_seq.str, id_line.get(value_seq.id));

			return -1;
		}

		SequenceInfo close_brac = sequence_infos[index + 3];
		if(close_brac.seq_type != SequenceType.CLOSED_BRACKET) {
			error_log.push("Needed '}' after definition of enum '" + enum_name_seq.str + "' but found '" + close_brac.str + "'.", close_brac.str, id_line.get(close_brac.id));

			return -1;
		}
		visited_ids.put(close_brac.id, true);

		return index + 3;
	}

	private int validate_else(int index) {
		SequenceInfo else_seq = sequence_infos[index];
		int return_val = -1;

		if(index + 1 >= num_sequences) {
			error_log.push("Missing '{' after 'else'.", else_seq.str, id_line.get(else_seq.id));

			return -1;
		}
		visited_ids.put(else_seq.id, true);

		SequenceInfo open_brac = sequence_infos[index + 1];
		if(open_brac.seq_type != SequenceType.OPEN_BRACKET) {
			error_log.push("Needed '{' after 'else' but found '" + open_brac.str + "'.", open_brac.str, id_line.get(open_brac.id));

			return -1;
		}
		visited_ids.put(open_brac.id, true);

		boolean recv_after_if = false;
		int i = index + 2;
		SequenceInfo s = null;
		for(; i <= num_sequences; ++i) {
			s = sequence_infos[i];
			if(s.seq_type == SequenceType.ELSE) {
				if(recv_after_if == false) {
					error_log.push("@else if: 'else if' can only be followed after an 'if'.", s.str, id_line.get(s.id));

					return -1;
				}

				SequenceInfo si_2 = null;
				if(i + 1 < num_sequences)
					si_2 = sequence_infos[i + 1];
				if(si_2 != null && si_2.seq_type == SequenceType.IF)
					i = validate_else_if(i);
				else {
					i = validate_else(i);
					recv_after_if = false;
				}
			}
			else if(s.seq_type == SequenceType.EXPRESSION) {
				i = validate_exp(i);
				recv_after_if = false;
			}
			else if(s.seq_type == SequenceType.IF) {
				i = validate_ifs(i);
				recv_after_if = true;
			}
			else if(s.seq_type == SequenceType.WHILE) {
				i = validate_while(i);
				recv_after_if = false;
			}
			else if(s.seq_type == SequenceType.RETURN) {
				i = validate_return(i);
			}
			else if(s.seq_type == SequenceType.VAR_STAT) {
				i = validate_var_stat(i);
				recv_after_if = false;
			}
			else if(s.seq_type == SequenceType.BREAK && inside_while) {
				i = validate_break(i);
			}
			else if(s.seq_type == SequenceType.CONTINUE && inside_while) {
				i = validate_continue(i);
			}
			else if(s.seq_type == SequenceType.CLOSED_BRACKET) {
				boolean vtd = visited_ids.get(s.id);
				if(!vtd) {
					visited_ids.put(s.id, true);
					break;
				}
			}
			else {
				error_log.push("Invalid statement '" + s.str + "' found", s.str, id_line.get(s.id));
				return -1;	
			}

			if(i == -1)
				return -1;

			visited_ids.put(s.id, true);
		}

		recv_after_if = false;
		return_val = i;

		return return_val;
	}

	private int validate_else_if(int index) {
		SequenceInfo else_seq = sequence_infos[index];
		int return_val = -1;

		if(index + 1 >= num_sequences) {
			error_log.push("Missing '{' after 'else'.", else_seq.str, id_line.get(else_seq.id));

			return -1;
		}
		visited_ids.put(else_seq.id, true);

		SequenceInfo si_info = sequence_infos[index + 1];
		visited_ids.put(si_info.id, true);
		if(si_info.seq_type == SequenceType.IF) {

			if(index + 2 >= num_sequences) {
				error_log.push("Missing 'expression' after 'else if'.", "else if", id_line.get(si_info.id));

				return -1;
			}

			SequenceInfo exp_info = sequence_infos[index + 2];
			if(exp_info.seq_type != SequenceType.EXPRESSION) {
				error_log.push("Needed 'expression' after 'else if' but found '" + exp_info.str + "'.", exp_info.str, id_line.get(exp_info.id));

				return -1;
			}
			visited_ids.put(exp_info.id, true);

			String valid_exp = exp_info.validate_syntax(ri, id_char_index.get(exp_info.id));
			if(!valid_exp.equals("none")) {
				error_log.push(valid_exp, exp_info.str, id_line.get(exp_info.id));
				return -1;
			}

			if(index + 3 >= num_sequences) {
				error_log.push("Missing '{' after '" + exp_info + "' in 'else if'.", exp_info.str, id_line.get(exp_info.id));

				return -1;
			}

			SequenceInfo open_brac = sequence_infos[index + 3];
			if(open_brac.seq_type != SequenceType.OPEN_BRACKET) {
				error_log.push("Needed '{' after 'else if' expression but found '" + open_brac.str + "'.", open_brac.str, id_line.get(open_brac.id));

				return -1;
			}
			visited_ids.put(open_brac.id, true);

			boolean recv_after_if = false;

			int i = index + 4;
			SequenceInfo s = null;
			for(; i <= num_sequences; ++i) {
				s = sequence_infos[i];
				if(s.seq_type == SequenceType.ELSE) {
					if(recv_after_if == false) {
						error_log.push("@else: 'else' or 'else if' can only be followed after an 'if'.", s.str, id_line.get(s.id));
						return -1;
					}

					SequenceInfo si_2 = null;
					if(i + 1 < num_sequences)
						si_2 = sequence_infos[i + 1];
					if(si_2 != null && si_2.seq_type == SequenceType.IF)
						i = validate_else_if(i);
					else {
						i = validate_else(i);
						recv_after_if = false;
					}
				}
				else if(s.seq_type == SequenceType.EXPRESSION) {
					i = validate_exp(i);
					recv_after_if = false;
				}
				else if(s.seq_type == SequenceType.IF) {
					i = validate_ifs(i);
					recv_after_if = true;
				}
				else if(s.seq_type == SequenceType.RETURN) {
					i = validate_return(i);
				}
				else if(s.seq_type == SequenceType.WHILE) {
					i = validate_while(i);
					recv_after_if = false;
				}
				else if(s.seq_type == SequenceType.VAR_STAT) {
					i = validate_var_stat(i);
					recv_after_if = false;
				}
				else if(s.seq_type == SequenceType.BREAK && inside_while) {
					i = validate_break(i);
				}
				else if(s.seq_type == SequenceType.CONTINUE && inside_while) {
					i = validate_continue(i);
				}
				else if(s.seq_type == SequenceType.CLOSED_BRACKET) {
					boolean vtd = visited_ids.get(s.id);
					if(!vtd) {
						visited_ids.put(s.id, true);
						break;
					}
				}
				else {
					error_log.push("Invalid statement '" + s.str + "' found", s.str, id_line.get(s.id));
					return -1;	
				}

				if(i == -1)
					return -1;

				visited_ids.put(s.id, true);
			}

			return_val = i;
		}
		else {
			error_log.push("Needed '{' after 'else' but found '" + si_info.str + "'.", si_info.str, id_line.get(si_info.id));

			return -1;
		}

		return return_val;
	}

	private int validate_return(int index) {
		SequenceInfo ret_seq_info = sequence_infos[index];

		if(index + 1 >= num_sequences) {
			error_log.push("Missing 'expression' after 'return'.", ret_seq_info.str, id_line.get(ret_seq_info.id));

			return -1;
		}
		visited_ids.put(ret_seq_info.id, true);

		SequenceInfo exp_info = sequence_infos[index + 1];
		if(exp_info.seq_type != SequenceType.EXPRESSION) {
			error_log.push("Needed 'expression' after 'return' but found '" + exp_info.str + "'.", exp_info.str, id_line.get(exp_info.id));

			return -1;
		}
		visited_ids.put(exp_info.id, true);

		String valid_exp = exp_info.validate_syntax(ri, id_char_index.get(exp_info.id));
		if(!valid_exp.equals("none")) {
			error_log.push(valid_exp, exp_info.str, id_line.get(exp_info.id));
			return -1;
		}

		if(index + 2 >= num_sequences) {
			error_log.push("Missing ';' after expression '" + exp_info.str + "'.", exp_info.str, id_line.get(exp_info.id));

			return -1;
		}

		SequenceInfo semicolon_info = sequence_infos[index + 2];
		if(semicolon_info.seq_type != SequenceType.SEMICOLON) {
			error_log.push("Needed ';' after expression '" + exp_info.str + "' but found '" + semicolon_info.str + "'.", semicolon_info.str, id_line.get(semicolon_info.id));

			return -1;
		}
		visited_ids.put(semicolon_info.id, true);

		return index + 2;
	}

	private int validate_exp(int index) {
		SequenceInfo exp_info = sequence_infos[index];

		if(index + 1 >= num_sequences) {
			error_log.push("Missing ';' after expression '" + exp_info + "'.", exp_info.str, id_line.get(exp_info.id));

			return -1;
		}

		String msg = exp_info.validate_syntax(ri, id_char_index.get(exp_info.id));
		if(!msg.equals("none")) {
			error_log.push(msg, exp_info.str, id_line.get(exp_info.id));
			return -1;
		}
		visited_ids.put(exp_info.id, true);

		SequenceInfo semicolon_info = sequence_infos[index + 1];
		if(semicolon_info.seq_type != SequenceType.SEMICOLON) {
			error_log.push("Needed ';' after 'expression' but found '" + semicolon_info.str + "'.", semicolon_info.str, id_line.get(semicolon_info.id));

			return -1;
		}
		visited_ids.put(semicolon_info.id, true);

		return index + 1;
	}
	}
