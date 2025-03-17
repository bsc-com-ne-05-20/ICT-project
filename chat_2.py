"""import random 

import json
import os
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'
import pickle
import numpy as np
import nltk 
from nltk.stem import WordNetLemmatizer
import tensorflow
from keras.models import load_model 

lemmatizer = WordNetLemmatizer()

intents = json.loads(open('intents.json').read())
words = pickle.load(open('words.pkl', 'rb'))
classes =  pickle.load(open('classes.pkl', 'rb'))

model = load_model("chatbot For Soil Health System.h5")

def clean_up_sentence(sentence):
    sentence_words = nltk.word_tokenize(sentence)
    sentence_words = [lemmatizer.lemmatize(word) for word in sentence_words]
    return sentence_words


def bag_of_words(sentence):
    sentence_words = clean_up_sentence(sentence)
    bag =[0]*len(words)
    for w in sentence_words:
        for i, word in enumerate(words):
            if word == w:
                bag[i]=1

    return np.array(bag)        

def predict_class(sentence):
    bow = bag_of_words(sentence)
    res = model.predict(np.array([bow]))[0]


    Error_THRESHOLD = 0.25
    results =[[i,r] for i, r in enumerate(res ) if r>Error_THRESHOLD]
    results.sort(key =lambda x:x[1], reverse = True)
    return_list = []
    for r in results:
        return_list.append({'intent': classes[r[0]], 'probability':str(r[1])})
    return return_list

def get_response(intents_list, intents_json):
    list_of_intents =intents_json['intents']
    if not intents_list:
        return "Sorry, I dont get what you are saying"
    tag = intents_list[0]['intent']
    for i in list_of_intents:
        if i['tag']==tag:
             
            return random.choice(i['responses'])
    return "I dont know how to respond to that"        

while True:
    message = input("Researcher: ")
    ints = predict_class(message)
    res = get_response(ints, intents)"""

import random
import json
import os
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'
import pickle
import numpy as np
import nltk 
from nltk.stem import WordNetLemmatizer
import tensorflow
from keras.models import load_model 

lemmatizer = WordNetLemmatizer()

intents = json.loads(open('intents.json').read())
words = pickle.load(open('words.pkl', 'rb'))
classes = pickle.load(open('classes.pkl', 'rb'))
model = load_model("chatbot For Soil Health System.h5")

def clean_up_sentence(sentence):
    sentence_words = nltk.word_tokenize(sentence)
    sentence_words = [lemmatizer.lemmatize(word) for word in sentence_words]
    return sentence_words

def bag_of_words(sentence):
    sentence_words = clean_up_sentence(sentence)
    bag = [0] * len(words)
    for w in sentence_words:
        for i, word in enumerate(words):
            if word == w:
                bag[i] = 1
    return np.array(bag)  

def predict_class(sentence):
    bow = bag_of_words(sentence)
    res = model.predict(np.array([bow]))[0]

    Error_THRESHOLD = 0.5
    results = [[i, r] for i, r in enumerate(res) if r > Error_THRESHOLD]
    results.sort(key=lambda x: x[1], reverse=True)
    return_list = []
    for r in results:
        return_list.append({'intent': classes[r[0]], 'probability': str(r[1])})
    return return_list

def get_response(intents_list, intents_json):
    list_of_intents = intents_json['intents']
    
    if not intents_list:
        return "Sorry, I don't understand what you're saying."
    
    tag = intents_list[0]['intent']
    
    for i in list_of_intents:
        if i['tag'] == tag:
            return random.choice(i['responses'])
    
    return "I don't know how to respond to that."

print("Here for agri counselling, feel free to ask")
while True:
    try:
        message = input("")
        if message.lower() == "exit":
            break
        
        ints = predict_class(message)
        res = get_response(ints, intents)
        print(res)
        
    except KeyboardInterrupt:
        print("\nExiting...")
        break
