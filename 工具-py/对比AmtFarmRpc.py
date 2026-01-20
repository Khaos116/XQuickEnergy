import re
import os

def parse_java_methods(file_path):
    methods = {}
    if not os.path.exists(file_path):
        print(f"错误: 找不到文件 {file_path}")
        return methods

    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
        # 1. 移除注释
        content = re.sub(r'//.*', '', content)
        content = re.sub(r'/\*.*?\*/', '', content, flags=re.DOTALL)

        # 2. 严格匹配模式：必须是 public static String [方法名]([参数])
        # \s+ 处理可能的换行或多个空格
        pattern = re.compile(r'public\s+static\s+String\s+(\w+)\s*\(([^)]*)\)')

        for match in pattern.finditer(content):
            method_name = match.group(1)
            params_block = match.group(2).strip()

            # 3. 解析参数名
            if not params_block:
                params = []
            else:
                raw_parts = params_block.split(',')
                params = []
                for p in raw_parts:
                    p = p.strip()
                    if not p: continue
                    # 移除注解
                    p = re.sub(r'@\w+(\([^)]*\))?', '', p).strip()
                    # 提取最后一个单词作为变量名
                    items = p.split()
                    if items:
                        params.append(items[-1])

            # 存储：(方法名, 参数个数) -> 参数名列表
            methods[(method_name, len(params))] = params

    return methods

def compare_files(file1, file2):
    data1 = parse_java_methods(file1)
    data2 = parse_java_methods(file2)

    # 提取所有方法名和个数的交集
    common_keys = set(data1.keys()) & set(data2.keys())

    print(f"\n[对比结果] 只针对 public static String 方法")
    print("-" * 110)
    print(f"{'方法名称':<35} | {'参数数':<6} | {'TK 参数列表':<35} | {'GR 参数列表'}")
    print("-" * 110)

    found_diff = False
    # 按照方法名排序显示，方便查看
    for key in sorted(common_keys):
        p1 = data1[key]
        p2 = data2[key]

        # 对比逻辑：方法名一致，个数一致，但参数名数组不一致
        if p1 != p2:
            found_diff = True
            print(f"{key[0]:<35} | {key[1]:<8} | {str(p1):<37} | {str(p2)}")

    if not found_diff:
        print("未发现符合条件（方法名/个数一致，但参数名不同）的方法。")

if __name__ == "__main__":
    # 路径处理
    current_dir = os.path.dirname(os.path.abspath(__file__))
    target_rel_path = "../app/src/main/java/io/github/lazyimmortal/sesame/model/task/antFarm"
    base_path = os.path.normpath(os.path.join(current_dir, target_rel_path))

    file_tk = os.path.join(base_path, "AntFarmRpcCallTK.java")
    file_gr = os.path.join(base_path, "AntFarmRpcCallGR.java")

    compare_files(file_tk, file_gr)