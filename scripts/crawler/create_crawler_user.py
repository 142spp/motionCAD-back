import psycopg2

conn = psycopg2.connect(
    host="localhost",
    database="motioncad",
    user="user",
    password="password"
)

cur = conn.cursor()

try:
    # Create crawler user with ID 1
    cur.execute("""
        INSERT INTO users (id, email, password_hash, nickname, created_at, updated_at)
        VALUES (1, 'crawler@motioncad.com', 'dummy_hash', 'Crawler', NOW(), NOW())
        ON CONFLICT (id) DO NOTHING
    """)
    
    conn.commit()
    
    print("✅ Crawler user created successfully!")
    print("   ID: 1")
    print("   Nickname: Crawler")
    print("   Email: crawler@motioncad.com")

except Exception as e:
    print(f"❌ Error: {e}")
    conn.rollback()
finally:
    cur.close()
    conn.close()
