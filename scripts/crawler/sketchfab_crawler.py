import requests
import os
import json
import concurrent.futures
import threading
from dotenv import load_dotenv
load_dotenv()

# Configuration
SKETCHFAB_API_TOKEN = os.getenv("SKETCHFAB_API_TOKEN") # Get yours at https://sketchfab.com/settings/password
BACKEND_URL = "http://localhost:8080/api/assets/aggregate"
CHECK_DUPLICATE_URL = "http://localhost:8080/api/assets/check-duplicate"

print_lock = threading.Lock()

def safe_print(*args, **kwargs):
    """Thread-safe print with thread identification."""
    thread_name = threading.current_thread().name
    if thread_name == "MainThread":
        prefix = "[Main]"
    else:
        # Shorten "ThreadPoolExecutor-0_0" to "Thread-0"
        prefix = f"[{thread_name.split('_')[-1]}]"
    
    with print_lock:
        print(f"{prefix}", *args, **kwargs)

def check_duplicate(source_id):
    """Check if model with given source_id already exists in backend."""
    try:
        response = requests.get(f"{CHECK_DUPLICATE_URL}/{source_id}")
        if response.status_code == 200:
            return response.json() # Returns true/false
        return False
    except Exception as e:
        safe_print(f"[Warning] Could not check duplicate for {source_id}: {e}")
        return False

def search_sketchfab(query, count=5):
    """Search for downloadable models on Sketchfab."""
    url = f"https://api.sketchfab.com/v3/search?type=models&downloadable=true&q={query}&count={count}"
    response = requests.get(url)
    response.raise_for_status()
    return response.json()['results']

def download_model(model_data):
    """Download model file from Sketchfab."""
    model_uid = model_data['uid']
    model_name = "".join(c for c in model_data['name'] if c.isalnum() or c in (' ', '_')).rstrip()
    
    safe_print(f"Fetching download URL: {model_name}...")
    
    download_url = f"https://api.sketchfab.com/v3/models/{model_uid}/download"
    headers = {"Authorization": f"Token {SKETCHFAB_API_TOKEN}"}
    
    try:
        res = requests.get(download_url, headers=headers)
        res.raise_for_status()
        download_data = res.json()
        
        # Sketchfab usually provides 'gltf' which is a ZIP containing GLB or GLTF+textures
        # Some newer models might have 'glb' directly.
        format_to_download = None
        if 'glb' in download_data:
            format_to_download = 'glb'
        elif 'gltf' in download_data:
            format_to_download = 'gltf'
        
        if not format_to_download:
            safe_print(f"No suitable 3D format for {model_name}. Available: {list(download_data.keys())}")
            return
        
        file_info = download_data[format_to_download]
        file_url = file_info['url']
        file_size = file_info.get('size', 0)
        
        safe_print(f"Downloading {format_to_download} ({file_size / 1024 / 1024:.2f} MB)...")
        
        os.makedirs("temp_downloads", exist_ok=True)
        ext = "glb" if format_to_download == "glb" else "zip"
        file_path = f"temp_downloads/{model_uid}.{ext}"
        
        file_res = requests.get(file_url, stream=True, timeout=30)
        file_res.raise_for_status()
        
        downloaded = 0
        with open(file_path, 'wb') as f:
            for chunk in file_res.iter_content(chunk_size=65536):
                if chunk:
                    f.write(chunk)
                    downloaded += len(chunk)
        
        safe_print(f"[Success] Saved: {file_path}")
        return file_path

    except Exception as e:
        safe_print(f"[Error] Download failed for {model_name}: {e}")
        return None

import zipfile
import shutil

def upload_to_backend(file_path, model_data):
    """Upload the downloaded file to Spring Boot backend."""
    model_name = model_data['name']
    safe_print(f"Uploading {model_name} to backend...")
    
    try:
        with open(file_path, 'rb') as f:
            files = {
                'modelFile': (os.path.basename(file_path), f, 'model/gltf-binary')
            }
            # Thumbnail is optional in our controller, so we skip it for now
            data = {
                'name': model_name,
                'type': 'OBJECT',
                'category': 'ART_ABSTRACT',
                'description': model_data.get('description', ''),
                'sourceId': model_data['uid']
            }
            
            response = requests.post(BACKEND_URL, data=data, files=files, timeout=60)
            if response.status_code == 200:
                safe_print(f"[Success] Registered Part ID: {response.text}")
                return True
            else:
                safe_print(f"[Error] Upload failed: {response.status_code} - {response.text}")
                return False
    except Exception as e:
        safe_print(f"[Error] Upload failed: {e}")
        return False

def process_downloaded_file(file_path, model_uid):
    """If the file is a zip, extract it and find the .glb file. Otherwise return path."""
    if file_path.endswith(".zip"):
        safe_print(f"Extracting ZIP for {model_uid}...")
        extract_dir = f"temp_downloads/{model_uid}_extracted"
        os.makedirs(extract_dir, exist_ok=True)
        
        with zipfile.ZipFile(file_path, 'r') as zip_ref:
            zip_ref.extractall(extract_dir)
        
        # Look for .glb file recursively
        for root, dirs, files in os.walk(extract_dir):
            for file in files:
                if file.lower().endswith(".glb"):
                    return os.path.join(root, file)
        
        safe_print(f"[Warning] No .glb file found in ZIP for {model_uid}")
        return None
    return file_path

def process_model(model):
    """Workflow for processing a single model: check duplicate, download, and upload."""
    uid = model['uid']
    name = model['name']
    
    safe_print(f"Processing: {name} ({uid})")
    if check_duplicate(uid):
        safe_print(f"[Skip] Model already exists: {uid}")
        return False

    saved_path = download_model(model)
    if saved_path:
        final_glb_path = process_downloaded_file(saved_path, uid)
        if final_glb_path:
            return upload_to_backend(final_glb_path, model)
    return False

if __name__ == "__main__":
    if not SKETCHFAB_API_TOKEN:
        print("Please set SKETCHFAB_API_TOKEN in .env file.")
    else:
        query = "low poly"
        safe_print(f"Searching for '{query}'...")
        # count is set in search_sketchfab call, or can be passed here
        results = search_sketchfab(query, count=100)
        
        safe_print(f"Found {len(results)} models. Starting parallel processing...")
        
        with concurrent.futures.ThreadPoolExecutor(max_workers=5) as executor:
            # Map the processing function to all models
            list(executor.map(process_model, results))
        
        safe_print("Pipeline execution complete.")
