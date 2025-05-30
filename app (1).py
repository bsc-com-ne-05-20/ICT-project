import os
from flask import Flask, request, jsonify
import requests

# ✅ Hardcoded Groq API Key (replace this with your actual key)
api_key = ""

app = Flask(__name__)

class Farmer_AgriGpt:
    def __init__(self, api_key):
        self.api_key = api_key
        self.base_url = "https://api.groq.com/openai/v1/chat/completions"
        self.model = "llama3-8b-8192"  # ✅ Use a model listed in your Groq dashboard

    def ask_soil_health(self, question):
        headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json"
        }
        payload = {
            "model": self.model,
            "messages": [
                {
                    "role": "system",
                    "content": (
                        "You are AgriGPT, an expert assistant in soil health, pH, nutrients, "
                        "crop rotation, fertiliser application, and best agriculture practices."
                    )
                },
                {"role": "user", "content": question}
            ],
            "max_tokens": 500,
            "temperature": 0.3
        }
        response = requests.post(self.base_url, headers=headers, json=payload)
        if response.status_code == 200:
            return response.json()["choices"][0]["message"]["content"].strip()
        else:
            return f"Error from Groq API: {response.status_code} - {response.text}"

agrigpt = Farmer_AgriGpt(api_key)

@app.route('/')
def home():
    return "✅ AgriGPT using Groq LLaMA 3 is running! POST to /ask with JSON: {'question':'your question'}"

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
    # ✅ Use port 5000 for local or the $PORT for Render deployment
    port = int(os.environ.get("PORT", 5000))
    app.run(host='0.0.0.0', port=port)
