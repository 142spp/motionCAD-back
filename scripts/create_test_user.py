import psycopg2
from datetime import datetime

# PostgreSQL 연결
conn = psycopg2.connect(
    host="localhost",
    database="motioncad",
    user="user",
    password="password"
)

cur = conn.cursor()

try:
    # 테스트 유저 생성
    cur.execute("""
        INSERT INTO users (email, password_hash, nickname, created_at, updated_at)
        VALUES (%s, %s, %s, %s, %s)
        ON CONFLICT (email) DO NOTHING
        RETURNING id;
    """, (
        'test@example.com',
        '$2a$10$dummyhashedpassword',  # BCrypt 해시 형식
        'Test User',
        datetime.now(),
        datetime.now()
    ))
    
    result = cur.fetchone()
    conn.commit()
    
    if result:
        print(f"✅ Test user created with ID: {result[0]}")
    else:
        # 이미 존재하는 경우
        cur.execute("SELECT id, email, nickname FROM users WHERE email = %s", ('test@example.com',))
        user = cur.fetchone()
        print(f"ℹ️  User already exists: ID={user[0]}, Email={user[1]}, Nickname={user[2]}")

except Exception as e:
    print(f"❌ Error: {e}")
    conn.rollback()
finally:
    cur.close()
    conn.close()
