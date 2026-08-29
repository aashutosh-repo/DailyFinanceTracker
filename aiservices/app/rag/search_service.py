from app.rag.vector_store import vector_store


def search_knowledge(
        question: str,
        company: str | None = None,
        document_type: str | None = None,
        k: int = 4,
        search_type: str = "similarity"
):

    # =====================================
    # Build Metadata Filter
    # =====================================

    metadata_filter = {}

    if company:

        metadata_filter["company"] = (
            company.upper()
        )


    if document_type:

        metadata_filter["document_type"] = (
            document_type
        )


    # =====================================
    # Build Search Configuration
    # =====================================

    search_kwargs = {
        "k": k
    }


    if metadata_filter:

        search_kwargs["filter"] = (
            metadata_filter
        )


    # =====================================
    # MMR Configuration
    # =====================================

    if search_type == "mmr":

        search_kwargs["fetch_k"] = 10

        search_kwargs["lambda_mult"] = 0.5


    # =====================================
    # Create Retriever
    # =====================================

    retriever = vector_store.as_retriever(

        search_type=search_type,

        search_kwargs=search_kwargs
    )


    # =====================================
    # Execute Search
    # =====================================

    documents = retriever.invoke(
        question
    )


    return documents
