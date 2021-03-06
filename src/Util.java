import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java.util.HashMap;

class StringAndIndex {
	int index = -1;
	String str = "";
}

class RangeIndices {
	int from_index = -1;
	int to_index = -1;
}

class IndexLen {
	public int index = -1;
	public int len = -1;
}

class Names_NA_Indices {
	List<String> names;
	List<RangeIndices> range_indices;
}

class Str_NA_Indices {
	String str;
	List<RangeIndices> range_indices;
}

public class Util {
	static List<String> operators = new ArrayList<>();
	static List<String> keywords = new ArrayList<>(); 
	static List<String> split_sequences = new ArrayList<>();
	static List<RangeIndices> quotes_range_indices = new ArrayList<>();

	static void init_ops() {
		operators.clear();
		operators.add("+"); operators.add("-"); operators.add("*"); operators.add("/"); operators.add("%");
		operators.add("=="); operators.add("!="); operators.add(">"); operators.add(">="); operators.add("<"); operators.add("<=");
		operators.add("&&"); operators.add("||"); operators.add("!");
		operators.add("&"); operators.add("|"); operators.add("^^"); operators.add(">>>"); operators.add("<<<");
		operators.add("("); operators.add(")"); operators.add("["); operators.add("]");
		operators.add("<<"); operators.add(">>");
		operators.add(":=");
	}

	static void init_split_sequences() {
		keywords.add("func"); keywords.add("::struct");
		keywords.add("::enum"); keywords.add("if");
		keywords.add("else"); keywords.add("while");
		keywords.add("return"); keywords.add("use");

		split_sequences.add(";"); split_sequences.add("{");
		split_sequences.add("}");
		for(int i = 0; i < keywords.size(); ++i)
			split_sequences.add(keywords.get(i));
	}

	static List<String> split_with_ops_the_types(String s) {
		List<String> exps = split_with_ops(s);

		for(int i = 0; i < exps.size(); ++i) {
			String str = exps.get(i);
			int ops_count = 0;
			if(str.equals("*")) {
				int j = i + 1;
				while(j < exps.size() && exps.get(j).equals("*")) {
					ops_count += 1;
					j += 1;
				}

				if((i - 1) < 0)
					continue;

				String new_str = exps.get(i - 1);
				int end_index = j - 1;

				if(ops_count == 0) {
					new_str += "*";
					exps.remove((int)(i));
				}
				else {
					for(int x = 0; x < ops_count + 1; ++x) {
						new_str += "*";
						exps.remove((int)(i));
					}
				}

				exps.set(i - 1, new_str);
				i += ops_count;
			}
		}

		return exps;
	}

	static List<String> split_with_ops(String str) {
		List<IndexLen> op_pos = get_op_pos_for_exp(str);
		List<String> in_list = new ArrayList<>();

		if(str.equals(""))
			return in_list;

		Integer s = op_pos.size();
		if(s == 0) {
			in_list.add(str);
			return in_list;
		}

		// adding the sub string before the first operator.
		IndexLen start_index_len = op_pos.get(0);
		String first_str = str.substring(0, start_index_len.index);
		if(!first_str.equals(""))
			in_list.add(first_str);

		for(int i = 0; i < s - 1; ++i) {
			IndexLen index_len_1 = op_pos.get(i);
			IndexLen index_len_2 = op_pos.get(i + 1);

			int begin_index = index_len_1.index + index_len_1.len;
			int end_index = index_len_2.index;

			String tmp_op = str.substring(index_len_1.index, index_len_1.index + index_len_1.len);
			if(!tmp_op.equals(""))
				in_list.add(tmp_op);

			String var_str = str.substring(begin_index, end_index);
			if(!var_str.equals(""))
				in_list.add(var_str);
		}

		IndexLen last_index_len = op_pos.get(s - 1);
		String tmp_op = str.substring(last_index_len.index, last_index_len.index + last_index_len.len);
		if(!tmp_op.equals(""))
			in_list.add(tmp_op);

		String last_str = str.substring(last_index_len.index + last_index_len.len);
		if(!last_str.equals(""))
			in_list.add(last_str);

		return in_list;
	}

	static List<IndexLen> get_op_pos_for_exp(String str) {
		List<IndexLen> index_len = new ArrayList<>();

		/* Note: The operator inside function calls, and array element calls should be neglected. */

		// The operator can be more than one character.
		for(int i = 0; i < str.length(); ++i) {
			String len_1_str, len_2_str = "", len_3_str = "";

			len_1_str = str.substring(i, i + 1);
			if((i + 1) < str.length())
				len_2_str = str.substring(i, i + 2);
			if((i + 2) < str.length())
				len_3_str = str.substring(i, i + 3);

			int len_3_index = str.indexOf(len_3_str, i);
			int len_2_index = str.indexOf(len_2_str, i);
			int len_1_index = str.indexOf(len_1_str, i);

			IndexLen tmp_index_len = new IndexLen();
			if(is_operator(len_3_str) && len_3_index != -1) {
				tmp_index_len.index = i;
				tmp_index_len.len = 3;
				index_len.add(tmp_index_len);
				i += 2;
			}
			else if(is_operator(len_2_str) && len_2_index != -1) {
				tmp_index_len.index = i;
				tmp_index_len.len = 2;
				index_len.add(tmp_index_len);
				i += 1;
			}
			else if(is_operator(len_1_str) && len_1_index != -1) {
				boolean should_include_op = true;
				if(len_1_str.equals("(") || len_1_str.equals("[")) {
					if(i > 0) {
						char prev_char = str.charAt(i - 1);
						int close_bracket_index = get_matching_close_bracket_index(str, len_1_str, i);
						if(is_char_alpha_digit_underscore(prev_char)) {
							i = close_bracket_index; // No Need to Inc because of ++i
							should_include_op = false;
						}
					}
				}
				if(should_include_op) {
					tmp_index_len.index = i;
					tmp_index_len.len = 1;
					index_len.add(tmp_index_len);
				}
			}
		}

		return index_len;
	}

	static int get_matching_close_bracket_index(String s, String c, int from_index) {
		int open_braces_count = 0;
		int close_braces_count = 0;
		int quotes_count = 0;
		int len = s.length();
		char closing_char = ' ';
		char opening_char = c.charAt(0);

		if(opening_char == '(')
			closing_char = ')';
		else if(opening_char == '[')
			closing_char = ']';

		for(int i = from_index; i < len; ++i) {
			char ch = s.charAt(i);
			if(ch == '\"') {
				if(i == 0 || (s.charAt(i - 1) != '\\')) {
					quotes_count += 1;
				}
			}
			if(quotes_count % 2 != 0)
				continue;

			if(ch == opening_char)
				open_braces_count += 1;
			else if(ch == closing_char)
				close_braces_count += 1;

			if(open_braces_count == close_braces_count)
				return i;
		}

		return -1;
	}

	static boolean is_operator(String op) {
		boolean is_op = operators.contains(op);
		return is_op;
	}

	static boolean is_only_unary_operator(String op) {
		if(op.equals(">>") || op.equals("<<") || op.equals("!"))
			return true;

		return false;
	}

	// @Redundant: Can be made into a single method
	static String eat_spaces(String str) {
		String tmp = "";

		int quotes_count = 0;
		for(int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if(c == '\"') {
				if(i == 0 || (str.charAt(i - 1) != '\\')) {
					quotes_count += 1;
				}
			}

			if(quotes_count % 2 != 0)
				tmp += c;
			else if(c != '\t' && c != ' ' && c != '\n')
				tmp += c;
		}

		return tmp;
	}

	// @Redundant: Can be made into a single method
	static String eat_only_spaces(String str) {
		String tmp = "";

		int quotes_count = 0;
		for(int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if(c == '\"') {
				if(i == 0 || (str.charAt(i - 1) != '\\')) {
					quotes_count += 1;
				}
			}

			if(quotes_count % 2 != 0)
				tmp += c;
			else if(c != '\t' && c != ' ')
				tmp += c;
		}

		return tmp;
	}

	static int get_num_chars(String str, char ch) {
		int num = 0;

		for(int i = 0; i < str.length(); ++i)
			if(str.charAt(i) == ch)
				num += 1;

		return num;
	}

	static int get_num_chars_outside_quotes(String str, char ch, List<RangeIndices> ri) {
		int count = 0;
		int quotes_count = 0;

		for(int i = 0; i < str.length(); ++i) {
			char c = str.charAt(i);

			if(c == '\"') {
				if(i == 0 || (str.charAt(i - 1) != '\\')) {
					quotes_count += 1;
				}
			}
			else if(quotes_count % 2 == 0 && c == ch)
				count += 1;
		}

		return count;
	}

	static boolean is_index_inside_quotes(int index, List<RangeIndices> li) {
		if(index < 0)
			return false;

		for(RangeIndices ri: li) {
			if(index >= ri.from_index && index <= ri.to_index)
				return true;
		}

		return false;
	}

	static boolean is_char_alpha_digit_underscore(char c) {
		if((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_')
			return true;

		return false;
	}

	static String get_primitive_type(String str) {
		// is_int
		boolean is_int = true;
		for(int i = 0; i < str.length(); ++i) {
			char c = str.charAt(i);
			if(c < '0' || c > '9') {
				is_int = false;
				break;
			}
		}
		if(is_int)
			return "int";

		// is_double
		boolean is_double = true;
		int dot_count = 0;
		int after_dot_count = 0;
		for(int i = 0; i < str.length(); ++i) {
			char c = str.charAt(i);
			if(c == '.')
				dot_count += 1;
			if(dot_count > 1) {
				is_double = false;
				break;
			}
			else if((c < '0' || c > '9') && c != '.') {
				is_double = false;
				break;
			}
			else if(dot_count == 1 && c >= '0' && c <= '9')
				after_dot_count += 1;
		}
		if(after_dot_count == 0)
			is_double = false;
		if(is_double)
			return "double";

		// is_bool
		if(str.equals("true") || str.equals("false"))
			return "bool";

		// is char
		boolean is_char = true;
		if(str.length() > 4 || str.length() <= 2)
			is_char = false;
		else {
			if(str.indexOf("'") == -1 || str.lastIndexOf("'") == -1)
				is_char = false;
		}

		if(is_char)
			return "char";

		// string(char*)
		boolean is_string = true;
		if(str.indexOf('\"') == -1 || str.lastIndexOf('\"') == -1)
			is_string = false;
		if(is_string)
			return "string";

		return "not_known";
	}

	static String get_primitive_literal(String primitive_type) {
		String literal = "not_known";
		if(primitive_type.equals("int"))
			literal = "1";
		else if(primitive_type.equals("double"))
			literal = "1.0";

		// @Note: If pointer then return the type.
		else if(primitive_type.charAt(primitive_type.length() - 1) == '*') {
			literal = primitive_type;
		}

		// @Incomplete ....
		// @Incomplete ....
		// @Incomplete ....

		return literal;
	}

	static List<RangeIndices> get_range_indices_of_quotes(String str) {
		List<RangeIndices> li = new ArrayList<>();

		int len = str.length();
		int quotes_count = 0;
		int start_index = -1;
		boolean is_eligible = false;

		for(int i = 0; i < len; ++i) {
			is_eligible = false;
			char c = str.charAt(i);
			if(c == '\"') {
				if(i == 0 || (str.charAt(i - 1) != '\\')) {
					is_eligible = true;
					quotes_count += 1;
				}
			}

			if(is_eligible) {
				if(quotes_count % 2 != 0)
					start_index = i;
				else {
					RangeIndices ri = new RangeIndices();
					ri.from_index = start_index;
					ri.to_index = i;

					li.add(ri);
				}
			}
		}

		return li;
	}

	static boolean is_valid_name(String str) {
		int len = str.length();
		if(len == 0)
			return false;

		// First character can be either _ or alphabet
		char first_char = str.charAt(0);
		if(first_char == '_' || (first_char >= 'a' && first_char <= 'z') || (first_char >= 'A' && first_char <= 'Z')) {

			for(int i = 1; i < len; ++i) {
				char ch = str.charAt(i);
				if(ch == '_' || (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z' || (ch >= '0' && ch <= '9'))) {
					// do_nothing
				}
				else
					return false;
			}
		}
		else
			return false;

		return true;
	}

	static boolean is_valid_type_name(String type) {
		if(type.equals(""))
			return false;

		int len = type.length();

		char first_char = type.charAt(0);
		if(first_char == '_' || (first_char >= 'a' && first_char <= 'z') || (first_char >= 'A' && first_char <= 'Z')) {} // do nothing
		else
			return false;

		if(first_char == '_') {
			if(type.length() == 1)
				return false;

			char second_char = type.charAt(1);
			if(second_char == '*')
				return false;
		}

		// Normal single variable type.
		boolean is_valid_var_name = true;
		for(int i = 1; i < len; ++i) {
			char c = type.charAt(i);
			if(!is_char_alpha_digit_underscore(c)) {
				for(int j = i; j < len; ++j) {
					char c2 = type.charAt(j);
					if(c2 != '*') {
						is_valid_var_name = false;
						break;
					}
				}
			}
		}


		if(is_valid_var_name)
			return true;

		// Checking if its an array.
		// @Note: It can also be an array of pointers...
		int i = 1;
		if(i < len) {
			char c = type.charAt(i);
			if((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {} // do nothing
			else
				return false;
		}

		boolean array_square_encountered = false;
		int num_digits = 0;
		int num_stars = 0;
		for(i = 2; i < len; ++i) {
			char c = type.charAt(i);
			if(!Util.is_char_alpha_digit_underscore(c)) {
				int num_close_square = 0;
				char open_square_char = ' ';

				open_square_char = type.charAt(i);
				if(open_square_char != '[')
					return false;

				int j = i + 1;
				if(j >= len)
					return false;

				while(j < type.length()) {
					char c2 = type.charAt(j);
					if(c2 < '0' || c2 > '9') {
						if(c2 == ']') {
							num_close_square += 1;
							array_square_encountered = true;
							i = j++;
							break;
						}
						else
							return false;
					}
					else
						num_digits += 1;

					j += 1;
				}

				if(num_close_square != 1)
					return false;

				// Checking for pointers '*' s.
				for(; j < type.length(); ++j) {
					char c3 = type.charAt(j);
					if(c3 != '*')
						return false;
					else
						num_stars += 1;
				}

				if(num_stars > 1 && num_digits == 0)
					return false;

				i = j;
			}
			else if(array_square_encountered)
				return false;
		}

		return true;
	}

	static boolean is_typename_array(String typename) {
		if(typename.indexOf('[') != -1 && typename.indexOf(']') != -1)
			return true;

		return false;
	}

	static List<String> split_array(String str) {
		List<String> li = new ArrayList<>();
		int index_of_prev_ch = 0;

		int quotes_count = 0;
		int open_braces_count = 0;
		int close_braces_count = 0;
		for(int i = 0; i < str.length(); ++i) {
			char c = str.charAt(i);
			if(c == '\"')
				quotes_count++;
			else if(c == '(')
				open_braces_count += 1;
			else if(c == ')')
				close_braces_count += 1;

			else if(quotes_count % 2 == 0 && open_braces_count == close_braces_count && c == ',') {
				String s = str.substring(index_of_prev_ch, i);
				li.add(s);

				index_of_prev_ch = i + 1;
			}
		}

		String s = str.substring(index_of_prev_ch);
		if(!s.equals(""))
			li.add(s);

		return li;

	}

	static String get_array_typename(String typename) {
		StringBuffer sb = new StringBuffer(typename);
		sb = sb.delete(sb.indexOf("["), sb.indexOf("]") + 1);

		return sb.toString();
	}

	static boolean is_valid_primitive_type(String type, List<String> all_types) {
		// Should also count for pointers ie. *

		for(int i = 0; i < all_types.size(); ++i) {
			String current_type = all_types.get(i);
			int index_of_type = type.indexOf(current_type);

			if (index_of_type != -1) {
				for(int j = current_type.length() + 1; j < type.length(); ++j) {
					char c = type.charAt(j);
					if(c != '*')
						return false;
				}
				return true;
			}
		}

		return false;
	}

	static StringAndIndex get_first_index_and_str(String str, int from_index, List<String> li) {
		StringAndIndex si = new StringAndIndex();
		final int max_index = 100000;
		int str_index = max_index;
		int li_index = -1;

		for(int i = 0; i < li.size(); ++i) {
			String s = li.get(i);
			int index = str.indexOf(s, from_index);
			if(index != -1 && index < str_index) {
				str_index = index;
				li_index = i;
			}
		}

		if(str_index == max_index) {
			si.index = -1;
			si.str = "";
		}
		else {
			si.index = str_index;
			si.str = li.get(li_index);
		}

		return si;
	}

	static boolean is_all_caps(String str) {
		for(int i = 0; i < str.length(); ++i) {
			char c = str.charAt(i);
			if(c < 'A' || c > 'Z')
				return false;
		}

		return true;
	}

	static List<String> my_split(String str, char ch) {
		List<String> li = new ArrayList<>();
		int index_of_prev_ch = 0;

		int quotes_count = 0;
		for(int i = 0; i < str.length(); ++i) {
			char c = str.charAt(i);
			if(c == '\"')
				quotes_count++;

			else if(quotes_count % 2 == 0 && c == ch) {
				String s = str.substring(index_of_prev_ch, i);
				li.add(s);

				index_of_prev_ch = i + 1;
			}
		}

		String s = str.substring(index_of_prev_ch);
		if(!s.equals(""))
			li.add(s);

		return li;
	}

	static int get_num_quotes(String str) {
		int quotes_count = 0;
		for(int i = 0; i < str.length(); ++i) {
			char c = str.charAt(i);
			if(c == '\"') {
				if(i == 0 || (str.charAt(i - 1) != '\\')) {
					quotes_count += 1;
				}
			}
		}

		return quotes_count;
	}

	static String is_valid_exp(String s, int from_index) {
		if(get_num_quotes(s) % 2 != 0)
			return "Number of open quotes not matching with the number of closing quotes in the expression.";

		int quotes_count = 0;
		int open_paren_count = 0;
		int close_paren_count = 0;
		int open_square_count = 0;
		int close_square_count = 0;
		for(int i = 0; i < s.length(); ++i) {
			char c = s.charAt(i);
			if(c == '\"') {
				if(i == 0 || (s.charAt(i - 1) != '\\')) {
					quotes_count += 1;
				}
			}
			if(quotes_count % 2 != 0)
				continue;

			if(c == '(')
				open_paren_count += 1;
			else if(c == ')')
				close_paren_count += 1;
			else if(c == '[')
				open_square_count += 1;
			else if(c == ']')
				close_square_count += 1;
		}

		if(open_paren_count != close_paren_count)
			return "Number of '(' not matching number of ')' in the expression.";
		if(open_square_count != close_square_count)
			return "Number of '[' not matching number of ']' in the expression.";

		// @Incomplete: Anything else....
		// @Incomplete: Anything else....
		// @Incomplete: Anything else....
		// @Incomplete: Anything else....

		return "none";
	}

	static boolean is_char_alpha_underscore(char c) {
		if(c == '_' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))
			return true;

		return false;
	}

	static boolean contains_func_call(String s) {
		if(s.length() < 3)
			return false;

		int quotes_count = 0;
		int len = s.length();
		for(int i = 0; i < len; ++i) {
			char c = s.charAt(i);
			if(c == '\"') {
				if(i == 0 || (s.charAt(i - 1) != '\\')) {
					quotes_count += 1;
				}
			}

			if(quotes_count % 2 != 0)
				continue;

			if(is_char_alpha_digit_underscore(c)) {
				if((i + 1) < len && s.charAt(i + 1) == '(') {
					int close_index = get_matching_close_bracket_index(s, ")", i + 1);
					if(close_index != -1)
						return true;
				}
			}
		}

		return false;
	}

	static boolean if_func_call(String s) {
		if(s.length() < 3)
			return false;

		char c1 = s.charAt(0);
		if(!is_char_alpha_underscore(c1))
			return false;

		int i = 1;
		int len = s.length();
		boolean encountered_open_paren = false;
		for(; i < len; ++i) {
			char c = s.charAt(i);
			if(c == '(') {
				encountered_open_paren = true;
				break;
			}
		}

		char last_char = s.charAt(len - 1);
		if(last_char == ')')
			return true;

		return false;
	}

	// @Note: 's' should not include the outer function's name ...
	static List<String> get_all_func_calls(String s) {
		List<String> li = new ArrayList<>();
		int len = s.length();
		int func_len = 0;
		for(int i = 0; i < len; ++i) {
			char c = s.charAt(i);
			if(is_char_alpha_digit_underscore(c))
				func_len += 1;
			else if(c == '(') {
				int close_paren_index = get_matching_close_bracket_index(s, "(", i);
				String func = s.substring(i - func_len, close_paren_index + 1);
				// Checking if it's not just (....) and the func has a name.
				if(func.charAt(0) != '(')
					li.add(func);

				func_len = 0;
			}
			else {
				func_len = 0;
			}
		}

		return li;
	}

	static List<String> get_func_args(String s) {
		int num_args = 1;
		int len = s.length();
		int index_of_first_open_paren = s.indexOf('(');
		int num_open_paren = 0;
		int num_close_paren = 0;
		int last_index = index_of_first_open_paren + 1;
		int quotes_count = 0;
		List<String> args = new ArrayList<>();

		for(int i = index_of_first_open_paren + 1; i < len; ++i) {
			char c = s.charAt(i);
			if(c == '\"') {
				if(i == 0 || (s.charAt(i - 1) != '\\')) {
					quotes_count += 1;
				}
			}
			else if(c == '(')
				num_open_paren += 1;
			else if(c == ')')
				num_close_paren += 1;
			else if(quotes_count % 2 == 0 && num_open_paren == num_close_paren && c == ',') {
				num_args += 1;
				String arg = s.substring(last_index, i);
				if(!args.equals(""))
					args.add(arg);

				last_index = i + 1;
			}
		}

		String last_arg = s.substring(last_index, len - 1);
		if(!last_arg.equals(""))
			args.add(last_arg);

		return args;
	}

	static String add_types(String type_1, String op) {
		String res = "not_known";

		if(op.equals(">>"))
			res = type_1.concat("*");
		else if(op.equals("<<") && type_1.endsWith("*"))
			res = type_1.substring(0, type_1.length() - 1);
		else if(op.equals("!") && type_1.equals("bool"))
			res = "bool";
		else if(op.equals("+") || op.equals("-")) {
			if(type_1.equals("int") || type_1.equals("double"))
				res = type_1;
		}

		return res;
	}

	static String add_types(String type_1, String type_2, String op) {
		String res = "not_known";

		// @Incomplete Maybe.
		boolean is_op_arith = op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/");
		boolean is_op_rela = op.equals(">") || op.equals("<") || op.equals("<=") || op.equals(">=");
		boolean is_op_bitwise = op.equals("&") || op.equals("|") || op.equals("^") || op.equals(">>>")
			|| op.equals("<<<");
		boolean is_op_logical = op.equals("&&") || op.equals("||");

		if((type_1.equals("int") && type_2.equals("double")) || (type_1.equals("double") && type_2.equals("int"))) {
			if(is_op_arith)
				res = "double";
			else if(is_op_rela)
				res = "bool";
		}
		else if(type_1.equals("int") && type_2.equals("int")) {
			if(is_op_arith || is_op_bitwise || op.equals("%"))
				res = "int";
			else if(is_op_rela || op.equals("==") || op.equals("!="))
				res = "bool";
		}
		else if(type_1.equals("double") && type_2.equals("double")) {
			if(is_op_arith)
				res = "double";
			else if(is_op_rela || op.equals("==") || op.equals("!="))
				res = "bool";
		}
		else if(type_1.equals("bool") && type_2.equals("bool")) {
			if(is_op_logical || op.equals("==") || op.equals("!="))
				res = "bool";
		}
		else if(type_1.equals("string") && type_2.equals("string")) {
			if(op.equals("+"))
				res = "string";
		}
		else if(type_1.equals("char") && type_2.equals("char")) {
			if(op.equals("==") || op.equals("!="))
				res = "bool";
		}
		else { // pointer arithmetic.
			if(is_type_pointer(type_1) && type_2.equals("int"))
				res = type_1;
			else if(is_type_pointer(type_2) && type_1.equals("int"))
				res = type_2;
		}

		return res;
	}

	static boolean validate_operation(String type_1, String op) {
		boolean is_valid = false;

		if(op.equals("+") || op.equals("-")) {
			if(type_1.equals("int") || type_1.equals("double"))
				is_valid = true;
		}
		else if(op.equals("!") && type_1.equals("bool"))
			is_valid = true;
		else if(type_1.endsWith("*") && op.equals("<<"))
			is_valid = true;
		else if(op.equals(">>"))
			is_valid = true;

		// @Incomplete.
		// @Incomplete.
		// @Incomplete.

		return is_valid;
	}

	static boolean validate_operation(String type_1, String type_2, String op) {
		boolean is_valid = false;

		if(type_1.equals("string") && type_2.equals("string")) {
			if(op.equals("+"))
				is_valid = true;
		}
		if(type_1.equals("char") && type_2.equals("char")) {
			if(op.equals("==") || op.equals("!="))
				is_valid = true;
		}
		else if(type_1.equals("bool") && type_2.equals("bool")) {
			if(op.equals("==") || op.equals("!=") || op.equals("&&") || op.equals("||"))
				is_valid = true;
		}
		else if((type_1.equals("int") && type_2.equals("double")) || (type_1.equals("double") && type_2.equals("int"))) {
			if(op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/")
					|| op.equals(">") || op.equals("<") || op.equals(">=") || op.equals("<="))
				is_valid = true;
		}
		else if(type_1.equals("int") && type_2.equals("int")) {
			if(op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/") ||
					op.equals("%") || op.equals(">") || op.equals("<") || op.equals(">=") || op.equals("<=")
					|| op.equals("&") || op.equals("|") || op.equals("^") || op.equals(">>>") || op.equals("<<<") || op.equals("==") || op.equals("!="))
				is_valid = true;
		}
		else if(type_1.equals("double") && type_2.equals("double")) {
			if(op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/") ||
					op.equals("!=") || op.equals(">") || op.equals("<") || op.equals("<=")
					|| op.equals(">=") || op.equals("=="))
				is_valid = true;
		}
		else { // Pointer arithmetic
			if(!(op.equals("+") || op.equals("-")))
				return false;
			if(is_type_pointer(type_1) && is_type_pointer(type_2))
				return false;

			if(is_type_pointer(type_1)) {
				String typename = type_1.substring(0, type_1.indexOf("*"));
				if(type_2.equals("int"))
					is_valid = true;
			}
			else if(is_type_pointer(type_2)) {
				String typename = type_2.substring(0, type_2.indexOf("*"));
				if(type_1.equals("int"))
					is_valid = true;
					is_valid = true;
			}
		}

		return is_valid;
	}

	static boolean is_type_pointer(String type) {
		int len = type.length();
		for(int i = len - 1; i >= 0; --i) {
			char c = type.charAt(i);
			if(c == '*')
				return true;
		}

		return false;
	}

	static Names_NA_Indices get_only_names_and_literals(String s, List<String> func_names) {
		StringBuilder sb = new StringBuilder(s);
		Names_NA_Indices names_na_indices = new Names_NA_Indices();

		// Finding not allowed_indices.
		List<RangeIndices> range_indices = new ArrayList<>();

		for(String func_name: func_names) {
			int index = sb.indexOf(func_name, 0);
			while(index != -1) {
				int begin = index;
				int end = index + func_name.length() - 1;

				RangeIndices ra = new RangeIndices();
				ra.from_index = begin;
				ra.to_index = end;
				range_indices.add(ra);

				index = sb.indexOf(func_name, end + 1);

				// System.out.println("(" + begin + ", " + end + ")");
			}
		}

		for(String func_name: func_names) {
			int index = sb.indexOf(func_name);
			while(index != -1) {
				int begin = index;
				int end = index + func_name.length();
				int close_paren_index = get_matching_close_bracket_index(sb.toString(), "(", end);

				sb.setCharAt(end, '@');
				sb.setCharAt(close_paren_index, '@');

				sb = sb.delete(begin, end);
				index = sb.indexOf(func_name);
			}
		}

		int sb_len = sb.length();
		StringBuffer sb_tmp = new StringBuffer("");
		List<String> all_exps = new ArrayList<>();
		Stack<Character> stack = new Stack<>();
		int quotes_count = 0;

		for(int i = 0; i < sb_len; ++i) {
			char c = sb.charAt(i);
			if(c == '\"') {
				if(i == 0 || (sb.charAt(i - 1) != '\\')) {
					quotes_count += 1;
				}
			}
			if(c == '@' || (c == ',' && quotes_count % 2 == 0)) {
				while(stack.size() != 0) {
					char ch = stack.pop();
					if(ch != '@' && ch != ',')
						sb_tmp.append(ch);
				}

				sb_tmp = sb_tmp.reverse();
				all_exps.add(sb_tmp.toString());
				sb_tmp = new StringBuffer("");
			}
			else
				stack.push(c);
		}

		StringBuffer last_str = new StringBuffer("");
		while(stack.size() != 0) {
			last_str.append(stack.pop());
		}

		if(last_str.length() != 0) {
			last_str = last_str.reverse();	

			all_exps.add(last_str.toString());
		}

		for(int i = 0; i < all_exps.size(); ++i) {
			String tmp_s = all_exps.get(i);
			if(tmp_s.equals(" ") || tmp_s.equals("")) {
				all_exps.remove((int)i);
				i -= 1; // Needed because we removed an element.
			}
		}

		List<String> final_all_exps = new ArrayList<>();
		for(int i = 0; i < all_exps.size(); ++i) {
			String tmp_s = all_exps.get(i);
			List<String> split_s = Util.split_with_ops(tmp_s);
			// System.out.println("split_s: " + split_s);
			for(int j = 0; j < split_s.size(); ++j) {
				String js = split_s.get(j);
				if(Util.is_operator(js)) {
					split_s.remove((int)(j));
					j -= 1;
				}
				else {
					final_all_exps.add(js);
				}
			}

			String final_s = "";
			for(int j = 0; j < split_s.size(); ++j)
				final_s += split_s.get(j);
			all_exps.set((int)i, final_s);
		}

		/*
			 System.out.print("final_all_exps: ");
			 for(int i = 0; i < final_all_exps.size(); ++i) {
			 System.out.print("< " + final_all_exps.get(i) + " > ");
			 }
			 System.out.println();
		 */

		// Note: We have to sort the array list in descending to ascending order in terms of size...
		// Sorting
		List<String> new_li = new ArrayList<>();
		int li_len = final_all_exps.size();
		while(final_all_exps.size() != 0) {
			int max_index = get_max_size_in_index(final_all_exps, 0);
			new_li.add(final_all_exps.get(max_index));
			final_all_exps.remove(max_index);
		}

		names_na_indices.names = new_li;
		names_na_indices.range_indices = range_indices;

		return names_na_indices;
	}

	static int get_max_size_in_index(List<String> li, int from_index) {
		int current_len = -1;
		int index = 0;
		for(int i = from_index; i < li.size(); ++i) {
			int len = li.get(i).length();
			if(len > current_len) {
				index = i;
				current_len = len;
			}
		}

		return index;
	}

	static Str_NA_Indices replace_in_str(String str, String from, String to, List<RangeIndices> ignore_indices) {
		StringBuffer new_str = new StringBuffer(str);
		int index = 0;

		while(index != -1 && index < new_str.length()) {
			index = new_str.indexOf(from, index);

			// Checking if index is in between ignore_indices
			boolean in_between = false;
			for(RangeIndices ri: ignore_indices) {
				if(index >= ri.from_index && index <= ri.to_index) {
					in_between = true;
					break;
				}
			}

			if(in_between) {
				index = index + from.length();
				continue;
			}

			if(index != -1) {
				int begin = index;
				int end = index + from.length();
				new_str = new_str.replace(begin, end, to);
				int by = to.length() - from.length();

				// Increasing the RangeIndices.
				for(int i = 0; i < ignore_indices.size(); ++i) {
					RangeIndices ri = ignore_indices.get(i);
					if(ri.from_index > begin) {
						RangeIndices new_ri = new RangeIndices();
						new_ri.from_index = ri.from_index + by;
						new_ri.to_index = ri.to_index + by;

						ignore_indices.set(i, new_ri);
					}
				}

				// Adding 'to' to ignore_indices
				RangeIndices new_ri = new RangeIndices();
				new_ri.from_index = begin;
				new_ri.to_index = begin + to.length() - 1;
				ignore_indices.add(new_ri);

				index = new_str.indexOf(from, end);
			}
		}

		Str_NA_Indices str_na_indices = new Str_NA_Indices();
		str_na_indices.str = new_str.toString();
		str_na_indices.range_indices = ignore_indices;

		return str_na_indices;
	}

	static List<String> split_into_var_names(String s) {
		List<String> li = new ArrayList<>();
		int index_of_prev_ch = 0;

		int quotes_count = 0;
		for(int i = 0; i < s.length(); ++i) {
			char c = s.charAt(i);
			if(c == '\"')
				quotes_count++;

			else if(quotes_count % 2 == 0 && (c == '.') || c == '^') {
				String str = s.substring(index_of_prev_ch, i);
				if(i != 1 && !str.equals(""))
					li.add(str);
				li.add(String.valueOf(c));

				index_of_prev_ch = i + 1;
			}
		}

		String last_str = s.substring(index_of_prev_ch);
		if(!s.equals(""))
			li.add(last_str);

		return li;
	}

	static String eat_pointers_and_array(String s) {
		String new_string = s;
		if(s.indexOf('*') != -1)
			new_string = s.substring(0, s.indexOf('*'));
		else if(s.indexOf("@array@") != -1)
			new_string = s.substring(0, s.indexOf("@array@"));

		return new_string;
	}

	static String remove_str_in_string(String str, String rm) {
		String new_string = str;
		int index = new_string.indexOf(rm);
		while(index >= 0) {
			new_string = new_string.substring(index + rm.length());
			index = new_string.indexOf(rm);
		}

		return new_string;
	}
}
