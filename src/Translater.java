import java.io.*;
import java.util.List;
import java.util.ArrayList;

public class Translater {
	SemanticAnalyser sa;
	FileWriter fw;
	String tab = "	";

	Translater(SemanticAnalyser sa) {
		this.sa = sa;
	}
	
	void translate(String filename) {
		try {
			fw = new FileWriter(filename);

			write_header_files();
			List<Info> infos = sa.infos;
			int len = infos.size();

			for(int i = 0; i < len; ++i) {
				Info info = infos.get(i);
				if(info.info_type == InfoType.STRUCT)
					write_struct((StructInfo)(info));
				else if(info.info_type == InfoType.FUNCTION)
					write_function((FunctionInfo)(info));
			}

			fw.close();
		}
		catch(IOException e) {
			System.out.println("Unable to open file: " + filename);
			e.printStackTrace();
		}
	}

	private void write_header_files() {
		try {
			fw.write("#include <iostream>\n");
			fw.write("#include <string>\n");
			fw.write("#include <stdio.h>\n");
			fw.write("using namespace std;\n\n");
		}
		catch(IOException e) {
			System.out.println(e);
		}
	}

	private void write_ifs(IfInfo if_info) {
		try {
			write_tab();
			fw.write("if (" + if_info.exp + ") {\n");
			List<Info> infos = if_info.infos;
			int len = if_info.infos.size();

			for(int i = 0; i < len; ++i) {
				Info info = infos.get(i);
				write_tab();
				if(info.info_type == InfoType.VAR_DECL)
					write_var_decl((VarDeclInfo)(info));
				else if(info.info_type == InfoType.IF)
					write_ifs((IfInfo)(info));
			}

			write_tab();
			fw.write("}\n");
		}
		catch(IOException e) {
			System.out.println(e);
		}
	}

	private void write_var_assgn(VarAssignInfo var_assign_info) {
			/*
		try {
			String name = var_assign_info.var_name;
			String raw_value = var_assign_info.raw_value;

				
		}
		catch(IOException e) {
			System.out.println(e);
		}
			*/

	}

	private void write_var_decl(VarDeclInfo var_decl_info) {
		try {
			String raw_value = var_decl_info.raw_value;
			String type = var_decl_info.type;
			String name = var_decl_info.name;
			String arr_str = "";
			String ptr_str = "";
			boolean is_array = false;

			if(type.equals("not_known")) {
				String scope_name = sa.id_scope_map.get(var_decl_info.id);
				type = sa.symbol_table.get_type(name, scope_name);

				if(type.indexOf('@') != -1) {
					arr_str = "[]";
					type = type.substring(0, type.indexOf('@'));
					is_array = true;
				}
			}
			else if(type.indexOf('[') != -1) {
				arr_str = type.substring(type.indexOf('['), type.lastIndexOf(']') + 1);
				type = type.substring(0, type.indexOf('['));
				is_array = true;
			}

			if(type.indexOf('*') != -1) {
				ptr_str = type.substring(type.indexOf('*'), type.lastIndexOf('*') + 1);
				type = type.substring(0, type.indexOf('*'));
			}

			if(sa.name_structvars_map.containsKey(type) || is_array)
				raw_value = "{ " + raw_value + " }";

			name = ptr_str + name + arr_str;

			if(raw_value.equals(""))
				fw.write(type + " " + name + ";\n");
			else {
				raw_value = replace_all_ops(raw_value);
				fw.write(type + " " + name + " = " + raw_value + ";\n");
			}
		}
		catch(IOException e) {
			System.out.println(e);
		}
	}

	private void write_function(FunctionInfo function_info) {
		try {
			fw.write(function_info.return_type + " " + function_info.name + "(");

			// Function arguments.
			List<VarDeclInfo> var_decl_infos = function_info.var_args;
			int var_args_len = var_decl_infos.size();
			for(int i = 0; i < var_args_len; ++i) {
				VarDeclInfo var_decl_info = var_decl_infos.get(i);
				String type = var_decl_info.type;
				String name = var_decl_info.name;
				String arr_str = "";
				String ptr_str = "";

				if(type.indexOf('[') != -1) {
					type = type.substring(0, type.indexOf('['));
					arr_str = "[]";
				}
				if(type.indexOf('*') != -1) {
					ptr_str = type.substring(type.indexOf('*'), type.lastIndexOf('*') + 1);
					type = type.substring(0, type.indexOf('*'));
				}

				name = ptr_str + name + arr_str;

				fw.write(type + " " + name);
				if(i != var_args_len - 1)
					fw.write(", ");
			}

			fw.write(") {\n");

			List<Info> infos = function_info.infos;
			int infos_len = infos.size();
			for(int i = 0; i < infos_len; i++) {
				Info info = infos.get(i);
				if(info.info_type == InfoType.VAR_DECL) {
					write_tab();
					write_var_decl((VarDeclInfo)(info));
				}
				else if(info.info_type == InfoType.IF)
					write_ifs((IfInfo)(info));
			}

			fw.write("}\n\n");
		}
		catch(IOException e) {
			System.out.println(e);
		}
	}

	private void write_struct(StructInfo struct_info) {
		try {
			String name = struct_info.name;
			StructVars struct_vars = sa.name_structvars_map.get(name);
			List<VarDeclInfo> var_decl_infos = struct_vars.var_decl_infos;
			int num_vars = var_decl_infos.size();

			fw.write("struct " + name + " {\n");
			for(int i = 0; i < num_vars; ++i) {
				VarDeclInfo var_decl_info = var_decl_infos.get(i);
				String raw_value = var_decl_info.raw_value;
				String type = var_decl_info.type;
				String arr_str = "";
				String ptr_str = "";

				if(type.indexOf("@array@") != -1) {
					String actual_type = struct_vars.actual_types.get(i);
					arr_str = actual_type.substring(actual_type.indexOf('['), actual_type.lastIndexOf(']') + 1);

					type = type.substring(0, type.indexOf('@'));
					if(!raw_value.equals(""))
						raw_value = "{ " + raw_value + " }";
				}
				if(type.indexOf('*') != -1) {
					ptr_str = type.substring(type.indexOf('*'), type.lastIndexOf('*') + 1);
					type = type.substring(0, type.indexOf('*'));
				}

				String new_name = ptr_str + var_decl_info.name + arr_str;

				write_tab();
				if(raw_value.equals(""))
					fw.write(type + " " + new_name + ";\n");
				else
					fw.write(type + " " + new_name + " = " + raw_value + ";\n");
			}

			fw.write("};\n\n");
		}
		catch(IOException e) {
			System.out.println(e);
		}
	}

	private void write_tab() {
		try {
			fw.write(tab);
		}
		catch(IOException e) {
			System.out.println(e);
		}
	}

	private String replace_in_built_funcs(String s) {
		StringBuffer sb = new StringBuffer(s);

		// @Incomplete: 
		// @Incomplete: 
		// @Incomplete: 
		// @Incomplete: 
		// @Incomplete: 
		// @Incomplete: 
		// @Incomplete: 
		// @Incomplete: 


		return sb.toString();
	}

	private String replace_all_ops(String str) {
		StringBuffer sb = new StringBuffer(str);

		// First, replacing ^ with ->
		while(true) {
			int indexOf_pointer_access = sb.indexOf("^");
			
			if(indexOf_pointer_access == -1)
				break;
			else if(!Util.is_index_inside_quotes(indexOf_pointer_access, sa.quotes_range_indices))
			sb = sb.replace(indexOf_pointer_access, indexOf_pointer_access + 1, "->");
		}

		while(true) {
			int indexOf_left_shift = sb.indexOf("<<<");
			int indexOf_right_shift = sb.indexOf(">>>");
			int indexOf_pointer = sb.indexOf(">>");
			int indexOf_pointer_deref = sb.indexOf("<<");
			int indexOf_xor = sb.indexOf("^^");
			boolean found_one = false;

			if(indexOf_left_shift != -1 && !Util.is_index_inside_quotes(indexOf_left_shift, sa.quotes_range_indices)) {
				sb = sb.replace(indexOf_left_shift, indexOf_left_shift + 3, "<<");
				found_one = true;
			}
			if(indexOf_right_shift != -1 && !Util.is_index_inside_quotes(indexOf_right_shift, sa.quotes_range_indices)) {
				sb = sb.replace(indexOf_right_shift, indexOf_right_shift + 3, "<<");
				found_one = true;
			}
			if(indexOf_pointer != -1 && !Util.is_index_inside_quotes(indexOf_pointer, sa.quotes_range_indices)) {
				sb = sb.replace(indexOf_pointer, indexOf_pointer + 2, "&");
				found_one = true;
			}
			if(indexOf_pointer_deref != -1 && !Util.is_index_inside_quotes(indexOf_pointer_deref, sa.quotes_range_indices)) {
				sb = sb.replace(indexOf_pointer_deref, indexOf_pointer_deref + 2, "*");
				found_one = true;
			}
			if(indexOf_xor != -1 && !Util.is_index_inside_quotes(indexOf_xor, sa.quotes_range_indices)) {
				sb = sb.replace(indexOf_xor, indexOf_xor + 2, "^");
				found_one = true;
			}

			if(!found_one)
				break;
		}

		return sb.toString();
	}
}
