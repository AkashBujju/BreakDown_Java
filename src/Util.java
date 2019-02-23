import java.util.List;
import java.util.ArrayList;

class Util {
	// @Redundant: Can be made into a single method
	static String eat_spaces(String str) {
		String tmp = "";
		int quotes_count = 0;
		for(int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if(c == '\"')
				quotes_count += 1;

			if(quotes_count % 2 != 0)
				tmp += c;
			else if(c != '\t' && c != ' ' && c != '\n')
				tmp += c;
		}

		return tmp;
	}

	// @Redundant: Can be made into a single method
	static String eat_only_spaces(String str)  {
		String tmp = "";

		int quotes_count = 0;
		for(int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if(c == '\"')
				quotes_count += 1;

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

	static boolean is_valid_type(String type, List<String> all_types) {
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

				index_of_prev_ch = i;
			}
		}

		String s;
		if(li.size() == 0)
			s = str.substring(index_of_prev_ch);
		else
			s = str.substring(index_of_prev_ch + 1);

		li.add(s);

		return li;
	}
}
