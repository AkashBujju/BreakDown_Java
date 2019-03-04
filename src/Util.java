import java.util.List;
import java.util.ArrayList;

class StringAndIndex {
	int index = -1;
	String str = "";
}

class RangeIndices {
	int from_index = -1;
	int to_index = -1;
}

public class Util {
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
		for(int i = 0; i < str.length(); ++i) {
			if(is_index_inside_quotes(i, ri))
				continue;

			char c = str.charAt(i);
			if(c == ch)
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

		// is_float
		boolean is_float = true;
		int dot_count = 0;
		for(int i = 0; i < str.length(); ++i) {
			char c = str.charAt(i);
			if(c == '.')
				dot_count += 1;
			if(dot_count > 1) {
				is_float = false;
				break;
			}
			else if((c < '0' || c > '9') && c != '.') {
				is_float = false;
				break;
			}
		}
		if(is_float)
			return "float";

		// is_bool
		if(str.equals("true") || str.equals("false"))
			return "bool";

		return "not_known";
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
		boolean is_valid = true;
		for(int i = 1; i < len; ++i) {
			char c = type.charAt(i);
			if(!is_char_alpha_digit_underscore(c)) {
				for(int j = i + 1; j < len; ++j) {
					char c2 = type.charAt(j);
					if(c2 != '*') {
						is_valid = false;
						break;
					}
				}
			}
		}

		if(is_valid)
			return true;

		// Checking if its an array.
		int i = 1;
		if(i < len) {
			char c = type.charAt(i);
			if((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {} // do nothing
			else
				return false;
		}

		boolean array_square_encountered = false;
		for(i = 2; i < len; ++i) {
			char c = type.charAt(i);
			if(!Util.is_char_alpha_digit_underscore(c)) {
				int num_close_square = 0;
				int num_digits = 0;
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
							i = j;
							break;
						}
						else
							return false;
					}
					else
						num_digits += 1;

					j += 1;
				}

				if(num_close_square != 1 || num_digits == 0)
					return false;
			}
			else if(array_square_encountered)
				return false;
		}

		return true;
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

	static List<String> split_into_tokens(String str, List<String> split_sequences, List<RangeIndices> quotes_range_indices) {
		List<String> split_list = new ArrayList<>();
		int len = str.length();

		if(len == 0)
			return split_list;

		int current_index = 0;
		StringAndIndex si;
		boolean if_index_inside_quotes = false;

		int i = 0;
		si = get_first_index_and_str(str, current_index + i, split_sequences);
		while(si.index != -1 || current_index < len) {
			boolean index_inside_quotes = Util.is_index_inside_quotes(si.index, quotes_range_indices);

			if(si.index == -1 && !index_inside_quotes) {
				String substring = str.substring(current_index);
				if(!substring.equals(""))
					split_list.add(substring);

				break;
			}

			if(!index_inside_quotes) {
				String substring = str.substring(current_index, si.index);

				if(!substring.equals(""))
					split_list.add(substring);
				split_list.add(si.str);
				current_index = si.index + si.str.length();
				i = 0;
			}
			else {
				i += 1;
			}

			si = get_first_index_and_str(str, current_index + i, split_sequences);
		}

		return split_list;
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

	static String is_valid_exp(String s, List<RangeIndices> ri, int from_index) {
		int quotes_count = get_num_quotes(s);
		if(quotes_count % 2 != 0)
			return "Number of open quotes not matching with the number of closing quotes in the expression.";

		int open_paren_count = 0;
		int close_paren_count = 0;
		int open_square_count = 0;
		int close_square_count = 0;
		for(int i = 0; i < s.length(); ++i) {
			char c = s.charAt(i);
			if(is_index_inside_quotes(i + from_index, ri)) {
				continue;
			}

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
			return "Number if '[' not matching number of ']' in the expression.";

		// @Incomplete: Anything else....
		// @Incomplete: Anything else....
		// @Incomplete: Anything else....
		// @Incomplete: Anything else....
		
		return "none";
	}
}
