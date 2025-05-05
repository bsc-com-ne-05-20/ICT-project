from sentence_transformers import SentenceTransformer
import faiss
import openai
import os
import pdfplumber
import docx
from pathlib import Path

# Initialize OpenAI API
openai.api_key = os.getenv('OPENAI_API_KEY')

# ----------------------------------------- DATA PREPARATION AND INDEXING ---------------------------------------#

# extracting text from different file types
def extract_text_from_pdf(file_path):
    with pdfplumber.open(file_path) as pdf:
        return "\n".join(page.extract_text() for page in pdf.pages if page.extract_text())

def extract_text_from_docx(file_path):
    doc = docx.Document(file_path)
    return "\n".join([para.text for para in doc.paragraphs])

# scan a folder and extract text from all files provided
def load_documents_from_directory(directory):
    all_texts = []
    folder = Path(directory)

    for file_path in folder.glob("*"):
        if file_path.suffix.lower() == ".pdf":
            print(f"Extracting from PDF: {file_path.name}")
            text = extract_text_from_pdf(file_path)
            all_texts.append(text)
        elif file_path.suffix.lower() == ".docx":
            print(f"Extracting from DOCX: {file_path.name}")
            text = extract_text_from_docx(file_path)
            all_texts.append(text)
    
    return all_texts

#------------------------ Load and preprocess documents ----------------------------------#

# replacing static list with actual files from a folder
documents_folder = "./data"
documents = load_documents_from_directory(documents_folder)

# here we initialize the embedding model
model = SentenceTransformer('all-MiniLM-L6-v2')
embeddings = model.encode(documents, convert_to_tensor=False).astype('float32')

# initialize and populate the index
index = faiss.IndexFlatL2(embeddings.shape[1])
index.add(embeddings)
faiss.write_index(index, 'faiss_index.bin')

# ------------------------------------------ VECTOR DB SETUP ---------------------------------------------------#
# creating a vector database object
class SimpleVectorDB:
    def __init__(self, index_path, docs):
        self.index = faiss.read_index(index_path)
        self.docs = docs

    def as_retriever(self):
        def retriever(query, k=3):
            query_embedding = model.encode([query]).astype('float32')
            distances, indices = self.index.search(query_embedding, k)
            return [self.docs[i] for i in indices[0]]
        return retriever

db = SimpleVectorDB(index_path='faiss_index.bin', docs=documents)

# ------------------------------------------- PROMPT TEMPLATE --------------------------------------------------#

# here we define the LLM chain with an example prompt
prompt_template = """
Use the following pieces of context to answer the question at the end. \n
If you don’t know the answer, just say that you don’t know, \n
don’t try to make up an answer. \n
Context:
{context}
Question:
{question}
"""
from openai import OpenAI

client = OpenAI(
    api_key=os.getenv('OPENAI_API_KEY')
)  # Initialize OpenAI client

def llm_chain(messages):
    response = client.chat.completions.create(
        model="gpt-3.5-turbo",
        messages=messages,
        temperature=0.7,
        max_tokens=300
    )
    return response.choices[0].message.content.strip()

# ------------------------------------------ CHAT LOOP ---------------------------------------------------------#

# deefining the question and answering function
def ask():
    print("Ai assistant is ready! Type 'exit' to quit.\n")

    messages = [
        {"role": "system", "content": "You are a helpful agricultural extension worker for farmers in Malawi. You are a soil expert and you assist with crop selection, soil health, and best practices or sustainable agricultural practices."}
    ]

    retriever = db.as_retriever()  # initialize the retriever

    while True:
        user_input = input("You: ")
        if user_input.lower() in ["exit", "quit"]:
            print("Assistant: Goodbye and happy farming! 🌱")
            break

        # Retrieve relevant documents
        relevant_docs = retriever(user_input)
        context = "\n".join(relevant_docs)

        # Create the prompt with context
        prompt = prompt_template.format(context=context, question=user_input)

        # Add user message and model response
        messages.append({"role": "user", "content": prompt})
        reply = llm_chain(messages)
        print(f"Assistant: {reply}\n")
        messages.append({"role": "assistant", "content": reply})

# Example usage
# Run the assistant
if __name__ == "__main__":
    ask()
