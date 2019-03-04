import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

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
			my_file = new MyFile();
			data = my_file.get_data(filename);
			quotes_range_indices = Util.get_range_indices_of_quotes(data.toString());

			/*
			System.out.println("quotes_range_indices: ");
			for(RangeIndices ri: quotes_range_indices) {
				System.out.println(ri.from_index + " to " + ri.to_index);
			}
			*/

			System.out.println("data: " + data);
			System.out.println("Size: " + data.length());
			System.out.println("Lines: " + my_file.line_number);
			System.out.println("----------------------------------------------");
			System.out.println();

			List<String> tokens = Util.split_into_tokens(data.toString(), split_sequences, quotes_range_indices);
			List<SequenceType> st_li = SequenceTypeInfo.get_sequence_types(tokens, quotes_range_indices);

			List<SequenceInfo> sq_info = new ArrayList<>();
			for(int i = 0; i < tokens.size(); ++i) {
				SequenceInfo info = new SequenceInfo(st_li.get(i), tokens.get(i), i);
				sq_info.add(info);
			}

			SequenceInfo[] sq_arr = (sq_info.toArray(new SequenceInfo[0]));
			HashMap<Integer, Integer> id_number = my_file.get_index_map(sq_info);
			HashMap<Integer, Integer> id_char_index = my_file.get_char_index_map(sq_info);

			/*
			System.out.println("id_char_indices: ");
			Iterator it = id_char_index.keySet().iterator();
			while(it.hasNext()) {
				Integer i = (Integer)(it.next());
				System.out.println("id: " + i + ", index: " + id_char_index.get(i));
			}
			*/

			System.out.println("Num Sequences: "+ sq_info.size());
			System.out.println();

			for(SequenceInfo sq: sq_info) {
				System.out.println("<" + sq.str + "> -------->  " + SequenceTypeInfo.get_in_str(sq.seq_type));
			}

			SyntaxChecker sc = new SyntaxChecker(sq_arr, my_file, quotes_range_indices, id_number, id_char_index);
			ErrorLog error_log = sc.validate_syntax();
			if(error_log.log.size() > 0) {
				error_log.show();
				return;
			}

			System.out.println();
			System.out.println("No syntax errors.");
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
