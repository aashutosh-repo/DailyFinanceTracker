from langchain_core.tools import tool

try:
    from app.rag.search_service import (
        search_knowledge
    )
    from app.rag.context_compressor import (
        compress_documents
    )
    from app.rag.retrieval_strategy import (
        choose_retrieval_strategy
    )
    from app.rag.relevance_filter import (
        filter_relevant_results
    )
except ModuleNotFoundError:  # pragma: no cover - optional RAG stack may be absent
    search_knowledge = None
    compress_documents = None
    choose_retrieval_strategy = None
    filter_relevant_results = None


@tool
def search_stock_knowledge(
        question: str,
        company: str | None = None
) -> dict:
    """
    Search the stock knowledge base for
    company and stock-analysis information.
    """
    if search_knowledge is None or choose_retrieval_strategy is None or compress_documents is None or filter_relevant_results is None:
        return {
            "ok": False,
            "error": {
                "type": "rag_unavailable",
                "message": "RAG dependencies are not installed in the current Python environment.",
            },
        }

    print("\n========== RAG TOOL CALLED ==========")
    print(f"Question: {question}")
    print(f"Company Filter: {company}")

    strategy = choose_retrieval_strategy(question)
    print(f"Retrieval Strategy: {strategy.value}")

    raw_results = search_knowledge(
        question=question,
        company=company,
        k=10,
        search_type=strategy.value
    )

    print(f"Raw Results: {len(raw_results)}")

    for index, (document, score) in enumerate(raw_results, start=1):
        print(f"\nRaw Result {index}")
        print(f"Source: {document.metadata.get('source_file')}")
        print(f"Score: {score}")
        print(f"Content: {document.page_content[:200]}")

    relevant_results = filter_relevant_results(raw_results)
    print(f"\nRelevant Results: {len(relevant_results)}")

    compressed_results = compress_documents(relevant_results, max_chars=4800)
    print(f"Compressed Results: {len(compressed_results)}")

    if not compressed_results:
        return {
            "search_type": strategy.value,
            "found": False,
            "message": "No sufficiently relevant information was found in the knowledge base.",
            "results": [],
        }

    return {
        "search_type": strategy.value,
        "found": True,
        "results": compressed_results,
    }