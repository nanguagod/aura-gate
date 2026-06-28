#!/usr/bin/env python3
"""Apply SQL file to MySQL."""
import os, sys

sql_file = os.path.join(os.path.dirname(__file__), 'sql', 'auragate_rbac.sql')
with open(sql_file) as f:
    statements = f.read().split(';')

import pymysql
conn = pymysql.connect(
    host='localhost',
    user=os.environ.get('DB_USER', 'root'),
    password=os.environ.get('DB_PASS', '123456')
)
cur = conn.cursor()
for s in statements:
    s = s.strip()
    if s:
        try:
            cur.execute(s)
        except Exception as e:
            print(f'SKIP: {e}')
conn.commit()
cur.close()
conn.close()
print('done')