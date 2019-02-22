import java.util.List;
import java.util.ArrayList;

public class Main {
		static List<String> keywords = new ArrayList<>(); 
		static List<String> split_sequences = new ArrayList<>();
		static List<String> arith_ops = new ArrayList<>();
		static List<String> logical_ops = new ArrayList<>();
		static List<String> relational_ops = new ArrayList<>();
		static List<String> bitwise_ops = new ArrayList<>();
		static List<RangeIndices> quotes_range_indices = new ArrayList<>();

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

			quotes_range_indices = Util.get_range_indices_of_quotes(data.toString());
			System.out.println("data: " + data);
			System.out.println("Size: " + data.length());
			System.out.println();

			List<String> tokens = split_into_tokens(data.toString(), split_sequences);
			List<SequenceType> st_li = SequenceTypeInfo.get_sequence_types(tokens, quotes_range_indices);

			List<SequenceInfo> sq_info = new ArrayList<>();
			for(int i = 0; i < tokens.size(); ++i) {
				SequenceInfo info = new SequenceInfo(st_li.get(i), tokens.get(i));
				sq_info.add(info);
			}
			
			Info info = new Info(sq_info, my_file);
			info.process();

			/*
			for(SequenceInfo sq: sq_info) {
				System.out.println(sq.str + " @@@@@@  " + SequenceTypeInfo.get_in_str(sq.seq_type));
			}
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
		// @Incomplete ... 
		// @Incomplete ... 
		
		keywords.add("func");
		keywords.add("::struct");
		keywords.add("::enum");
		keywords.add("if");
		keywords.add("else");
		keywords.add("while");
		keywords.add("return");
		keywords.add("use");

		split_sequences.add(";");
		split_sequences.add("{");
		split_sequences.add("}");
		split_sequences.add("->");
		for(int i = 0; i < keywords.size(); ++i)
			split_sequences.add(keywords.get(i));

		arith_ops.add("+");
		arith_ops.add("-");
		arith_ops.add("*");
		arith_ops.add("/");
		arith_ops.add("%");

		logical_ops.add("||");
		logical_ops.add("&&");
		logical_ops.add("!");
		
		relational_ops.add("==");
		relational_ops.add("!=");
		relational_ops.add(">=");
		relational_ops.add("<=");
		relational_ops.add("<");
		relational_ops.add(">");

		bitwise_ops.add("|");
		bitwise_ops.add("&");
		bitwise_ops.add("^");
		bitwise_ops.add(">>>");
		bitwise_ops.add("<<<");
	}

	static List<String> split_into_tokens(String str, List<String> tokens) {
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
}

class StringAndIndex {
	int index = -1;
	String str = "";
}

class RangeIndices {
	int from_index = -1;
	int to_index = -1;
}
