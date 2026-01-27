import psycopg2

conn = psycopg2.connect(
    host="localhost",
    database="motioncad",
    user="user",
    password="password"
)

cur = conn.cursor()

try:
    # sourceId가 null이 아닌 Parts의 sourceId 추출
    cur.execute("""
        SELECT DISTINCT source_id 
        FROM parts 
        WHERE source_id IS NOT NULL
        ORDER BY source_id
    """)
    
    source_ids = [row[0] for row in cur.fetchall()]
    
    # source_archive.txt에 저장
    with open('scripts/crawler/source_archive.txt', 'w') as f:
        for source_id in source_ids:
            f.write(source_id + '\n')
    
    print(f"✅ Exported {len(source_ids)} source IDs to source_archive.txt")
    print(f"📁 Location: scripts/crawler/source_archive.txt")
    
    if source_ids:
        print("\nFirst 5 entries:")
        for sid in source_ids[:5]:
            print(f"  - {sid}")

except Exception as e:
    print(f"❌ Error: {e}")
    
    # source_id 컬럼이 없으면 빈 파일 생성
    if "column" in str(e).lower() and "source_id" in str(e).lower():
        print("\nℹ️  source_id column doesn't exist (already migrated to file_hash)")
        print("   Creating empty source_archive.txt")
        with open('scripts/crawler/source_archive.txt', 'w') as f:
            f.write("# Archived source IDs from old schema\n")
        print("✅ Created empty archive file")
finally:
    cur.close()
    conn.close()
