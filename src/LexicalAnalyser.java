import java.util.List;
import java.util.ArrayList;

class LexicalAnalyser {
	public static List<String> split(String str, List<String> split_sequences, List<RangeIndices> quotes_range_indices) {
		List<String> split_list = new ArrayList<>();
		int len = str.length();

		if(len == 0)
			return split_list;

		int current_index = 0;
		StringAndIndex si;
		boolean if_index_inside_quotes = false;

		int i = 0;
		si = Util.get_first_index_and_str(str, current_index + i, split_sequences);
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

			si = Util.get_first_index_and_str(str, current_index + i, split_sequences);
		}

		return split_list;

	}
}
