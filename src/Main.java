import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

public class Main {
	public static void main(String[] args) {
		String filename = "C:\\Users\\Akash\\Documents\\GitHub\\BreakDown_Java\\asset\\demo.veg";
		StringBuffer data;
		MyFile my_file;

		Util.init_split_sequences();
		Util.init_ops();

		try {
			my_file = new MyFile();
			data = my_file.get_data(filename);
			Util.quotes_range_indices = Util.get_range_indices_of_quotes(data.toString());

			System.out.println("data: " + data);
			System.out.println("Size: " + data.length());
			System.out.println("Lines: " + my_file.line_number);
			System.out.println("----------------------------------------------");
			System.out.println();

			List<String> tokens = LexicalAnalyser.split(data.toString(), Util.split_sequences, Util.quotes_range_indices);
			List<SequenceType> st_li = SequenceTypeInfo.get_sequence_types(tokens, Util.quotes_range_indices);

			List<SequenceInfo> sq_info = new ArrayList<>();
			for(int i = 0; i < tokens.size(); ++i) {
				SequenceInfo info = new SequenceInfo(st_li.get(i), tokens.get(i), i);
				sq_info.add(info);
			}

			SequenceInfo[] sq_arr = (sq_info.toArray(new SequenceInfo[0]));
			HashMap<Integer, Integer> id_number = my_file.get_index_map(sq_info);
			HashMap<Integer, Integer> id_char_index = my_file.get_char_index_map(sq_info);

			System.out.println("Num Sequences: "+ sq_info.size());
			System.out.println();

			for(SequenceInfo sq: sq_info) {
				System.out.println("<" + sq.str + "> -------->  " + SequenceTypeInfo.get_in_str(sq.seq_type));
			}
			System.out.println();
			System.out.println();

			SyntaxAnalyser sc = new SyntaxAnalyser(sq_arr, my_file, Util.quotes_range_indices, id_number, id_char_index);
			ErrorLog error_log = sc.validate_syntax();
			if(error_log.log.size() > 0) {
				error_log.show();
				return;
			}

			System.out.println("-----------------");
			System.out.println("No syntax errors.");
			System.out.println("-----------------");
			System.out.println();

			SemanticAnalyser sa = new SemanticAnalyser(sc.infos, Util.quotes_range_indices);
			sa.start();
			error_log = sa.error_log;
			if(error_log.log.size() != 0) {
				error_log.show();
				return;
			}

			System.out.println();
			System.out.println("No semantic errors.");
		}
		catch (Exception e) {
			System.out.println();
			System.out.println("INTERNAL COMPILER ERROR !!!!!!");
			System.out.println("--------------------------------");
			System.out.println("What: " + e);
			e.printStackTrace();
			System.out.println("--------------------------------");
		}
	}
}
