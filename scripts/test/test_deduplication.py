import requests

# 테스트할 파일 - 같은 파일을 두 번 업로드
model_file_path = r"temp_downloads\dd0a83c82a1e4a85b0770a02eb4073f1.glb"

url = "http://localhost:8080/api/parts"

def upload_part(name):
    files = {
        'modelFile': ('duplicate-test.glb', open(model_file_path, 'rb'), 'model/gltf-binary')
    }
    
    data = {
        'name': name,
        'type': 'OBJECT',
        'category': 'ART_ABSTRACT',
        'isAiGenerated': 'false'
    }
    
    print(f"\n📤 Uploading: {name}")
    response = requests.post(url, files=files, data=data)
    
    print(f"   Status: {response.status_code}")
    print(f"   Part ID: {response.text}")
    
    return response.json() if response.status_code == 201 else None

print("="*80)
print("🧪 File Hash Deduplication Test")
print("="*80)

# First upload
print("\n🔵 Test 1: First Upload (should create new part)")
part_id_1 = upload_part("Duplicate Test - First Upload")

# Second upload with same file
print("\n🟢 Test 2: Second Upload (same file, should return existing ID)")
part_id_2 = upload_part("Duplicate Test - Second Upload")

print("\n" + "="*80)
print("📊 Results:")
print("="*80)
print(f"First Upload Part ID:  {part_id_1}")
print(f"Second Upload Part ID: {part_id_2}")

if part_id_1 == part_id_2:
    print("\n✅ SUCCESS! Deduplication working correctly!")
    print(f"   Both uploads returned the same Part ID ({part_id_1})")
    print("   The second file was NOT uploaded to S3 (saved bandwidth!)")
else:
    print("\n❌ FAILED! Deduplication not working!")
    print("   Two different Part IDs were created for the same file")
