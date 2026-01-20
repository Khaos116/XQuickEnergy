import re
import os

def parse_java_methods(file_path):
    """提取 public static String 方法，并记录参数"""
    methods = {}
    if not os.path.exists(file_path):
        return {}

    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
        # 移除注释
        content = re.sub(r'//.*', '', content)
        content = re.sub(r'/\*.*?\*/', '', content, flags=re.DOTALL)

        # 匹配方法名和参数
        pattern = re.compile(r'public\s+static\s+String\s+(\w+)\s*\(([^)]*)\)')

        for match in pattern.finditer(content):
            method_name = match.group(1)
            params_block = match.group(2).strip()

            # 提取参数名
            if not params_block:
                params = []
            else:
                raw_parts = params_block.split(',')
                params = [p.strip().split()[-1] for p in raw_parts if p.strip()]
                params = [re.sub(r'@\w+(\([^)]*\))?', '', p).strip() for p in params]

            methods[method_name] = params

    return methods

def run_comparison(file_tk, file_gr):
    data_tk = parse_java_methods(file_tk)
    data_gr = parse_java_methods(file_gr)

    # --- 功能 1：原有的参数名差异检测 ---
    print(f"\n[检测 1] 方法名相同，但【参数名称】不同：")
    print("-" * 100)
    common_names = set(data_tk.keys()) & set(data_gr.keys())
    found1 = False
    for name in common_names:
        p1 = data_tk[name]
        p2 = data_gr[name]
        if p1 != p2 and len(p1) == len(p2):
            print(f"方法名: {name:<30} | TK: {p1} | GR: {p2}")
            found1 = True
    if not found1: print("未发现参数名不同的同名方法。")

    # --- 功能 2：专门查找带 "New" 的方法 ---
    print(f"\n[检测 2] 包含 'New' 关键字的方法汇总：")
    print("-" * 100)
    print(f"{'来源文件':<15} | {'方法名称':<40} | {'参数列表'}")
    print("-" * 100)

    found2 = False
    # 检查 TK 文件
    for name, params in data_tk.items():
        if 'new' in name.lower():
            print(f"{'TK 文件':<15} | {name:<40} | {params}")
            found2 = True

    # 检查 GR 文件
    for name, params in data_gr.items():
        if 'new' in name.lower():
            print(f"{'GR 文件':<15} | {name:<40} | {params}")
            found2 = True

    if not found2:
        print("未在两个文件中发现带 'New' 的方法。")

if __name__ == "__main__":
    current_dir = os.path.dirname(os.path.abspath(__file__))
    target_rel_path = "../app/src/main/java/io/github/lazyimmortal/sesame/model/task/antFarm"
    base_path = os.path.normpath(os.path.join(current_dir, target_rel_path))

    file_tk = os.path.join(base_path, "AntFarmRpcCallTK.java")
    file_gr = os.path.join(base_path, "AntFarmRpcCallGR.java")

    run_comparison(file_tk, file_gr)