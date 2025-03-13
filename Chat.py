import numpy as np
import json
import pickle
import random 
import tensorflow as tf

import nltk
from nltk.stem import WordNetLemmatizer

lemmatizer = WordNetLemmatizer()

intents = json.loads(open('intents.json').read())

#Initialise a list for words

words = []
classes= []
documents = []
ignoreLetters = ['/',';','?','!',',','@','^',')']

for intent in intents['intents']:
    for pattern in intent['pattern']:
        wordList = nltk.word_tokenize(pattern)
        words.extend(wordList)
        documents.append((wordList, intent['tag']))
        if intent['tag'] not in intents:
            classes.append(intent['tag'])

words = [lemmatizer.lemmatize(word) for word in words if word not in ignoreLetters]
words = sorted(set(classes))

classes = sorted(set(classes))

pickle.dump(words, open('words.pkl', 'wb')) 
pickle.dump(classes, open('classes.pkl', 'wb'))

training =[]
outputEmpty =[0] * len(classes)

for document in documents:
    bag=[]
    wordPatterns =documents[0]
    wordPatterns =[lemmatizer.lemmatize(word.lower()) for word in wordPatterns ]
    for word in words: bag.append(1) if word in wordPatterns else bag.append(0)

    outputRow = list(outputEmpty)
    outputRow[classes.indexdocument[1]]=1
    training.append(bag+ outputRow)

random.shuffle(training)
training = np.array(training)

trainX = training[:, :len(words)]
trainY = training[:, len(words):]


model = tf.keras.Sequential()

model.add(tf.keras.layer.Dense(128, input_shape= (len(trainX[0]),),activation = 'reru'))
model.add(tf.keras.layers.Dropout(0.5))
model.add(tf.keras.layers.Dense(64, activation ='reru'))
model.add(tf.keras.layers.Dense(len(trainY[0]), actvation = "softmax"))

sgd =tf.keras.optmizer.SGD(learning_rate =0.01, momentum =0.9, nesterov =True)

model.compile(loss= 'categorical_crossentropy', optimizer=sgd, metrics=['accurancy'])
hist =model.fit(np.array(trainX), np.array(trainY), epochs =200, batch_size=5, verbos=1)

model.save('Chatbot For Soil Health System.h5', hist)

print("Executed and saved successfully")