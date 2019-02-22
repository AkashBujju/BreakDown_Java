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
}

/*
// Example of pattern: "true, false, :=, ::struct"
static String get_first_occuring_pattern(String str, int from_index, String pat) {
String first_occurring_pattern = "";
String new_str = str.substring(from_index);
String[] patterns = pat.split(",");
int first_occuring_index = 100000; // Sufficiently max.

for(String s: patterns) {
int index = new_str.indexOf(s);
if(index != -1 && index < first_occuring_index) {
first_occurring_pattern = s;
first_occuring_index = index;
}
}

return first_occurring_pattern;
}

// Example of pattern: "true, false, :=, ::struct"
static int get_first_index_of(String str, int from_index, String pat) {
String new_str = str.substring(from_index);
String[] patterns = pat.split(",");
int first_occurring_index = 100000; // Sufficiently max.

for(String s: patterns) {
int index = new_str.indexOf(s);
if(index != -1 && index < first_occurring_index) {
first_occurring_index = index;
}
}

if(first_occurring_index == 100000)
return -1;

return first_occurring_index + from_index;
}

static boolean if_matches(String str, int from_index, String match) {
int count = 0;

for(int i = 0; i < match.length(); ++i) {
if(i + from_index >= str.length())
return false;

count += 1;
char c1 = str.charAt(i + from_index);
char c2 = match.charAt(i);

if(c1 != c2) {
return false;
}
}

if(count != match.length())
return false;

return true;
}

// Pattern should be seperated by comma.
static boolean if_has_patterns(String str, int from_index, int to_index, String pat) {
String[] split_pat = pat.split(",");
String new_str = str.substring(from_index, to_index);

for(String s: split_pat) {
int found_index = new_str.indexOf(s);

if(found_index != -1)
return true;
}

return false;
}

static List<String> split_using_at(String str) {
	List<String> li = new ArrayList<>();

	int quotes_count = 0;
	for(int i = 0; i < str.length(); ++i) {
		char c = str.charAt(i);
		if(c == '\"')
			quotes_count++;

		else if(quotes_count % 2 == 0 && c == '@') {
			// Find the index of next '@', if found then find the the string is between current '@' and next '@'. If not found then find the string b/w current '@' to the end of the string.

			int index_of_next_at = str.indexOf("@", i + 1);
			if(index_of_next_at != -1) {
				li.add(str.substring(i + 1, index_of_next_at));
				i = index_of_next_at - 1; // because i gets incremented.
			}
			else {
				li.add(str.substring(i + 1));
				break;
			}
		}
	}

	return li;
}
*/
