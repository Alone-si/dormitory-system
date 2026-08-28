#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成学生测试数据的 SQL 脚本
"""

# 密码 123456 的 BCrypt 加密值
PASSWORD_HASH = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmHNsZLQAqU2jRTC'

# 男生姓名
male_names = [
    '张三', '李四', '王五', '赵六', '孙七', '周八', '吴九', '郑十',
    '陈一', '刘二', '杨三', '黄四', '朱五', '林六', '何七', '郭八',
    '马九', '罗十', '梁一', '宋二', '唐三', '许四', '韩五', '冯六',
    '邓七', '曹八', '彭九', '曾十', '肖一', '田二', '董三', '袁四',
    '潘五', '于六', '蒋七', '蔡八', '余九', '杜十', '叶一', '程二',
    '苏三', '魏四', '吕五', '丁六', '任七', '沈八', '姚九', '卢十',
    '姜一', '崔二'
]

# 女生姓名
female_names = [
    '小红', '小芳', '小丽', '小华', '小燕', '小玲', '小梅', '小霞',
    '小娟', '小敏', '小静', '小雪', '小兰', '小慧', '小琴', '小云',
    '小萍', '小英', '小莉', '小珍', '小艳', '小凤', '小秀', '小娜',
    '小婷', '小洁', '小倩', '小琳', '小欣', '小丹', '小蓉', '小佳',
    '小薇', '小晶', '小颖', '小璐', '小瑶', '小婷', '小雯', '小月',
    '小菲', '小娇', '小妮', '小茜', '小媛', '小露', '小曼', '小诗',
    '小韵', '小悦'
]

sql_lines = []
sql_lines.append('-- 清空并重新插入学生数据')
sql_lines.append('START TRANSACTION;')
sql_lines.append('')
sql_lines.append('DELETE FROM users WHERE role = \\'STUDENT\\';')
sql_lines.append('UPDATE rooms SET occupied = 0, status = \\'AVAILABLE\\';')
sql_lines.append('')

# 生成男生数据
for i in range(50):
    student_id = f'2024{i+1:04d}'
    name = male_names[i]
    phone = f'138000{i+1:05d}'
    
    # 前30个分配班级
    if i < 30:
        class_name = f'2024级计算机{(i // 10) + 1}班'
    else:
        class_name = 'NULL'
    
    if class_name == 'NULL':
        sql = f"INSERT INTO users (username, display_name, password, name, student_id, role, gender, phone, status) VALUES ('{student_id}', '{student_id}', '{PASSWORD_HASH}', '{name}', '{student_id}', 'STUDENT', 'MALE', '{phone}', 'ACTIVE');"
    else:
        sql = f"INSERT INTO users (username, display_name, password, name, student_id, role, gender, phone, class_name, status) VALUES ('{student_id}', '{student_id}', '{PASSWORD_HASH}', '{name}', '{student_id}', 'STUDENT', 'MALE', '{phone}', '{class_name}', 'ACTIVE');"
    
    sql_lines.append(sql)

# 生成女生数据
for i in range(50):
    student_id = f'2024{i+51:04d}'
    name = female_names[i]
    phone = f'139000{i+1:05d}'
    
    # 前30个分配班级
    if i < 30:
        class_name = f'2024级软件工程{(i // 10) + 1}班'
    else:
        class_name = 'NULL'
    
    if class_name == 'NULL':
        sql = f"INSERT INTO users (username, display_name, password, name, student_id, role, gender, phone, status) VALUES ('{student_id}', '{student_id}', '{PASSWORD_HASH}', '{name}', '{student_id}', 'STUDENT', 'FEMALE', '{phone}', 'ACTIVE');"
    else:
        sql = f"INSERT INTO users (username, display_name, password, name, student_id, role, gender, phone, class_name, status) VALUES ('{student_id}', '{student_id}', '{PASSWORD_HASH}', '{name}', '{student_id}', 'STUDENT', 'FEMALE', '{phone}', '{class_name}', 'ACTIVE');"
    
    sql_lines.append(sql)

sql_lines.append('')
sql_lines.append('COMMIT;')
sql_lines.append('SELECT COUNT(*) AS total_students FROM users WHERE role = \\'STUDENT\\';')

# 写入文件
with open('../backend/sql/reset-and-insert-students-full.sql', 'w', encoding='utf-8') as f:
    f.write('\\n'.join(sql_lines))

print('SQL 脚本已生成: backend/sql/reset-and-insert-students-full.sql')
print('包含 100 个学生（男女各 50）')
