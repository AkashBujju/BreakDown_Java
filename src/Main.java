import java.util.List;
import java.util.ArrayList;

public class Main {
		static List<String> keywords = new ArrayList<>(); 
		static List<String> split_sequences = new ArrayList<>();

	public static void main(String[] args) {
		String filename = "C:\\Users\\Akash\\Documents\\GitHub\\BreakDown_Java\\asset\\demo.veg";
		StringBuffer data;
		MyFile my_file;
		
		init_ops();

		try {
			my_file = new MyFile(filename);
			data = my_file.get_data();

			String eaten_string = Util.eat_spaces(data.toString());
			data = new StringBuffer(eaten_string);

			System.out.println("data: " + data);
			System.out.println("Size: " + data.length());
			System.out.println();

			List<String> split_str = split_with_tokens(data.toString(), split_sequences);
			System.out.println("*********************************");
			System.out.println("Size: " + split_str.size());
			for(String s: split_str) {
				System.out.println(s);
			}

			/*
			// Process character by character ....
			BreakDown_1 b = new BreakDown_1(data.toString());
			List<String> bd_1_li = b.get_broken_string();

			System.out.println("-----START-----");
			for(String s: bd_1_li) {
				System.out.println(s);
			}
			System.out.println("-----END-----");
			System.out.println();

			Info info = new Info();
			for(String s: bd_1_li) {
				StatementInfo stat_info = Util.get_stat_info_from_broken_str(s);
				if(stat_info != null)
					info.add(stat_info);
			}

			info.process();
			*/
		}
		catch (Exception e) {
			System.out.println("Exception caught in class Main: ");
			System.out.println("What: " + e);
		}
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

	static void init_ops() {
		keywords.add("func");
		keywords.add("::struct");
		keywords.add("::enum");
		keywords.add("if");
		keywords.add("else");
		keywords.add("elseif");
		keywords.add("while");
		keywords.add("return");
		keywords.add("use");

		split_sequences.add(";");
		split_sequences.add("{");
		split_sequences.add("}");
		split_sequences.add("->");
		for(int i = 0; i < keywords.size(); ++i)
			split_sequences.add(keywords.get(i));
	}

	static List<String> split_with_tokens(String str, List<String> tokens) {
		List<String> split_list = new ArrayList<>();

		if(str.length() == 0)
			return split_list;

		int current_index = 0;
		StringAndIndex si = get_first_index_and_str(str, current_index, split_sequences);

		if(si.index != -1) {
			split_list.add(si.str);
			current_index = si.index + si.str.length();
		}

		while(current_index < str.length()) {
			si = get_first_index_and_str(str, current_index, split_sequences);

			if(si.index != -1) {
				String li_str = si.str;
				String substr = str.substring(current_index, si.index);

				split_list.add(li_str);
				if(!substr.equals("")) {
					split_list.add(substr);
				}

				current_index = si.index + li_str.length();
			}
			else {
				String substr = str.substring(current_index);
				split_list.add(substr);
				current_index = str.length();
			}

		}

		return split_list;
	}
}

class StringAndIndex {
	int index = -1;
	String str = "";
}
