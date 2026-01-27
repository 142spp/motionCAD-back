import requests
from PIL import Image
import io

# 테스트할 파일들
model_file_path = r"temp_downloads\be3cf507c06943809ad779a1a72a044d.glb"

# 간단한 썸네일 이미지 생성 (테스트용)
img = Image.new('RGB', (256, 256), color='lightblue')
img_bytes = io.BytesIO()
img.save(img_bytes, format='JPEG')
img_bytes.seek(0)

# API 엔드포인트
url = "http://localhost:8080/api/parts"

# multipart/form-data 요청
files = {
    'modelFile': ('ai-generated-model.glb', open(model_file_path, 'rb'), 'model/gltf-binary'),
    'thumbnailFile': ('thumbnail.jpg', img_bytes, 'image/jpeg')
}

data = {
    'name': 'AI Generated Abstract Art',
    'type': 'OBJECT',
    'category': 'ART_ABSTRACT',
    'isAiGenerated': 'true'  # AI 생성 플래그
}

print("📤 Uploading AI-generated part with thumbnail...")
print(f"📁 Model: {model_file_path}")
print(f"🖼️  Thumbnail: Generated 256x256 JPEG")
print(f"📊 Data: {data}\n")

try:
    response = requests.post(url, files=files, data=data)
    
    print(f"✅ Status Code: {response.status_code}")
    print(f"📋 Response: {response.text}")
    
    if response.status_code == 201:
        part_id = response.json()
        print(f"\n🎉 Success! Part ID: {part_id}")
        print(f"✨ This part is marked as AI-generated with a thumbnail!")
    else:
        print(f"\n❌ Failed!")
        
except Exception as e:
    print(f"❌ Error: {e}")
