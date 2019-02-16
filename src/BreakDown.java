import java.util.List;
import java.util.ArrayList;

/*
	enum StatementType {
	VAR_DECLARE_1,
	VAR_DECLARE_2,
	VAR_ASSIGN,
	ENUM,
	STRUCT,
	IF,
	ELSE,
	ELSE_IF,
	FUNC,
	WHILE,
	USE
	}

	class StatementInfo {
	StatementType type;
	}
	*/

/*
	class SymbolTable {
	List<VarInfo> varinfos;

	SymbolTable() {
	varinfos = new ArrayList<>();
	}
	}

	class VarInfo {
	String name;
	String type;
	String value;
	}

	class StructInfo {
	String name;
	}
	*/

class NotknownRange {
	int start_index = -1;
	int end_index = -1;
	boolean is_active = false;
}

/*
	@Incomplete: Only check if the statements are incomplete. and add messages to the ErrorLog object.
	*/

class BreakDown {
	private String str;
	private int current_index = 0;
	private int last_sequence_index = -1;
	private List<NotknownRange> n_range;
	private String char_sequences;

	BreakDown(String str) {
		this.str = str;
		n_range = new ArrayList<>();
		char_sequences = ":,=,:=,::struct,::enum,func,if,else,elseif,use";
	}

	private boolean do_variable_name_operators_come_first() {
		String pat = Util.get_first_occuring_pattern(str, current_index, ":=,=,::struct,::enum,if,else,func,elseif");

		if(pat.equals(""))
			return false;

		if(pat.equals(":=") || pat.equals("=") || pat.equals("::struct") || pat.equals("::enum")) {
			return true;
		}

		// Explicitly checking if a:int = 10; type...
		int index_of_error_sequences = Util.get_first_index_of(str, current_index, "while,func,if,else,elseif");
		int index_of_colon = str.indexOf(':', current_index);
		int index_of_equal = str.indexOf('=', current_index);
		if(index_of_colon < index_of_error_sequences && index_of_equal < index_of_error_sequences)
			return true;

		// It is one of the sequences that produce errors.
		return false;
	}

	private NotknownRange get_garbage_indices_and_update_current_index() {
		NotknownRange nr = new NotknownRange();

		// Checking for all the character sequences
		boolean variable_name_ops_first = do_variable_name_operators_come_first();
		if(variable_name_ops_first) {
			return nr;
		}

		int index_of_char_seq = Util.get_first_index_of(str, current_index, "func,::struct,::enum,if,else,elseif,use");
		if(index_of_char_seq == -1) {
			return nr;
		}

		nr.start_index = current_index;
		nr.end_index = index_of_char_seq;
		nr.is_active = true;

		current_index = index_of_char_seq - 1;
		last_sequence_index = index_of_char_seq - 1;

		return nr;
	}

	private String get_enum_value() {
		String value = "";

		// Pattern: <enum><name><value>
		char c = str.charAt(current_index);
		if(c == ':') {
			int index_of_enum = str.indexOf("::enum", current_index - 1);
			if(index_of_enum == -1)
				return "";
			if(current_index != index_of_enum)
				return "";

			int index_of_open_bracket = str.indexOf('{', index_of_enum + 6);
			if(index_of_open_bracket == -1)
				return "";

			int index_of_closing_bracket = str.indexOf('}', index_of_open_bracket + 1);
			if(index_of_closing_bracket == -1)
				return "";

			value += "@enum";
			value += "@" + str.substring(last_sequence_index + 1, index_of_enum);
			value += "@" + str.substring(index_of_open_bracket + 1, index_of_closing_bracket) + "@";

			current_index = index_of_closing_bracket;
			last_sequence_index = index_of_closing_bracket;
		}

		return value;
	}

	private String get_use_value() {
		String value = "";

		// Ex: use "demo.veg";
		// Pattern: <use><value>
		char c = str.charAt(current_index);
		if(c == 'u') {
			int index_of_use = str.indexOf("use", current_index);
			if(index_of_use == -1)
				return "";
			if(current_index != index_of_use)
				return "";

			int index_of_semicolon = str.indexOf(';', index_of_use + 3);
			if(index_of_semicolon == -1)
				return "";

			value = "@use";
			value += "@" + str.substring(current_index + 3, index_of_semicolon + 1) + "@";

			current_index = index_of_semicolon;
			last_sequence_index = index_of_semicolon;
		}

		return value;
	}

	private String else_if_value() {
		String value = "";

		// Ex: else if expression {}
		// Pattern: <elseif><condition><value>

		char c = str.charAt(current_index);
		if(c == 'e') {
			int index_of_else = str.indexOf("elseif", current_index);
			if (index_of_else == -1)
				return "";
			if(current_index != index_of_else)
				return "";

			int index_open_braces = str.indexOf('{', current_index);
			if(index_open_braces == -1)
				return "";

			int index_of_closing_braces = str.indexOf('}', index_open_braces);
			if(index_of_closing_braces == -1)
				return "";

			value = "@elseif";
			value += "@" + str.substring(current_index + 6, index_open_braces);
			value += "@" + str.substring(index_open_braces + 1, index_of_closing_braces) + "@";

			current_index = index_of_closing_braces;
			last_sequence_index = index_of_closing_braces;
		}

		return  value;
	}

	private String else_value() {
		String value = "";

		// Ex: else {}
		// Pattern: <else><value>
		char c = str.charAt(current_index);
		if(c == 'e') {
			int index_of_else = str.indexOf("else", current_index);
			if(index_of_else == -1)
				return "";
			if(current_index != index_of_else)
				return "";

			// Checking if it's not else if
			int index_of_else_if = str.indexOf("elseif", current_index);
			if(index_of_else_if != -1)
				return "";

			int index_open_braces = str.indexOf('{', current_index);
			if(index_open_braces == -1)
				return "";

			int index_of_closing_braces = str.indexOf('}', index_open_braces);
			if(index_of_closing_braces == -1)
				return "";

			value = "@else";
			value += "@" + str.substring(index_open_braces + 1, index_of_closing_braces) + "@";

			current_index = index_of_closing_braces;
			last_sequence_index = index_of_closing_braces;
		}

		return value;
	}

	private String if_value() {
		String value = "";
		// Ex: if expression <>
		// Pattern: <if><condition><value>

		char c = str.charAt(current_index);
		if(c == 'i') {
			int if_index = str.indexOf("if", current_index);
			if(if_index == -1)
				return "";
			if(current_index != if_index)
				return "";

			int index_open_braces = str.indexOf('{', current_index);
			if(index_open_braces == -1)
				return "";

			int index_of_closing_braces = str.indexOf('}', index_open_braces);
			if(index_of_closing_braces == -1)
				return "";

			value = "@if";
			value += "@" + str.substring(current_index + 2, index_open_braces);
			value += "@" + str.substring(index_open_braces + 1, index_of_closing_braces) + "@";

			current_index = index_of_closing_braces;
			last_sequence_index = index_of_closing_braces;
		}

		return value;
	}

	private String while_value() {
		String value = "";

		// Ex: while expression {}
		// Pattern: <while><condition><value>

		char c = str.charAt(current_index);
		if(c == 'w') {
			int while_index = str.indexOf("while", current_index);
			if(while_index == -1)
				return "";
			if(current_index != while_index)
				return "";

			int index_open_braces = str.indexOf('{', current_index);
			if(index_open_braces == -1)
				return "";

			int index_of_closing_braces = str.indexOf('}', index_open_braces);
			if(index_of_closing_braces == -1)
				return "";

			value = "@while";
			value += "@" + str.substring(current_index + 5, index_open_braces);
			value += "@" + str.substring(index_open_braces + 1, index_of_closing_braces) + "@";

			current_index = index_of_closing_braces;
			last_sequence_index = index_of_closing_braces;
		}

		return value;
	}

	private String var_assign_value() {
		String value = "";
		// Ex: a = 10;
		// Pattern: <var_assignment><name><value>

		char c = str.charAt(current_index);
		if(c == '=') {
			int index_of_semicolon = str.indexOf(';', current_index);
			if(index_of_semicolon == -1)
				return "";

			value = "@var_assign";
			value += "@" + str.substring(last_sequence_index + 1, current_index);
			value += "@" + str.substring(current_index + 1, index_of_semicolon) + "@";

			current_index = index_of_semicolon;
			last_sequence_index = index_of_semicolon;
		}

		return value;
	}

	private String var_declare_2() {
		String value = "";

		// Ex: a : int = 10;
		// Pattern: <var_declare_2><name><type><value>
		char c = str.charAt(current_index);
		if(c == ':') {
			int index_of_equal = str.indexOf('=', current_index);
			if(index_of_equal == current_index + 1 || index_of_equal == -1)
				return "";

			// Checking if its not an ::enum
			if(Util.if_matches(str, current_index, "::enum") == true) {
				return "";
			}

			int index_of_semicolon = str.indexOf(';', index_of_equal);
			if(index_of_semicolon == -1)
				return "";

			value = "@var_declare_2";
			value += "@" + str.substring(last_sequence_index + 1, current_index);
			value += "@" + str.substring(current_index + 1, index_of_equal);
			value += "@" + str.substring(index_of_equal + 1, index_of_semicolon) + "@";

			current_index = index_of_semicolon;
			last_sequence_index = index_of_semicolon;
		}

		return value;
	}

	private String var_declare_1() {
		String value = "";

		// Ex: a := 10;
		// Pattern: <var_declare_1><name><type><value>
		char c = str.charAt(current_index);
		if(c == ':') {
			int index_of_equal_to = str.indexOf("=", current_index);
			if(index_of_equal_to == -1 || index_of_equal_to != current_index + 1)
				return "";

			int index_of_semicolon = str.indexOf(';', current_index + 2);
			if(index_of_semicolon == -1)
				return "";

			value += "@var_declare_1";
			value +=  "@" + str.substring(last_sequence_index + 1, current_index);
			value += "@not_known";
			value += "@" + str.substring(current_index + 2, index_of_semicolon) + "@";

			current_index = index_of_semicolon;
			last_sequence_index = index_of_semicolon;
		}

		return value;
	}

	private String struct_value() {
		String value = "";

		// Ex: Foo::struct { }
		// Value pattern: <struct><struct_name><struct_code>
		char c = str.charAt(current_index);
		if(c == ':') {
			int index_of_struct = str.indexOf("::struct", current_index - 1);
			if(index_of_struct == -1)
				return "";
			if(current_index != index_of_struct)
				return "";

			int index_of_open_bracket = str.indexOf('{', index_of_struct + 8);
			if(index_of_open_bracket == -1)
				return "";

			int index_of_closing_bracket = str.indexOf('}', index_of_open_bracket + 1);
			if(index_of_closing_bracket == -1)
				return "";

			value += "@struct";
			value += "@" + str.substring(last_sequence_index + 1, index_of_struct);
			value += "@" + str.substring(index_of_open_bracket + 1, index_of_closing_bracket) + "@";

			current_index = index_of_closing_bracket;
			last_sequence_index = index_of_closing_bracket;
		}

		return value;
	}

	private String func_values() {
		String value = "";

		// Ex: func main() -> int { }
		// Value pattern: <func><func_name><func_args><return_type><func_code>

		// @Incomplete: Add error checking for missing function name, return type, braces etc;
		char c = str.charAt(current_index);
		if(c == 'f') {
			int index_of_func = str.indexOf("func", current_index);
			if(index_of_func == -1)
				return "";
			if(current_index != index_of_func)
				return "";

			int index_of_open_paren = str.indexOf('(', current_index);
			if(index_of_open_paren == -1)
				return "";

			int index_of_closing_paren = str.indexOf(')', index_of_open_paren);
			if(index_of_closing_paren == -1)
				return "";

			int index_of_arrow = str.indexOf("->", index_of_closing_paren + 1);
			if(index_of_arrow == -1)
				return "";

			int index_of_open_bracket = str.indexOf('{', index_of_arrow);
			if (index_of_open_bracket == -1)
				return "";

			int index_of_closing_bracket = str.indexOf('}', index_of_open_bracket + 1);
			if(index_of_closing_bracket == -1)
				return "";

			value += "@func";
			value += "@" + str.substring(current_index + 4, index_of_open_paren);
			value += "@" + str.substring(index_of_open_paren + 1, index_of_closing_paren);
			value += "@" + str.substring(index_of_arrow + 2, index_of_open_bracket);
			value += "@" + str.substring(index_of_open_bracket + 1, index_of_closing_bracket) + "@";

			current_index = index_of_closing_bracket;
			last_sequence_index = index_of_closing_bracket;
		}

		return value;
	}

	List<String> get_broken_string() {
		List<String> li = new ArrayList<>();

		for(current_index = 0; current_index < str.length(); current_index++) {
			String func_value = func_values();
			String struct_value = struct_value();
			String var_declare_1_value = var_declare_1();
			String var_declare_2_value = var_declare_2();
			String var_assign_value = var_assign_value();
			String while_value = while_value();
			String if_value = if_value();
			String else_value = else_value();
			String else_if_value = else_if_value();
			String use_value = get_use_value();
			String enum_value = get_enum_value();

			if(!enum_value.equals(""))
				li.add(enum_value);
			else if(!use_value.equals(""))
				li.add(use_value);
			else if(!else_if_value.equals(""))
				li.add(else_if_value);
			else if(!else_value.equals(""))
				li.add(else_value);
			else if(!if_value.equals(""))
				li.add(if_value);
			else if(!while_value.equals(""))
				li.add(while_value);
			else if(!var_assign_value.equals(""))
				li.add(var_assign_value);
			else if(!var_declare_2_value.equals(""))
				li.add(var_declare_2_value);
			else if(!var_declare_1_value.equals(""))
				li.add(var_declare_1_value);
			else if(!func_value.equals(""))
				li.add(func_value);
			else if(!struct_value.equals(""))
				li.add(struct_value);
			else {
				NotknownRange nr = get_garbage_indices_and_update_current_index();
				if(nr.is_active)
					n_range.add(nr);
			}
		}

		// Inserting all the error characters sequences and their ranges.
		// @Note: The Errors will always be in the end.
		for(NotknownRange nr: n_range) {
			// Pattern: <Error><start_index><end_index><sequence>
			StringBuffer sb = new StringBuffer();
			sb.append("@Error");
			sb.append("@" + nr.start_index);
			sb.append("@" + (nr.end_index - 1));
			sb.append("@" + str.substring(nr.start_index, nr.end_index) + "@");

			li.add(sb.toString());
		}

		return li;
	}
}
