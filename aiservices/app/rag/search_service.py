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
        metadata_filter["company"] = company.upper()

    if document_type:
        metadata_filter["document_type"] = document_type


    # =====================================
    # Similarity Search With Scores
    # =====================================

    if search_type == "similarity":

        results = vector_store.similarity_search_with_score(
            query=question,
            k=k,
            filter=metadata_filter or None
        )

        return results


    # =====================================
    # MMR Search
    # =====================================

    elif search_type == "mmr":

        documents = vector_store.max_marginal_relevance_search(
            query=question,
            k=k,
            fetch_k=10,
            lambda_mult=0.5,
            filter=metadata_filter or None
        )

        # MMR does not return scores
        return [
            (document, None)
            for document in documents
        ]


    # =====================================
    # Unsupported Strategy
    # =====================================

    raise ValueError(
        f"Unsupported search type: {search_type}"
    )