import psycopg2

# PostgreSQL 연결
conn = psycopg2.connect(
    host="localhost",
    database="motioncad",
    user="user",
    password="password"
)

cur = conn.cursor()

try:
    # 최근 업로드된 Parts 조회
    cur.execute("""
        SELECT 
            p.id,
            p.name,
            p.type,
            p.category,
            p.is_ai_generated,
            p.model_file_url,
            p.thumbnail_url,
            u.nickname as creator,
            p.created_at
        FROM parts p
        LEFT JOIN users u ON p.creator_id = u.id
        ORDER BY p.id DESC
        LIMIT 5
    """)
    
    rows = cur.fetchall()
    
    if rows:
        print("\n" + "="*100)
        print("📊 Recently Uploaded Parts")
        print("="*100)
        
        for row in rows:
            id, name, type_, category, is_ai, model_url, thumbnail_url, creator, created_at = row
            
            print(f"\n🆔 Part ID: {id}")
            print(f"   📝 Name: {name}")
            print(f"   🏷️  Type: {type_} | Category: {category}")
            print(f"   🤖 AI Generated: {'Yes ✅' if is_ai else 'No ❌'}")
            print(f"   📦 Model: {model_url[:50]}..." if len(model_url or '') > 50 else f"   📦 Model: {model_url}")
            print(f"   🖼️  Thumbnail: {thumbnail_url[:50]}..." if thumbnail_url and len(thumbnail_url) > 50 else f"   🖼️  Thumbnail: {thumbnail_url or 'None'}")
            print(f"   👤 Creator: {creator}")
            print(f"   📅 Created: {created_at}")
            print("-" * 100)
        
        print("\n✅ Database verification successful!")
        
    else:
        print("❌ No parts found in database")

except Exception as e:
    print(f"❌ Error: {e}")
finally:
    cur.close()
    conn.close()
