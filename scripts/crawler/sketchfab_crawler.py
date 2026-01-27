import requests
import os
import json
import hashlib
import concurrent.futures
import threading
from dotenv import load_dotenv
load_dotenv()

# Configuration
SKETCHFAB_API_TOKEN = os.getenv("SKETCHFAB_API_TOKEN") # Get yours at https://sketchfab.com/settings/password
BACKEND_URL = "http://localhost:8080/api/parts"  # Changed to use /api/parts for automatic hash calculation
SOURCE_ARCHIVE_FILE = "scripts/crawler/source_archive.txt"

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

# Load archived source IDs (already crawled)
def load_archived_sources():
    """Load source IDs from archive file to skip already crawled models."""
    if not os.path.exists(SOURCE_ARCHIVE_FILE):
        return set()
    with open(SOURCE_ARCHIVE_FILE, 'r', encoding='utf-8') as f:
        return set(line.strip() for line in f if line.strip() and not line.startswith('#'))

def save_to_archive(source_id):
    """Append a source ID to the archive file."""
    with open(SOURCE_ARCHIVE_FILE, 'a', encoding='utf-8') as f:
        f.write(source_id + '\n')

ARCHIVED_SOURCES = load_archived_sources()
safe_print(f"Loaded {len(ARCHIVED_SOURCES)} archived source IDs from {SOURCE_ARCHIVE_FILE}")

def calculate_file_hash(file_path):
    """Calculate MD5 hash of a file."""
    md5_hash = hashlib.md5()
    with open(file_path, 'rb') as f:
        for chunk in iter(lambda: f.read(8192), b""):
            md5_hash.update(chunk)
    return md5_hash.hexdigest()

def search_sketchfab(query, count=5, sort_by='likeCount'):
    """Search for downloadable models on Sketchfab, sorted by popularity."""
    url = f"https://api.sketchfab.com/v3/search?type=models&downloadable=true&q={query}&count={count}&sort_by={sort_by}"
    response = requests.get(url)
    response.raise_for_status()
    return response.json()['results']

def is_valid_glb(file_path):
    """Check if a GLB file is valid by verifying the magic number and minimum size."""
    try:
        if not os.path.exists(file_path):
            return False
        
        # GLB files must be at least 12 bytes (header)
        if os.path.getsize(file_path) < 12:
            return False
        
        # Check GLB magic number (should start with "glTF" in binary)
        with open(file_path, 'rb') as f:
            magic = f.read(4)
            # GLB magic number is 0x46546C67 which is "glTF" in ASCII
            if magic != b'glTF':
                return False
        
        return True
    except Exception:
        return False

def download_model(model_data):
    """Download model file from Sketchfab, or reuse existing file if already downloaded."""
    model_uid = model_data['uid']
    model_name = "".join(c for c in model_data['name'] if c.isalnum() or c in (' ', '_')).rstrip()
    
    # Check if file already exists locally and is valid
    models_dir = "temp_downloads/models"
    os.makedirs(models_dir, exist_ok=True)
    
    # Check for existing GLB file
    glb_file = f"{models_dir}/{model_uid}.glb"
    if os.path.exists(glb_file):
        if is_valid_glb(glb_file):
            safe_print(f"[Reuse] Valid GLB file exists: {glb_file}")
            return glb_file
        else:
            safe_print(f"[Warning] Corrupted GLB file, re-downloading: {glb_file}")
            os.remove(glb_file)
    
    # Check for existing ZIP file
    zip_file = f"{models_dir}/{model_uid}.zip"
    if os.path.exists(zip_file):
        # For ZIP files, just check if they're not empty and can be opened
        try:
            import zipfile
            with zipfile.ZipFile(zip_file, 'r') as z:
                if len(z.namelist()) > 0:
                    safe_print(f"[Reuse] Valid ZIP file exists: {zip_file}")
                    return zip_file
        except:
            safe_print(f"[Warning] Corrupted ZIP file, re-downloading: {zip_file}")
            os.remove(zip_file)
    
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
        
        ext = "glb" if format_to_download == "glb" else "zip"
        file_path = f"{models_dir}/{model_uid}.{ext}"
        
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

def download_thumbnail(model_data):
    """Download thumbnail image from Sketchfab."""
    model_uid = model_data['uid']
    model_name = model_data['name']
    
    # Create thumbnails directory
    thumbnails_dir = "temp_downloads/thumbnails"
    os.makedirs(thumbnails_dir, exist_ok=True)
    
    thumbnail_path = f"{thumbnails_dir}/{model_uid}.jpeg"
    
    # Check if thumbnail already exists
    if os.path.exists(thumbnail_path) and os.path.getsize(thumbnail_path) > 0:
        safe_print(f"[Reuse] Thumbnail exists: {thumbnail_path}")
        return thumbnail_path
    
    # Get thumbnail URL from model data
    # Sketchfab API provides thumbnails in different sizes
    thumbnails = model_data.get('thumbnails', {})
    thumbnail_url = None
    
    # Try to get the largest available thumbnail
    for size in ['images', 'large', 'medium', 'small']:
        if size in thumbnails and thumbnails[size]:
            if isinstance(thumbnails[size], list) and len(thumbnails[size]) > 0:
                thumbnail_url = thumbnails[size][0].get('url')
            elif isinstance(thumbnails[size], dict):
                thumbnail_url = thumbnails[size].get('url')
            if thumbnail_url:
                break
    
    if not thumbnail_url:
        safe_print(f"[Warning] No thumbnail URL found for {model_name}")
        return None
    
    try:
        safe_print(f"Downloading thumbnail for {model_name}...")
        response = requests.get(thumbnail_url, timeout=30)
        response.raise_for_status()
        
        with open(thumbnail_path, 'wb') as f:
            f.write(response.content)
        
        safe_print(f"[Success] Saved thumbnail: {thumbnail_path}")
        return thumbnail_path
    
    except Exception as e:
        safe_print(f"[Error] Thumbnail download failed for {model_name}: {e}")
        return None

def get_category_for_search(search_query):
    """Map search query to DB PartCategory enum."""
    category_map = {
        'furniture': 'FURNITURE_HOME',
        'character': 'CHARACTERS_CREATURES',
        'vehicle': 'CARS_VEHICLES',
        'building': 'ARCHITECTURE',
        'nature': 'NATURE_PLANTS',
        'weapon': 'WEAPONS_MILITARY',
        'food': 'FOOD_DRINK',
        'animal': 'ANIMALS_PETS',
        'tool': 'SCIENCE_TECHNOLOGY',
        'architecture': 'ARCHITECTURE',
        'car': 'CARS_VEHICLES',
        'plant': 'NATURE_PLANTS',
        'pet': 'ANIMALS_PETS',
        'electronics': 'ELECTRONICS_GADGETS',
        'gadget': 'ELECTRONICS_GADGETS',
        'fashion': 'FASHION_STYLE',
        'music': 'MUSIC',
        'sports': 'SPORTS_FITNESS',
        'people': 'PEOPLE',
        'culture': 'CULTURAL_HERITAGE_HISTORY',
        'history': 'CULTURAL_HERITAGE_HISTORY',
        'travel': 'PLACES_TRAVEL',
        'art': 'ART_ABSTRACT'
    }
    return category_map.get(search_query.lower(), 'ART_ABSTRACT')

def upload_to_backend(file_path, model_data, thumbnail_path=None, category='ART_ABSTRACT'):
    """Upload the downloaded file and thumbnail to Spring Boot backend."""
    model_name = model_data['name']
    safe_print(f"Uploading {model_name} to backend...")
    
    try:
        files = {}
        with open(file_path, 'rb') as model_file:
            files['modelFile'] = (os.path.basename(file_path), model_file.read(), 'model/gltf-binary')
            
            # Add thumbnail if available
            if thumbnail_path and os.path.exists(thumbnail_path):
                with open(thumbnail_path, 'rb') as thumb_file:
                    files['thumbnailFile'] = (os.path.basename(thumbnail_path), thumb_file.read(), 'image/jpeg')
            
            data = {
                'name': model_name,
                'type': 'OBJECT',
                'category': category,
                'isAiGenerated': 'false'  # Crawled assets are not AI generated
            }
            
            response = requests.post(BACKEND_URL, data=data, files=files, timeout=60)
            if response.status_code == 201:  # POST /api/parts returns 201 Created
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
        extract_dir = f"temp_downloads/models/{model_uid}_extracted"
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

def process_model(model, search_category='art'):
    """Workflow for processing a single model: check if exists locally, then upload."""
    uid = model['uid']
    name = model['name']
    category = get_category_for_search(search_category)
    
    safe_print(f"Processing: {name} ({uid})")
    
    # Check local archive first (fast, no upload needed)
    if uid in ARCHIVED_SOURCES:
        safe_print(f"[Skip] Already uploaded (found in archive): {uid}")
        return False
    
    # Check if model file exists locally
    models_dir = "temp_downloads/models"
    glb_file = f"{models_dir}/{uid}.glb"
    zip_file = f"{models_dir}/{uid}.zip"
    
    model_file_path = None
    if os.path.exists(glb_file) and is_valid_glb(glb_file):
        model_file_path = glb_file
        safe_print(f"[Found] Local GLB file: {glb_file}")
    elif os.path.exists(zip_file):
        # Process ZIP file to extract GLB
        final_glb = process_downloaded_file(zip_file, uid)
        if final_glb and os.path.exists(final_glb):
            model_file_path = final_glb
            safe_print(f"[Found] Extracted GLB from ZIP: {final_glb}")
    else:
        # Model not found locally, download it
        safe_print(f"[Download] Model not found locally, downloading for {uid}...")
        saved_path = download_model(model)
        if not saved_path:
            safe_print(f"[Error] Failed to download model for {uid}")
            return False
        
        final_glb = process_downloaded_file(saved_path, uid)
        if not final_glb or not os.path.exists(final_glb):
            safe_print(f"[Error] Failed to process downloaded model for {uid}")
            return False
        
        model_file_path = final_glb
        safe_print(f"[Downloaded] Successfully downloaded and processed: {model_file_path}")
    
    # Check for thumbnail
    thumbnail_path = f"temp_downloads/thumbnails/{uid}.jpeg"
    if not os.path.exists(thumbnail_path):
        safe_print(f"[Warning] Thumbnail not found, downloading...")
        thumbnail_path = download_thumbnail(model)
    else:
        safe_print(f"[Found] Local thumbnail: {thumbnail_path}")
    
    # Upload to backend with both model and thumbnail
    success = upload_to_backend(model_file_path, model, thumbnail_path, category)
    
    if success:
        # Save to archive so we don't upload this again
        save_to_archive(uid)
        safe_print(f"[Archived] {uid}")
    
    return success

def get_model_info_from_sketchfab(uid):
    """Fetch model information from Sketchfab API by UID."""
    url = f"https://api.sketchfab.com/v3/models/{uid}"
    try:
        response = requests.get(url, timeout=10)
        response.raise_for_status()
        return response.json()
    except Exception as e:
        safe_print(f"[Error] Failed to fetch model info for {uid}: {e}")
        return None

def download_thumbnails_for_existing_models():
    """Download thumbnails for all existing model files in temp_downloads/models/."""
    models_dir = "temp_downloads/models"
    
    if not os.path.exists(models_dir):
        safe_print(f"[Error] Models directory not found: {models_dir}")
        return
    
    # Get all GLB and ZIP files
    model_files = []
    for file in os.listdir(models_dir):
        if file.endswith('.glb') or file.endswith('.zip'):
            # Extract UID from filename (remove extension)
            uid = os.path.splitext(file)[0]
            # Skip extracted directories
            if not uid.endswith('_extracted'):
                model_files.append(uid)
    
    safe_print(f"Found {len(model_files)} existing model files")
    safe_print(f"Starting thumbnail download with 5 workers...\n")
    
    def download_thumbnail_for_uid(uid):
        safe_print(f"Processing UID: {uid}")
        model_info = get_model_info_from_sketchfab(uid)
        if model_info:
            download_thumbnail(model_info)
        return uid
    
    with concurrent.futures.ThreadPoolExecutor(max_workers=5) as executor:
        list(executor.map(download_thumbnail_for_uid, model_files))
    
    safe_print("\n" + "="*60)
    safe_print(f"Thumbnail download complete for {len(model_files)} models")
    safe_print("="*60)

if __name__ == "__main__":
    if not SKETCHFAB_API_TOKEN:
        print("Please set SKETCHFAB_API_TOKEN in .env file.")
    else:
        # Define 10 categories to crawl
        CATEGORIES = [
            "furniture", "character", "vehicle", "building", 
            "nature", "weapon", "food", "animal", "tool", "architecture"
        ]
        
        all_models = []
        safe_print(f"Searching {len(CATEGORIES)} categories with 10 popular models each...")
        
        for category in CATEGORIES:
            safe_print(f"\n{'='*60}")
            safe_print(f"Searching category: '{category}'...")
            try:
                results = search_sketchfab(category, count=10, sort_by='likeCount')
                safe_print(f"Found {len(results)} models in '{category}'")
                # Store category with each model for later mapping
                for result in results:
                    result['_search_category'] = category
                all_models.extend(results)
            except Exception as e:
                safe_print(f"[Error] Failed to search '{category}': {e}")
        
        safe_print(f"\n{'='*60}")
        safe_print(f"Total models found: {len(all_models)}")
        safe_print(f"Checking local models and uploading to backend...")
        safe_print(f"Starting parallel processing with 5 workers...")
        safe_print(f"{'='*60}\n")
        
        with concurrent.futures.ThreadPoolExecutor(max_workers=5) as executor:
            # Map the processing function to all models with their search category
            results = list(executor.map(lambda m: process_model(m, m.get('_search_category', 'art')), all_models))
        
        uploaded_count = sum(1 for r in results if r)
        safe_print(f"\n{'='*60}")
        safe_print(f"Upload complete: {uploaded_count}/{len(all_models)} models uploaded")
        safe_print(f"{'='*60}")
