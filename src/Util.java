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

	static StatementType get_stat_type_from_broken_str(String broken_string) {
		if(broken_string.equals(""))
			return StatementType.NOT_KNOWN;

		char start_char = broken_string.charAt(0);
		if(start_char != '@')
			return StatementType.NOT_KNOWN;

		int index_of_second_at = broken_string.indexOf("@", 1);
		if(index_of_second_at == -1)
			return StatementType.NOT_KNOWN;

		String type = broken_string.substring(1, index_of_second_at);
		if(type.equals("func_def"))
			return StatementType.FUNC_DEF;
		else if(type.equals("var_declare_1"))
			return StatementType.VAR_DECLARE_1;
		else if(type.equals("var_declare_2"))
			return StatementType.VAR_DECLARE_2;
		else if(type.equals("var_assign"))
			return StatementType.VAR_ASSIGN;
		else if(type.equals("enum"))
			return StatementType.ENUM;
		else if(type.equals("struct"))
			return StatementType.STRUCT;
		else if(type.equals("if"))
			return StatementType.IF;
		else if(type.equals("else"))
			return StatementType.ELSE;
		else if(type.equals("elseif"))
			return StatementType.ELSE_IF;
		else if(type.equals("while"))
			return StatementType.WHILE;
		else if(type.equals("use"))
			return StatementType.USE;
		else if(type.equals("error"))
			return StatementType.ERROR;

		return StatementType.NOT_KNOWN;
	}

	static StatementInfo get_stat_info_from_broken_str(String broken_string) {
		StatementType type = get_stat_type_from_broken_str(broken_string);
		StatementInfo info = null;

		if(type == StatementType.VAR_DECLARE_1)
			info = new VariableInfo(broken_string, type);
		else if(type == StatementType.VAR_DECLARE_2)
			info = new VariableInfo(broken_string, type);
		else if(type == StatementType.VAR_ASSIGN)
			info = new VariableInfo(broken_string, type);
		else if(type == StatementType.FUNC_DEF)
			info = new FunctionInfo(broken_string);
		else if(type == StatementType.ERROR)
			info = new ErrorInfo(broken_string);

		return info;
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
}
