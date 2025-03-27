import os
from flask import Flask, request, jsonify
import requests
from dotenv import load_dotenv
load_dotenv()

api_key =""
app = Flask(__name__)

class Farmer_AgriGpt:
    def __init__(self, api_key):
        self.api_key = api_key
        self.base_url = "https://api.openai.com/v1/chat/completions"

    def ask_soil_health(self, question):
        headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json"
        }
        payload = {
            "model": "gpt-3.5-turbo",
            "messages": [
                {
                    "role": "system",
                    "content": "You are AgriGPT, an expert assistant for soil health, pH, nutrients, crop rotation, fertiliser application, and adviceadvice on agriculture best practices"
                },
                {"role": "user", "content": question}
            ],
            "max_tokens": 500,
            "temperature": 0.3
        }
        response = requests.post(self.base_url, headers=headers, json=payload)
        return response.json()["choices"][0]["message"]["content"].strip() if response.status_code == 200 else f"Error: {response.text}"

agrigpt = Farmer_AgriGpt(os.environ.get("OPENAI_API_KEY"))

@app.route('/')
def home():
    return "AgriGPT API is running! Send POST requests to /ask with JSON {'question':'your_question'}"

@app.route('/ask', methods=['POST'])
def ask():
    try:
        data = request.get_json()
        if not data or 'question' not in data:
            return jsonify({"error": "Missing 'question' parameter"}), 400
        
        question = data['question']
        response = agrigpt.ask_soil_health(question)
        return jsonify({"response": response})
    
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=10000)  # Matching Render's port