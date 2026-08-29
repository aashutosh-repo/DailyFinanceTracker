from pathlib import Path

from langchain_community.document_loaders import TextLoader

from langchain_text_splitters import (
    RecursiveCharacterTextSplitter
)

from app.rag.vector_store import vector_store


# ==========================================
# Configuration
# ==========================================

KNOWLEDGE_PATH = Path("knowledge")


# ==========================================
# Document Metadata Builder
# ==========================================

def enrich_metadata(document, file_path):

    document.metadata["source_file"] = file_path.name

    file_name = file_path.stem.lower()


    # --------------------------------------
    # Company Documents
    # --------------------------------------

    if file_name == "tcs_overview":

        document.metadata["document_type"] = (
            "company_overview"
        )

        document.metadata["company"] = "TCS"


    elif file_name == "infosys_overview":

        document.metadata["document_type"] = (
            "company_overview"
        )

        document.metadata["company"] = "INFOSYS"


    # --------------------------------------
    # Stock Analysis Knowledge
    # --------------------------------------

    elif file_name == "stock_analysis_guide":

        document.metadata["document_type"] = (
            "analysis_guide"
        )


    # --------------------------------------
    # Default
    # --------------------------------------

    else:

        document.metadata["document_type"] = (
            "general"
        )


# ==========================================
# Ingest Documents
# ==========================================

def ingest_documents():

    documents = []

    files = list(
        KNOWLEDGE_PATH.glob("*.txt")
    )


    print(
        f"\nFound {len(files)} documents\n"
    )


    # =====================================
    # Load Documents
    # =====================================

    for file_path in files:

        print(
            f"Loading: {file_path.name}"
        )


        loader = TextLoader(
            str(file_path),
            encoding="utf-8"
        )


        loaded_documents = loader.load()


        # =================================
        # Add Metadata
        # =================================

        for document in loaded_documents:

            enrich_metadata(
                document,
                file_path
            )


        documents.extend(
            loaded_documents
        )


    print(
        f"\nLoaded {len(documents)} documents"
    )


    # =====================================
    # Split Documents
    # =====================================

    splitter = RecursiveCharacterTextSplitter(

        chunk_size=500,

        chunk_overlap=100
    )


    chunks = splitter.split_documents(
        documents
    )


    print(
        f"Created {len(chunks)} chunks"
    )


    # =====================================
    # Store Vectors
    # =====================================

    vector_store.add_documents(
        documents=chunks
    )


    print(
        "\nDocuments indexed successfully"
    )


# ==========================================
# Run Ingestion
# ==========================================

if __name__ == "__main__":

    ingest_documents()
