import psycopg2

conn = psycopg2.connect(
    host="localhost",
    database="motioncad",
    user="user",
    password="password"
)

cur = conn.cursor()

try:
    # Check parts table
    cur.execute("""
        SELECT 
            id,
            name,
            file_hash,
            is_ai_generated
        FROM parts
        ORDER BY id
    """)
    
    rows = cur.fetchall()
    
    print(f"✅ Total Parts in DB: {len(rows)}\n")
    print("="*100)
    
    for row in rows:
        id, name, file_hash, is_ai = row
        hash_display = file_hash[:8] + "..." if file_hash else "None"
        print(f"#{id:2d} | {name[:50]:50s} | Hash: {hash_display:12s} | AI: {is_ai}")
    
    print("="*100)
    
    # Check file_hash uniqueness
    cur.execute("""
        SELECT COUNT(DISTINCT file_hash) as unique_hashes,
               COUNT(*) as total_parts
        FROM parts
        WHERE file_hash IS NOT NULL
    """)
    
    result = cur.fetchone()
    if result:
        unique, total = result
        print(f"\n📊 Statistics:")
        print(f"   Total Parts with Hash: {total}")
        print(f"   Unique Hashes: {unique}")
        if total > unique:
            print(f"   ⚠️  Duplicates: {total - unique}")
        else:
            print(f"   ✅ No duplicates!")
    else:
        print("\n⚠️  No parts with file_hash found")

except Exception as e:
    print(f"❌ Error: {e}")
finally:
    cur.close()
    conn.close()
