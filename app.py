import os
from flask import Flask, request, jsonify
import requests

app = Flask(__name__)

class FarmerAgriGPT:
    SYSTEM_PROMPT = "You are AgriGPT, an agriculture assistant. Provide expert advice on farming topics."

    def __init__(self, api_key):
        self.api_key = api_key
        self.base_url = "https://api.openai.com/v1/chat/completions"

    def ask(self, messages):
        headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json"
        }
        payload = {
            "model": "gpt-3.5-turbo",
            "messages": [{"role": "system", "content": self.SYSTEM_PROMPT}] + messages,
            "temperature": 0.3
        }
        response = requests.post(self.base_url, headers=headers, json=payload)
        return response.json()["choices"][0]["message"]["content"].strip()

agrigpt = FarmerAgriGPT(os.environ["OPENAI_API_KEY"])  # Key from HF Secrets

conversations = {}  # Stores chat history

@app.route('/')
def home():
    return "Send POST requests to /ask with {'question': '...', 'session_id': 'optional'}"

@app.route('/ask', methods=['POST'])
def ask():
    try:
        data = request.get_json()
        question = data.get('question')
        session_id = data.get('session_id', 'default')

        if not question:
            return jsonify({"error": "Missing 'question'"}), 400

        if session_id not in conversations:
            conversations[session_id] = []

        conversations[session_id].append({"role": "user", "content": question})
        response = agrigpt.ask(conversations[session_id])
        conversations[session_id].append({"role": "assistant", "content": response})

        return jsonify({"response": response, "session_id": session_id})
    
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=int(os.getenv("PORT", 8080)))