import requests
import json

# API 엔드포인트
url = "http://localhost:8080/api/parts"

# Part 목록 조회
params = {
    'type': 'OBJECT',
    'sort': 'latest'
}

print("📡 Fetching parts from /api/parts...")

try:
    response = requests.get(url, params=params)
    
    if response.status_code == 200:
        parts = response.json()
        
        print(f"\n✅ Found {len(parts)} parts\n")
        print("="*100)
        
        # 최근 업로드한 Part 2개만 표시
        for part in parts[:2]:
            print(f"\n🆔 Part ID: {part['id']}")
            print(f"📝 Name: {part['name']}")
            print(f"🤖 AI Generated: {part['isAiGenerated']}")
            print(f"\n📦 Model URL (Presigned):")
            print(f"   {part['modelFileUrl'][:100]}...")
            
            if part.get('thumbnailUrl'):
                print(f"\n🖼️  Thumbnail URL (Presigned):")
                print(f"   {part['thumbnailUrl'][:100]}...")
                print(f"\n   👉 This is a real, accessible URL valid for 5 minutes!")
            else:
                print(f"\n🖼️  Thumbnail: None")
            
            print("-" * 100)
    else:
        print(f"❌ Failed: {response.status_code}")
        print(response.text)
        
except Exception as e:
    print(f"❌ Error: {e}")
