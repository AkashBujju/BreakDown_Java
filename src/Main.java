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
			System.out.println("----------------------------------------------");
			System.out.println();

			List<String> tokens = Util.split_into_tokens(data.toString(), split_sequences, quotes_range_indices);
			List<SequenceType> st_li = SequenceTypeInfo.get_sequence_types(tokens, quotes_range_indices);

			List<SequenceInfo> sq_info = new ArrayList<>();
			for(int i = 0; i < tokens.size(); ++i) {
				SequenceInfo info = new SequenceInfo(st_li.get(i), tokens.get(i));
				sq_info.add(info);
			}

			/*
			for(SequenceInfo sq: sq_info) {
				System.out.println(sq.str + " @@@@@@  " + SequenceTypeInfo.get_in_str(sq.seq_type));
			}
			*/

			Info info = new Info(sq_info, my_file, quotes_range_indices);
			info.process();
		}
		catch (Exception e) {
			System.out.println("Exception caught in class Main: ");
			System.out.println("What: " + e);
		}
	}

	static void init_ops() {
		// @Incomplete ... 
		// @Incomplete ... 

		keywords.add("func"); keywords.add("::struct");
		keywords.add("::enum"); keywords.add("if");
		keywords.add("else"); keywords.add("while");
		keywords.add("return"); keywords.add("use");

		split_sequences.add(";"); split_sequences.add("{");
		split_sequences.add("}");
		for(int i = 0; i < keywords.size(); ++i)
			split_sequences.add(keywords.get(i));

		arith_ops.add("+"); arith_ops.add("-");
		arith_ops.add("*"); arith_ops.add("/");
		arith_ops.add("%");

		logical_ops.add("||"); logical_ops.add("&&"); logical_ops.add("!");

		relational_ops.add("=="); relational_ops.add("!=");
		relational_ops.add(">="); relational_ops.add("<=");
		relational_ops.add("<"); relational_ops.add(">");

		bitwise_ops.add("|"); bitwise_ops.add("&");
		bitwise_ops.add("^"); bitwise_ops.add(">>>");
		bitwise_ops.add("<<<");
	}
}
