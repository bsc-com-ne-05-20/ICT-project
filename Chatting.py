import random
import json
import pickle
import numpy as np
import nltk
from nltk.stem import WordNetLemmatizer
import tensorflow as tf

lemmatizer = WordNetLemmatizer()

# Load trained model
model = tf.keras.models.load_model('Chatbot For Soil Health System.h5')

# Load words and classes
words = pickle.load(open('words.pkl', 'rb'))
classes = pickle.load(open('classes.pkl', 'rb'))

# Load intents
with open('intents.json') as file:
    intents = json.load(file)

def clean_up_sentence(sentence):
    """Tokenize and lemmatize the input sentence"""
    sentence_words = nltk.word_tokenize(sentence)
    sentence_words = [lemmatizer.lemmatize(word.lower()) for word in sentence_words]
    return sentence_words

def bag_of_words(sentence):
    """Convert sentence into bag of words array"""
    sentence_words = clean_up_sentence(sentence)
    bag = [0] * len(words)
    for word in sentence_words:
        for i, w in enumerate(words):
            if w == word:
                bag[i] = 1
    return np.array(bag)

def predict_class(sentence):
    """Predict class of the given sentence"""
    bow = bag_of_words(sentence)
    res = model.predict(np.array([bow]))[0]
    ERROR_THRESHOLD = 0.25  # Adjust this threshold as needed
    results = [[i, r] for i, r in enumerate(res) if r > ERROR_THRESHOLD]

    results.sort(key=lambda x: x[1], reverse=True)
    return [{"intent": classes[r[0]], "probability": str(r[1])} for r in results]

def get_response(intents_list, intents_json):
    """Get response from intents.json based on predicted intent"""
    if intents_list:
        tag = intents_list[0]['intent']
        for i in intents_json['intents']:
            if i['tag'] == tag:
                return random.choice(i['responses'])
    return "I'm sorry, I don't understand."

def chat():
    print("Chatbot is running! Type 'quit' to exit.")
    while True:
        message = input("You: ")
        if message.lower() == "quit":
            break
        intents_list = predict_class(message)
        response = get_response(intents_list, intents)
        print(f"Bot: {response}")

if __name__ == "__main__":
    chat()
