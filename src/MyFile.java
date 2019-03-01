import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.HashMap;

class LineInfo {
	int line_number = -1;
	int start_index = -1;
	int end_index = -1;
}

class MyFile {
	private String filename;
	private List<LineInfo> line_info = new ArrayList<>();
	int line_number = 0;
	StringBuffer data;

	StringBuffer get_data(String filename, List<RangeIndices> ri, boolean ignore_comments) throws FileNotFoundException {
		this.filename = filename;
		line_info.clear();
		data = new StringBuffer();

		// Storing the beginning and ending index of each line, ignoring the new_line character
		try (Scanner s = new Scanner(new BufferedReader(new FileReader(filename)))) {
			int current_begin_index = 0;
			line_number = 0;

			while (s.hasNextLine()) {
				line_number += 1;
				String current_line = s.nextLine();
				current_line = Util.eat_only_spaces(current_line);

				if(ignore_comments) {
					for(int i = 0; i < current_line.length(); ++i) {
						char c = current_line.charAt(i);
						if(c == '#') {
							boolean is_inside_quotes = Util.is_index_inside_quotes(data.length() + i, ri);
							if(!is_inside_quotes) {
								current_line = current_line.substring(0, i);
								break;
							}
						}
					}
				}

				data.append(current_line);

				int end_index = 0;
				end_index = current_line.length() + current_begin_index - 1;

				if(!current_line.equals("")) {
					LineInfo tmp_line_info = new LineInfo();
					tmp_line_info.line_number = line_number;
					tmp_line_info.start_index = current_begin_index;
					tmp_line_info.end_index = end_index;

					line_info.add(tmp_line_info);
				}

				current_begin_index = end_index + 1;
			}
		}

		return data;
	}

	HashMap<Integer, Integer> get_index_map(List<SequenceInfo> li) {
		HashMap<Integer, Integer> hm = new HashMap<>();
		int current_char_index = 0;

		for(SequenceInfo si: li) {
			hm.put(si.id, get_line_number(current_char_index));
			current_char_index += si.str.length();
		}

		return hm;
	}

	int get_line_number(int index) {
		for(LineInfo l: line_info) {
			if(index >= l.start_index && index <= l.end_index)
				return l.line_number;
		}

		return -1;
	}
}
