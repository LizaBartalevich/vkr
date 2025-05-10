import cv2
import numpy as np
import pytesseract
from PIL import Image
import io

def preprocess_image(image_bytes):
    img = cv2.imdecode(np.frombuffer(image_bytes, np.uint8), cv2.IMREAD_COLOR)
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
    contrast = clahe.apply(gray)
    binary = cv2.adaptiveThreshold(contrast, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY_INV, 11, 2)
    return binary

def recognize_kanji(image_bytes):
    img = preprocess_image(image_bytes)
    _, img_encoded = cv2.imencode('.png', img)
    img_pil = Image.open(io.BytesIO(img_encoded.tobytes()))
    custom_config = r'--oem 3 --psm 7 -l jpn --dpi 600'
    text = pytesseract.image_to_string(img_pil, config=custom_config)
    kanji = "".join(char for char in text if '\u4e00' <= char <= '\u9fff')
    return list(kanji)