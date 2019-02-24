import java.util.List;
import java.util.ArrayList;

enum SequenceType {
	FUNC_NAME_ARGS,
	SEMICOLON,
	EXPRESSION,
	VAR_DECLARE_OR_DEFINE,
	OPEN_BRACKET,
	CLOSED_BRACKET,
	ENUM,
	IF,
	ELSE,
	STRUCT,
	WHILE,
	FUNC,
	RETURN,
	NOT_KNOWN
}

public class SequenceInfo {
	SequenceType seq_type;
	String str;
	boolean visited;

	SequenceInfo(SequenceType seq_type, String str) {
		this.seq_type = seq_type;
		this.str = str;
		visited = false;
	}

	List<String> split_str() {
		List<String> li;

		switch(seq_type) {
			case FUNC_NAME_ARGS:
				li = split_func_name_args();
				break;
			case VAR_DECLARE_OR_DEFINE:
				li = split_var_declare_or_define();
			default:
				li = new ArrayList<>();
		}

		return li;
	}

	List<String> split_var_declare_or_define() {
		List<String> li = new ArrayList<>();

		return li;
	}

	List<String> split_func_name_args() {
		List<String> li = new ArrayList<>();

		int index_of_open_paren = str.indexOf('(');
		int index_of_close_paren = str.indexOf(')');

		String func_name = str.substring(0, index_of_open_paren);
		li.add(func_name);

		if(index_of_close_paren == index_of_open_paren + 1)
			return li;

		String args_str = str.substring(index_of_open_paren + 1, index_of_close_paren);

		String[] split_str = args_str.split(",");
		for(String s: split_str) {
			int index_of_colon = s.indexOf(':');
			String name = s.substring(0, index_of_colon);
			String type = s.substring(index_of_colon + 1);
			
			li.add(name);
			li.add(type);
		}

		int index_of_arrow = str.indexOf("->");
		String ret_type = str.substring(index_of_arrow + 2);
		li.add(ret_type);

		return li;
	}

	// "none": no errors , "ErrorMsg": errors
	String validate_syntax(List<RangeIndices> ri) {
		// Note: Only declaration/definition, expressions, func_name_args will be checked for
		switch(seq_type) {
			case FUNC_NAME_ARGS:
				return validate_func_declaration_syntax();
			case VAR_DECLARE_OR_DEFINE:
				return validate_var_decl_def(ri);
		}

		return "none";
	}

	private String validate_var_decl_def(List<RangeIndices> ri) {
		String var_name, var_type = "", exp_value = "";

		int index_of_equal_colon = str.indexOf(":=");	
		if(index_of_equal_colon != -1) {
			var_name = str.substring(0, index_of_equal_colon);
			exp_value = str.substring(index_of_equal_colon + 2);	
		}
		else {
			int index_of_equal = str.indexOf("=");
			int index_of_colon = str.indexOf(":");

			if(index_of_equal > index_of_colon) {
				return "In declaring variables ':' has to come before '='.";
			}

			if(index_of_equal == -1) {
				var_name = str.substring(0, index_of_colon);
				var_type = str.substring(index_of_colon + 1);
			}
			else if(index_of_colon == -1) {
				var_name = str.substring(0, index_of_equal);
				exp_value = str.substring(index_of_equal + 1);
			}
			else {
				var_name = str.substring(0, index_of_colon);
				var_type = str.substring(index_of_colon + 1, index_of_equal);
				exp_value = str.substring(index_of_equal + 1);
			}
		}

		if(!Util.is_valid_name(var_name))
			return "Variable name '" + var_name + "' is not valid.";
		if(!var_type.equals("") && !Util.is_valid_name(var_type))
			return "Identifier '" + var_type + "' is not found.";
		
		String exp_msg = Util.is_valid_exp(exp_value, ri);

		return "none";
	}

	private String validate_func_declaration_syntax() {
		// No quotes	
		int index_of_quotes = str.indexOf("\"");
		if(index_of_quotes != -1)
			return "Invalid character '\"' found at index: " + index_of_quotes;
		
		// Only one ( and )
		int num_open_paren = Util.get_num_chars(str, '(');
		if(num_open_paren != 1)
			return "Number of '(' should be equal to one";

		int num_close_paren = Util.get_num_chars(str, ')');
		if(num_close_paren != 1)
			return "Number of ')' should be equal to one";

		String func_name = str.substring(0, str.indexOf('('));
		if(!Util.is_valid_name(func_name))
			return "Function name: " + func_name + " is not valid.";

		int index_of_open_paren = str.indexOf('(');
		int index_of_close_paren = str.indexOf(')');
		if(index_of_close_paren == index_of_open_paren + 1)
			return "none";

		String in_str = str.substring(index_of_open_paren + 1, index_of_close_paren);
		List<String> split_str = Util.my_split(in_str, ',');

		for(int i = 0; i < split_str.size(); ++i) {
			String s = split_str.get(i);
			if(s.equals("")) {
				return "Function '" + func_name + "' missing 'arg_name' and 'type' at argument number " + (i + 1) + ".";
			}
			int num_colons = Util.get_num_chars(s, ':');
			if(num_colons == 0)
				return "Function '" + func_name + "' is missing 'argument type' for argument number: " + i;
			else if(num_colons > 1)
				return "Function '" + func_name + "' should have only one ':' b/w name and argument. But contains " + num_colons + " ':' .";

			int index_of_colon = s.indexOf(':');
			String name = s.substring(0, index_of_colon);
			if(!Util.is_valid_name(name)) {
				return "In function '" + func_name + "' argument name '" + name + "' is not a valid name";
			}

			String arg_type = s.substring(index_of_colon + 1);
			if(arg_type.equals(""))
				return "In function '" + func_name + "' argument type is missing for argument '" + name + "' at argument number " + (i + 1) + ".";
		}

		int index_of_arrow = str.indexOf("->");
		if(index_of_arrow == -1)
			return "Function '" + func_name + "' does not have a '->'";

		String return_type = str.substring(index_of_arrow + 2);
		if(return_type.equals(""))
			return "Function '" + func_name + "' is missing a return type after '->'.";

		return "none";
	}
}

class SequenceTypeInfo {
	public static List<SequenceType> get_sequence_types(List<String> tokens, List<RangeIndices> quote_range_indices) {
		List<SequenceType> li = new ArrayList<>();

		for(String s: tokens) {
			SequenceType type = SequenceType.NOT_KNOWN;

			if(s.equals("func"))
				type = SequenceType.FUNC;
			else if(s.equals("while"))
				type = SequenceType.WHILE;
			else if(s.equals("if"))
				type = SequenceType.IF;
			else if(s.equals("else"))
				type = SequenceType.ELSE;
			else if(s.equals(";"))
				type = SequenceType.SEMICOLON;
			else if(s.equals("::enum"))
				type = SequenceType.ENUM;
			else if(s.equals("::struct"))
				type = SequenceType.STRUCT;
			else if(s.equals("{"))
				type = SequenceType.OPEN_BRACKET;
			else if(s.equals("}"))
				type = SequenceType.CLOSED_BRACKET;
			else if(s.equals("return"))
				type = SequenceType.RETURN;
			else if(is_func_name_args(s))
				type = SequenceType.FUNC_NAME_ARGS;
			else if(if_var_declaration_or_def(s, quote_range_indices)) 
				type = SequenceType.VAR_DECLARE_OR_DEFINE;
			else // Might be an expression ie. just type, variable_name, long expression
				type = SequenceType.EXPRESSION;

			li.add(type);
		}

		return li;
	}

	// @Tmp:
	public static String get_in_str(SequenceType type) {
		String type_str = "";

		if(type == SequenceType.FUNC)
			type_str = "FUNC";
		else if(type == SequenceType.NOT_KNOWN)
			type_str = "NOT_KNOWN";
		else if(type == SequenceType.WHILE)
			type_str = "WHILE";
		else if(type == SequenceType.IF)
			type_str = "IF";
		else if(type == SequenceType.ELSE)
			type_str = "ELSE";
		else if(type == SequenceType.OPEN_BRACKET)
			type_str = "OPEN_BRACKET";
		else if(type == SequenceType.CLOSED_BRACKET)
			type_str = "CLOSED_BRACKET";
		else if(type == SequenceType.SEMICOLON)
			type_str = "SEMICOLON";
		else if(type == SequenceType.STRUCT)
			type_str = "STRUCT";
		else if(type == SequenceType.ENUM)
			type_str = "ENUM";
		else if(type == SequenceType.VAR_DECLARE_OR_DEFINE)
			type_str = "VAR_DECLARE_OR_DEFINE";
		else if(type == SequenceType.FUNC_NAME_ARGS)
			type_str = "FUNC_NAME_ARGS";
		else if(type == SequenceType.EXPRESSION)
			type_str = "EXPRESSION";
		else if(type == SequenceType.RETURN)
			type_str = "RETURN";

		return type_str;
	}

	private static boolean is_func_name_args(String s) {
		if(s.indexOf('\"') != -1)
			return false;

		// 3 because name should at min be length 1, and i for ( and 1 for ).
		if(s.length() < 3)
			return false;	

		if(!Util.is_char_alpha_digit_underscore(s.charAt(0)))
			return false;

		int index_of_arrow = s.indexOf("->");
		if(index_of_arrow != -1)
			return true;

		return false;
	}

	private static boolean if_var_declaration_or_def(String s, List<RangeIndices> quote_range_indices) {

		if(s.length() < 3)
			return false;

		int num_equals = Util.get_num_chars_outside_quotes(s, '=', quote_range_indices);
		if(num_equals > 1)
			return false;

		int num_colons = Util.get_num_chars_outside_quotes(s, ':', quote_range_indices);
		if(num_colons != 1)
			return false;

		return true;
	}
}
