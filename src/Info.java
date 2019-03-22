import java.util.List;
import java.util.ArrayList;

enum InfoType {
	STRUCT, ENUM, FUNCTION, VAR_DECL, VAR_ASSIGN, EXPRESSION, IF, ELSE_IF, ELSE, WHILE, NONE, OTHER, RETURN, USE
}

public abstract class Info {
	InfoType info_type = InfoType.NONE;
	int id = -1;
	int line_number = -1;

	abstract String get_info();
}

class IfInfo extends Info {
	String exp;
	List<Info> infos = new ArrayList<>();

	IfInfo(String exp, int line_number, List<Info> infos, int id) {
		this.exp = exp;
		this.line_number = line_number;
		this.infos = infos;
		this.id = id;
		info_type = InfoType.IF;
	}

	String get_info() {
		return "if " + exp;
	}
}

class ElseInfo extends Info {
	List<Info> infos = new ArrayList<>();

	ElseInfo(List<Info> infos, int id, int line_number) {
		this.infos = infos;
		this.id = id;
		this.line_number = line_number;
		info_type = InfoType.ELSE;
	}

	String get_info() {
		return "else";
	}
}

class ElseIfInfo extends Info {
	String exp;
	List<Info> infos = new ArrayList<>();

	ElseIfInfo(String exp, int line_number, List<Info> infos, int id) {
		this.exp = exp;
		this.line_number = line_number;
		this.infos = infos;
		this.id = id;
		info_type = InfoType.ELSE_IF;
	}

	String get_info() {
		return "else if " + exp;
	}
}

class ReturnInfo extends Info {
	String exp;

	ReturnInfo(String exp, int line_number, int id) {
		this.exp = exp;
		this.line_number = line_number;
		this.id = id;
		info_type = InfoType.RETURN;
	}

	String get_info() {
		return "return " + exp;
	}
}

class WhileInfo extends Info {
	String exp;
	List<Info> infos = new ArrayList<>();

	WhileInfo(String exp, int line_number, List<Info> infos, int id) {
		this.exp = exp;
		this.line_number = line_number;
		this.id = id;
		this.infos = infos;
		info_type = InfoType.WHILE;
	}

	String get_info() {
		return "while " + exp;
	}
}

class OtherInfo extends Info {
	String str;

	OtherInfo(String str, int line_number, int id) {
		this.str = str;
		this.line_number = line_number;
		this.id = id;
		info_type = InfoType.OTHER;
	}

	String get_info() {
		return str;
	}
}

class VarDeclInfo extends Info {
	String name;
	String type;
	String raw_value;
	
	VarDeclInfo(String name, String type, String raw_value, int line_number, int id) {
		this.name = name;
		this.type = type;
		this.raw_value = raw_value;
		this.line_number = line_number;
		this.id = id;
		info_type = InfoType.VAR_DECL;
	}

	String get_info() {
		return name + ": " + type + " = " + raw_value;
	}
}

class UseInfo extends Info {
	String filename;

	UseInfo(String filename, int line_number, int id) {
		this.filename = filename;
		this.line_number = line_number;
		this.id = id;
		info_type = InfoType.USE;
	}

	String get_info() {
		return "use \"" + filename + "\".";
	}
}

class VarAssignInfo extends Info {
	String var_name;
	String raw_value;

	VarAssignInfo(String var_name, String raw_value, int line_number, int id) {
		this.var_name = var_name;
		this.raw_value = raw_value;
		this.line_number = line_number;
		this.id = id;
		info_type = InfoType.VAR_ASSIGN;
	}

	String get_info() {
		return var_name + " = " + raw_value;
	}
}

class StructInfo extends Info {
	String name;
	List<VarDeclInfo> var_decl_infos = new ArrayList<>();

	StructInfo(String name, int line_number, List<VarDeclInfo> var_decl_infos, int id) {
		this.name = name;
		this.line_number = line_number;
		this.var_decl_infos = var_decl_infos;
		this.id = id;
		info_type = InfoType.STRUCT;
	}

	String get_info() {
		return name + "::struct";
	}
}

class EnumInfo extends Info {
	String name;
	List<String> values = new ArrayList<>();

	EnumInfo(String name, int line_number, List<String> values, int id) {
		this.name = name;
		this.line_number = line_number;
		this.id = id;
		this.values = values;
		info_type = InfoType.ENUM;
	}

	String get_info() {
		return name + "::enum";
	}
}

class FunctionInfo extends Info {
	String name;
	String return_type;
	List<VarDeclInfo> var_args;
	List<Info> infos = new ArrayList<>();

	FunctionInfo(String name, String return_type, List<VarDeclInfo> var_args, List<Info> infos, int signature_line_number, int id) {
		this.name = name;
		this.return_type = return_type;
		this.infos = infos;
		this.line_number = signature_line_number;
		this.var_args = var_args;
		this.id = id;
		info_type = InfoType.FUNCTION;
	}

	String get_info() {
		return "func " + name + " .... ";
	}
}

class ExpInfo extends Info {
	String exp;

	ExpInfo(String exp, int line_number, int id) {
		this.exp = exp;
		this.id = id;
		this.line_number = line_number;
	}

	String get_info() {
		return exp;
	}
}
