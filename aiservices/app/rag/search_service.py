from app.rag.vector_store import get_vector_store, is_vector_store_enabled


def _tokens(text: str) -> set[str]:
    return {
        token
        for token in "".join(
            character.lower() if character.isalnum() else " "
            for character in text
        ).split()
        if len(token) > 2
    }

def _rerank_results(
        question: str,
        results: list[tuple[object, float | None]],
        k: int = 4
): 
    question_tokens = _tokens(question)
    def rank(result: tuple[object, float | None]):
        document, score = result
        current_tokens = _tokens(document.page_content)
        overlap = len(question_tokens.intersection(current_tokens))
        distance = score if score is not None else 1.0
        return (-overlap, distance)
    return sorted(results, key=rank)[:k]

def search_knowledge(
        question: str,
        company: str | None = None,
        document_type: str | None = None,
    k: int = 10,
        search_type: str = "similarity"
):
    if not is_vector_store_enabled():
        return []
    vector_store = get_vector_store()

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

        return _rerank_results(question, results, k)


    # =====================================
    # MMR Search
    # =====================================

    elif search_type == "mmr":

        documents = vector_store.max_marginal_relevance_search(
            query=question,
            k=k,
            fetch_k=max(k * 3, 30),
            lambda_mult=0.5,
            filter=metadata_filter or None
        )

        # MMR does not return scores
        return [(document, None)
            for document in documents
        ]


    # =====================================
    # Hybrid search
    # =====================================

    elif search_type == "hybrid":

        similirity_results = vector_store.similarity_search_with_score(
            query=question,
            k=k,
            filter=metadata_filter or None
        )

        mmr_documents = vector_store.max_marginal_relevance_search(
            query=question,
            k=k,
            fetch_k=max(k * 3, 30),
            lambda_mult=0.5,
            filter=metadata_filter or None
        )

        merged_results = []
        seen = set()

        for document, score in similirity_results:
            key = (document.page_content, tuple(document.metadata.items()))
            if key not in seen:
                merged_results.append((document, score))
                seen.add(key)
                merged_results.append((document, score))

        for document in mmr_documents:
            key = (document.page_content, tuple(document.metadata.items()))
            if key not in seen:
                merged_results.append((document, None))
                seen.add(key)
                merged_results.append((document, None))

        return _rerank_results(question, merged_results, k)

    # =====================================
    # Unsupported Strategy
    # =====================================

    raise ValueError(
        f"Unsupported search type: {search_type}"
    )