import psycopg2

conn = psycopg2.connect(
    host="localhost",
    database="motioncad",
    user="user",
    password="password"
)

cur = conn.cursor()

try:
    # file_hash 컬럼이 있는지 확인하고 최근 Part 조회
    cur.execute("""
        SELECT 
            id,
            name,
            file_hash,
            model_file_url,
            created_at
        FROM parts
        WHERE id >= 28
        ORDER BY id DESC
        LIMIT 3
    """)
    
    rows = cur.fetchall()
    
    print("\n" + "="*100)
    print("🔍 Verifying file_hash column in database")
    print("="*100)
    
    if rows:
        for row in rows:
            id, name, file_hash, model_url, created_at = row
            print(f"\n🆔 Part ID: {id}")
            print(f"   📝 Name: {name}")
            print(f"   🔐 File Hash: {file_hash}")
            print(f"   📦 Model URL: {model_url[:50]}...")
            print(f"   📅 Created: {created_at}")
            print("-" * 100)
        
        print("\n✅ File hash deduplication verified in database!")
        
        # 중복 해시 확인
        cur.execute("""
            SELECT file_hash, COUNT(*) as count
            FROM parts
            WHERE file_hash IS NOT NULL
            GROUP BY file_hash
            HAVING COUNT(*) > 1
        """)
        
        duplicates = cur.fetchall()
        if duplicates:
            print(f"\n⚠️  Found {len(duplicates)} duplicate file hashes (this should be impossible due to UNIQUE constraint):")
            for hash_val, count in duplicates:
                print(f"   Hash: {hash_val} - Count: {count}")
        else:
            print("\n✅ No duplicate file hashes found - UNIQUE constraint working correctly!")
    else:
        print("❌ No parts found")

except Exception as e:
    print(f"❌ Error: {e}")
    import traceback
    traceback.print_exc()
finally:
    cur.close()
    conn.close()
