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
}
