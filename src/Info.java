import java.util.List;
import java.util.ArrayList;

enum InfoType {
	STRUCT, ENUM, FUNCTION, VAR_DECL, VAR_ASSIGN, EXPRESSION, IF, ELSE_IF, ELSE, WHILE, NONE, OTHER, RETURN, USE
}

public abstract class Info {
	InfoType info_type;
	int id = -1;
}

class IfInfo extends Info {
	String exp;
	int exp_line_number = -1;
	List<Info> infos = new ArrayList<>();

	IfInfo(String exp, int exp_line_number, List<Info> infos, int id) {
		this.exp = exp;
		this.exp_line_number = exp_line_number;
		this.infos = infos;
		this.id = id;
		info_type = InfoType.IF;
	}
}

class ElseInfo extends Info {
	List<Info> infos = new ArrayList<>();

	ElseInfo(List<Info> infos, int id) {
		this.infos = infos;
		this.id = id;
		info_type = InfoType.ELSE;
	}
}

class ElseIfInfo extends Info {
	String exp;
	int exp_line_number = -1;
	List<Info> infos = new ArrayList<>();

	ElseIfInfo(String exp, int exp_line_number, List<Info> infos, int id) {
		this.exp = exp;
		this.exp_line_number = exp_line_number;
		this.infos = infos;
		this.id = id;
		info_type = InfoType.ELSE_IF;
	}
}

class ReturnInfo extends Info {
	String exp;
	int line_number = -1;

	ReturnInfo(String exp, int line_number, int id) {
		this.exp = exp;
		this.line_number = line_number;
		this.id = id;
		info_type = InfoType.RETURN;
	}
}

class WhileInfo extends Info {
	String exp;
	int exp_line_number = -1;
	List<Info> infos = new ArrayList<>();

	WhileInfo(String exp, int exp_line_number, List<Info> infos, int id) {
		this.exp = exp;
		this.exp_line_number = exp_line_number;
		this.id = id;
		this.infos = infos;
		info_type = InfoType.WHILE;
	}
}

class OtherInfo extends Info {
	String str;
	int line_number = -1;

	OtherInfo(String str, int line_number, int id) {
		this.str = str;
		this.line_number = line_number;
		this.id = id;
		info_type = InfoType.OTHER;
	}
}

class VarDeclInfo extends Info {
	String name;
	String type;
	String raw_value;
	int line_number = -1;
	
	VarDeclInfo(String name, String type, String raw_value, int line_number, int id) {
		this.name = name;
		this.type = type;
		this.raw_value = raw_value;
		this.line_number = line_number;
		this.id = id;
		info_type = InfoType.VAR_DECL;
	}
}

class UseInfo extends Info {
	String filename;
	int line_number = -1;

	UseInfo(String filename, int line_number, int id) {
		this.filename = filename;
		this.line_number = line_number;
		this.id = id;
		info_type = InfoType.USE;
	}
}

class VarAssignInfo extends Info {
	String var_name;
	String raw_value;
	int line_number = -1;

	VarAssignInfo(String var_name, String raw_value, int line_number, int id) {
		this.var_name = var_name;
		this.raw_value = raw_value;
		this.line_number = line_number;
		this.id = id;
		info_type = InfoType.VAR_ASSIGN;
	}
}

class StructInfo extends Info {
	String name;
	int name_line_number = -1;
	List<VarDeclInfo> var_decl_infos = new ArrayList<>();

	StructInfo(String name, int name_line_number, List<VarDeclInfo> var_decl_infos, int id) {
		this.name = name;
		this.name_line_number = name_line_number;
		this.var_decl_infos = var_decl_infos;
		this.id = id;
		info_type = InfoType.STRUCT;
	}
}

class EnumInfo extends Info {
	String name;
	int name_line_number = -1;
	List<String> values = new ArrayList<>();

	EnumInfo(String name, int name_line_number, List<String> values, int id) {
		this.name = name;
		this.name_line_number = name_line_number;
		this.id = id;
		this.values = values;
		info_type = InfoType.ENUM;
	}
}

class FunctionInfo extends Info {
	String name;
	String return_type;
	List<VarDeclInfo> var_args;
	List<Info> infos = new ArrayList<>();
	int signature_line_number = -1;
	String scope_name;
	List<String> scope_names;

	FunctionInfo(String name, String return_type, List<VarDeclInfo> var_args, List<Info> infos, int signature_line_number, int id) {
		this.name = name;
		this.return_type = return_type;
		this.infos = infos;
		this.signature_line_number = signature_line_number;
		this.var_args = var_args;
		this.id = id;
		info_type = InfoType.FUNCTION;

		scope_name = name + "@" + return_type + "@";
		for(int i = 0; i < var_args.size(); ++i)
			scope_name += var_args.get(i).type;

		scope_names = new ArrayList<>();
		// System.out.println("scope_name: " + scope_name);
	}
}

class ExpInfo extends Info {
	String exp;
	int line_number = -1;

	ExpInfo(String exp, int line_number, int id) {
		this.exp = exp;
		this.id = id;
		this.line_number = line_number;
	}
}
