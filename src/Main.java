import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.time.Instant;
import java.time.Duration;

public class Main {
	public static void main(String[] args) {
		if(args.length < 1) {
			System.out.println("Error missing filename");
			return;
		}

		String filename = "C:\\Users\\akash\\Documents\\GitHub\\BreakDown_Java\\asset\\" + args[0];
		StringBuffer data;
		MyFile my_file;

		boolean show_sequences = false;
		boolean show_data = false;
		boolean show_symbol_table = false;
		boolean silent_compile = false;

		// Processing command-line-arguments.
		for(int i = 0; i < args.length; ++i) {
			String arg = args[i];
			if(arg.equals("-show_seq"))
				show_sequences = true;
			else if(arg.equals("-show_data"))
				show_data = true;
			else if(arg.equals("-show_table"))
				show_symbol_table = true;
			else if(arg.equals("-silent"))
				silent_compile = true;
		}

		Util.init_split_sequences();
		Util.init_ops();

		try {
			Instant start_time = Instant.now();

			my_file = new MyFile();
			data = my_file.get_data(filename);
			Util.quotes_range_indices = Util.get_range_indices_of_quotes(data.toString());


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


			SyntaxAnalyser sc = new SyntaxAnalyser(sq_arr, my_file, Util.quotes_range_indices, id_number, id_char_index);
			ErrorLog error_log = sc.validate_syntax();
			if(error_log.log.size() > 0) {
				System.out.println("Syntax Errors Found.");
				error_log.show();
				return;
			}

			SemanticAnalyser sa = new SemanticAnalyser(sc.infos, Util.quotes_range_indices);
			sa.start();
			error_log = sa.error_log;
			if(error_log.log.size() != 0) {
				error_log.show();
				return;
			}

			Translater translater = new Translater(sa);
			translater.translate("C:\\Users\\akash\\Documents\\GitHub\\BreakDown_Java\\asset\\" + args[0] + ".cpp");

			Instant end_time = Instant.now();
			long timeElapsed = Duration.between(start_time, end_time).toMillis();
			if(!silent_compile) {
				System.out.println();
				System.out.println("Took " + timeElapsed + " ms to compile.");
				System.out.println("----------------------");
				System.out.println();
			}

			// OPTIONS
			if(show_data) {
				System.out.println("data: " + data);
				System.out.println("Size: " + data.length());
				System.out.println("Lines: " + my_file.line_number);
				System.out.println("----------------------------------------------");
				System.out.println();
			}

			if(show_sequences) {
				System.out.println("Num Sequences: "+ sq_info.size());
				System.out.println();
				int max_len = get_max_len(sq_info);
				for(SequenceInfo sq: sq_info) {
					System.out.print(sq.str);
					print_n_spaces(max_len - sq.str.length() + 2);
					System.out.println(SequenceTypeInfo.get_in_str(sq.seq_type));
				}
				System.out.println();
				System.out.println();
			}

			if(show_symbol_table)
				sa.symbol_table.show_all();
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

	public static int get_max_len(List<SequenceInfo> sq_infos) {
		int max_len = 0;
		for(SequenceInfo sq_info: sq_infos) {
			int len = sq_info.str.length();
			if(len > max_len)
				max_len = len;
		}

		return max_len;
	}

	public static void print_n_spaces(int n) {
		for(int i = 0; i < n; ++i)
			System.out.print(" ");
	}
}
