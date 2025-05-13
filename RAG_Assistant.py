import os
from dotenv import load_dotenv
from langchain_openai import ChatOpenAI, OpenAIEmbeddings
from langchain_community.document_loaders import TextLoader
from langchain.text_splitter import CharacterTextSplitter
from langchain_community.vectorstores import FAISS
from langchain.memory import ConversationBufferMemory
from langchain.chains import ConversationalRetrievalChain
from langchain_core.prompts import PromptTemplate

load_dotenv()
api_key = os.getenv("OPENAI_API_KEY")

#load documents for more local agricultural context
data_dir = './extracted_text'
text_files = [f for f in os.listdir(data_dir) if f.endswith('.txt')]

documents = []
for file in text_files:
    file_path = os.path.join(data_dir, file)
    loader = TextLoader(file_path=file_path, encoding="utf-8")
    documents.extend(loader.load())

text_splitter = CharacterTextSplitter(
    chunk_size=800,
    chunk_overlap=200,
    separator="\n"
)
split_docs = text_splitter.split_documents(documents)

#creating the vectorstore
embeddings = OpenAIEmbeddings(
    model="text-embedding-3-small",
    chunk_size=500
)
vectorstore = FAISS.from_documents(split_docs, embedding=embeddings)
retriever = vectorstore.as_retriever(search_kwargs={"k": 3})

#setting up or intitializing te large language model
llm = ChatOpenAI(model_name="gpt-4", temperature=0.2)

#the prompt template to ensure the chatbot or llm is not hallucinating 
custom_template = """You are a helpful agricultural assistant for Malawians, with expertise in soil health analysis and recommendations.

Key rules:
- respond to greetings warmly
- remember our conversation history 
- reference malawian contexts when relevant and possible
- use simple language suitable for farmers
- provide practical, actionable advice
- For greetings, be warm but varied
- admit knowledge gaps honestly

Chat history:
{chat_history}

Context:
{context}

Question: {question}
Answer:"""
CUSTOM_QA_PROMPT = PromptTemplate.from_template(custom_template)

#here is a chain of the conversation that ensures the bot has memory of the conversation
memory = ConversationBufferMemory(memory_key="chat_history", return_messages=True)
conversation_chain = ConversationalRetrievalChain.from_llm(
    llm=llm,
    retriever=retriever,
    memory=memory,
    combine_docs_chain_kwargs={"prompt": CUSTOM_QA_PROMPT},
    verbose=False
)

#here is the interactive loop
print("I am your Agricultural assistant, How can I help you today?")
while True:
    try:
        query = input("\nYou: ").strip()
        
        if query.lower() in ['exit', 'quit', 'bye']:
            print("\nAssistant: Goodbye! Have a great day!")
            break

        result = conversation_chain.invoke({"question": query})
        
        response = result["answer"].strip()
        if not response or response.lower() == "i don't know":
            response = "please be more specific"
        
        print(f"\nAssistant: {response}")

    except KeyboardInterrupt:
        print("\nAssistant: session ended abruptly, Goodbye!")
        break
    except Exception as e:
        print(f"\nAssistant: Sorry I encountered an error, please try again. ({str(e)})")