import requests

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
            "model": "gpt-3.5-turbo",  # or "gpt-3.5-turbo" if gpt-4 not available
            "messages": [
                {
                    "role": "system",
                    "content": (
                        "You are AgriGPT, an expert assistant that only answers questions related to soil health, "
                        "soil pH, fertility, nutrients, composting, soil care, crop rotation, and related soil management topics. "
                        "If the user asks anything unrelated, politely respond that you can only answer soil health questions."
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
            return f"Error: {response.status_code} - {response.text}"


if __name__ == "__main__":
    api_key = "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"
    agrigpt =Farmer_AgriGpt(api_key)

    while True:
        question = input("Ask about soil health: ")
        if question.lower() in ["exit", "quit"]:
            break
        answer = agrigpt.ask_soil_health(question)
        print(f"AgriGPT: {answer}")
