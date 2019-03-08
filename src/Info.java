import java.util.List;
import java.util.ArrayList;

enum InfoType {
	STRUCT, ENUM, FUNCTION, VAR_DECL, VAR_ASSIGN, EXPRESSION, IF, ELSE_IF, ELSE, WHILE, NONE, OTHER, RETURN, USE
}

public abstract class Info {
	InfoType info_type;
}

class IfInfo extends Info {
	String exp;
	int exp_line_number = -1;
	List<Info> infos = new ArrayList<>();

	IfInfo(String exp, int exp_line_number, List<Info> infos) {
		this.exp = exp;
		this.exp_line_number = exp_line_number;
		this.infos = infos;
		info_type = InfoType.IF;
	}
}

class ElseInfo extends Info {
	List<Info> infos = new ArrayList<>();

	ElseInfo(List<Info> infos) {
		this.infos = infos;
		info_type = InfoType.ELSE;
	}
}

class ElseIfInfo extends Info {
	String exp;
	int exp_line_number = -1;
	List<Info> infos = new ArrayList<>();

	ElseIfInfo(String exp, int exp_line_number, List<Info> infos) {
		this.exp = exp;
		this.exp_line_number = exp_line_number;
		this.infos = infos;
		info_type = InfoType.ELSE_IF;
	}
}

class ReturnInfo extends Info {
	String exp;
	int line_number = -1;

	ReturnInfo(String exp, int line_number) {
		this.exp = exp;
		this.line_number = line_number;
		info_type = InfoType.RETURN;
	}
}

class WhileInfo extends Info {
	String exp;
	int exp_line_number = -1;
	List<Info> infos = new ArrayList<>();

	WhileInfo(String exp, int exp_line_number, List<Info> infos) {
		this.exp = exp;
		this.exp_line_number = exp_line_number;
		this.infos = infos;
		info_type = InfoType.WHILE;
	}
}

class OtherInfo extends Info {
	String str;
	int line_number = -1;

	OtherInfo(String str, int line_number) {
		this.str = str;
		this.line_number = line_number;
		info_type = InfoType.OTHER;
	}
}

class VarDeclInfo extends Info {
	String name;
	String type;
	String raw_value;
	int line_number = -1;
	// @Incomplete: What about scope ????
	
	VarDeclInfo(String name, String type, String raw_value, int line_number) {
		this.name = name;
		this.type = type;
		this.raw_value = raw_value;
		this.line_number = line_number;
		info_type = InfoType.VAR_DECL;
	}
}

class UseInfo extends Info {
	String filename;
	int line_number = -1;

	UseInfo(String filename, int line_number) {
		this.filename = filename;
		this.line_number = line_number;
		info_type = InfoType.USE;
	}
}

class VarAssignInfo extends Info {
	String var_name;
	String raw_value;
	int line_number = -1;

	VarAssignInfo(String var_name, String raw_value, int line_number) {
		this.var_name = var_name;
		this.raw_value = raw_value;
		this.line_number = line_number;
		info_type = InfoType.VAR_ASSIGN;
	}
}

class StructInfo extends Info {
	String name;
	int name_line_number = -1;
	List<VarDeclInfo> var_decl_infos = new ArrayList<>();

	StructInfo(String name, int name_line_number, List<VarDeclInfo> var_decl_infos) {
		this.name = name;
		this.name_line_number = name_line_number;
		this.var_decl_infos = var_decl_infos;
		info_type = InfoType.STRUCT;
	}
}

class EnumInfo extends Info {
	String name;
	int name_line_number = -1;
	List<String> values = new ArrayList<>();

	EnumInfo(String name, int name_line_number, List<String> values) {
		this.name = name;
		this.name_line_number = name_line_number;
		this.values = values;
		info_type = InfoType.ENUM;
	}
}

class FunctionInfo extends Info {
	String name;
	String return_type;
	List<String> var_names = new ArrayList<>();
	List<String> var_types = new ArrayList<>();
	List<Info> infos = new ArrayList<>();
	int signature_line_number = -1;

	FunctionInfo(String name, String return_type, List<String> var_names, List<String> var_types, List<Info> infos, int signature_line_number) {
		this.name = name;
		this.return_type = return_type;
		this.var_names = var_names;
		this.infos = infos;
		this.signature_line_number = signature_line_number;
		info_type = InfoType.FUNCTION;
	}
}

class ExpInfo extends Info {
	String exp;
	int line_number = -1;

	ExpInfo(String exp, int line_number) {
		this.exp = exp;
		this.line_number = line_number;
	}
}
