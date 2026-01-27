import requests

# 테스트할 파일
model_file_path = r"temp_downloads\dd0a83c82a1e4a85b0770a02eb4073f1.glb"

# API 엔드포인트
url = "http://localhost:8080/api/parts"

# multipart/form-data 요청
files = {
    'modelFile': ('test-model.glb', open(model_file_path, 'rb'), 'model/gltf-binary')
}

data = {
    'name': 'Test Chair Model',
    'type': 'OBJECT',
    'category': 'FURNITURE_HOME',
    'isAiGenerated': 'false'
}

print("📤 Uploading part to /api/parts...")
print(f"📁 File: {model_file_path}")
print(f"📊 Data: {data}\n")

try:
    response = requests.post(url, files=files, data=data)
    
    print(f"✅ Status Code: {response.status_code}")
    print(f"📋 Response: {response.text}")
    
    if response.status_code == 201:
        part_id = response.json()
        print(f"\n🎉 Success! Part ID: {part_id}")
    else:
        print(f"\n❌ Failed!")
        
except Exception as e:
    print(f"❌ Error: {e}")
