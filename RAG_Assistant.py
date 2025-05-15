import os
from dotenv import load_dotenv
from langchain_openai import ChatOpenAI, OpenAIEmbeddings
from langchain_community.document_loaders import TextLoader
from langchain.text_splitter import CharacterTextSplitter
from langchain_community.vectorstores import FAISS
from langchain.memory import ConversationBufferMemory
from langchain.chains import ConversationalRetrievalChain
from langchain_core.prompts import PromptTemplate
from pdfminer.high_level import extract_text as pdfminer_extract
from PyPDF2 import PdfReader

load_dotenv()
api_key = os.getenv("OPENAI_API_KEY")

#---- here we extract text from pdf files with the necessary local data and transfer the data to text files (.txt files are better for RAG) ----#
        
#pdf miner handles spaces well when extracting texts from documents(makes the extracted text look better).
def extract_with_pdfminer(pdf_path):
    """Extract text using pdfminer.six (primary method)"""
    try:
        return pdfminer_extract(pdf_path)
    except Exception as e:
        print(f" PDF miner extraction failed!!!: {str(e)}")
        return None
        
#incase pdfminer fails. pydf2 is more robust and complex than pdfminer, but its slow and does not handle spaces well.
def extract_with_pypdf2(pdf_path):
    """Fallback text extraction with PYDF2"""
    text=""
    try:
        with open(pdf_path, 'rb') as file:
            reader = PdfReader(file)
            for page in reader.pages:
                page_text = page.extract_text()
                if page_text:
                    text += page_text + "\n"
        return text
    
    except Exception as e:
        print(f" PDF Extraction has failed!!!: {str(e)}")
        return None
    
#cleaning the extract by fixing the spaces
def clean_extracted_text(text):
    """Clean extracted text by fixing spaces"""
    if not text:
        return ""
    
    lines = text.split('\n')
    cleaned_lines =[]
    for line in lines:
        words = line.split(' ')
        cleaned_words = []
        for word in words:
            if len(word) > 1 and sum(1 for c in word if c == ' ') > len(word)/2:
                cleaned_words.append(word.replace(' ',''))
            else:
                cleaned_words.append(word)
        cleaned_lines.append(' '.join(cleaned_words))
    return '\n'.join(cleaned_lines)

def process_pdf_file(pdf_path):
    """process a single pdf file with fallback methods"""
    #here we start with pdfminer first
    text = extract_with_pdfminer(pdf_path)

    #then tries pydf2, if pdfminer fails
    if not text:
        text = extract_with_pypdf2(pdf_path)

    #if we extract something with either the two libraries, we clean the text
    if text:
        text = clean_extracted_text(text)
        return text
    else:
        print(f" All methods for extraction have failed!!!")
        return None
    
def process_pdf_folder(input_folder,output_folder):
    """Process a folder of pdf files"""
    if not os.path.exists(input_folder):
        print(f"Error: folder '{input_folder}' does not exist")
        return
    os.makedirs(output_folder, exist_ok=True)

    pdf_files = [f for f in os.listdir(input_folder) if f.lower().endswith('.pdf')]
    total_files = len(pdf_files)

    if total_files == 0:
        print(f"No pdf files found in folder: {input_folder}")
        return
    else:
        print(f"Processing {total_files} pdf files in folder: {input_folder}")
        success_count = 0
        for i, filename in enumerate(pdf_files, 1):
            pdf_path = os.path.join(input_folder,filename)
            text = process_pdf_file(pdf_path)

            if text:
                #saving the extracted text to a new .txt file of the same name as its pdf
                output_path = os.path.join(output_folder, f"{os.path.splitext(filename)[0]}.txt")
                try:
                    with open(output_path, 'w', encoding='utf-8') as text_file:
                        text_file.write(text)
                    print(f" Saved to {output_folder}!")
                    success_count += 1
                except Exception as e:
                    print(f" Failed saving file: {str(e)}")
        print( f" Successfully processed {success_count}/{total_files} pdf files!")

if __name__ == "__main__":
    input_folder ='data'
    output_folder = 'extracted_text'
    process_pdf_folder(input_folder, output_folder)

#------------------- load documents/.txt files for more local agricultural context ---------------------------#
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

#--------------------------------- creating the vectorstore -------------------------------------------------#

embeddings = OpenAIEmbeddings(
    model="text-embedding-3-small",
    chunk_size=500
)
vectorstore = FAISS.from_documents(split_docs, embedding=embeddings)
retriever = vectorstore.as_retriever(search_kwargs={"k": 3})

#-------------------------------- setting up or initializing the large language model ------------------------#

llm = ChatOpenAI(model_name="gpt-4", temperature=0.2)

#---------------- the prompt template to ensure the chatbot or llm is not hallucinating ----------------------#
 
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

#---------------------- here is a chain of the conversation that ensures the bot has memory of the current conversation -----------------------#

memory = ConversationBufferMemory(memory_key="chat_history", return_messages=True)
conversation_chain = ConversationalRetrievalChain.from_llm(
    llm=llm,
    retriever=retriever,
    memory=memory,
    combine_docs_chain_kwargs={"prompt": CUSTOM_QA_PROMPT},
    verbose=False
)

#----------------------------------------------- here is the interactive loop ---------------------------------------------------#

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

#-------------- conversation chain to load different chat ids to keep track of different conversations in our mobile app ---------------------#
def create_conversation_chain():
    memory = ConversationBufferMemory(memory_key="chat_history", return_messages=True)
    return ConversationalRetrievalChain.from_llm(
        llm=llm,
        retriever=retriever,
        memory=memory,
        combine_docs_chain_kwargs={"prompt": CUSTOM_QA_PROMPT},
        verbose=False
    )
