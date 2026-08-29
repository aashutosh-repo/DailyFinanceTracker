from langchain_core.tools import tool

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


@tool
def search_stock_knowledge(
        question: str,
        company: str | None = None
) -> dict:
    """
    Search the stock knowledge base for
    company and stock-analysis information.
    """

    print("\n========== RAG TOOL CALLED ==========")

    print(f"Question: {question}")

    print(f"Company Filter: {company}")


    # =====================================
    # 1. Choose Retrieval Strategy
    # =====================================

    strategy = choose_retrieval_strategy(
        question
    )

    print(
        f"Retrieval Strategy: "
        f"{strategy.value}"
    )


    # =====================================
    # 2. Retrieve Documents
    # =====================================

    raw_results = search_knowledge(
        question=question,
        company=company,
        search_type=strategy.value
    )

    print(
        f"Raw Results: "
        f"{len(raw_results)}"
    )


    # =====================================
    # 3. Print Raw Results
    # =====================================

    for index, (document, score) in enumerate(
            raw_results,
            start=1
    ):

        print(f"\nRaw Result {index}")

        print(
            f"Source: "
            f"{document.metadata.get('source_file')}"
        )

        print(
            f"Score: {score}"
        )

        print(
            f"Content: "
            f"{document.page_content[:200]}"
        )


    # =====================================
    # 4. Filter Relevant Results
    # =====================================

    relevant_results = filter_relevant_results(
        raw_results
    )

    print(
        f"\nRelevant Results: "
        f"{len(relevant_results)}"
    )

    # =====================================
    # Compress Relevant Context
    # =====================================

    compressed_results = compress_documents(
        relevant_results,
        max_chars=1200
    )


    print(
        f"Compressed Results: "
        f"{len(compressed_results)}"
    )


    # =====================================
    # No Useful Context
    # =====================================

    if not compressed_results:

        return {

            "search_type":
                strategy.value,

            "found":
                False,

            "message":
                "No sufficiently relevant information "
                "was found in the knowledge base.",

            "results":
                []
        }


    # =====================================
    # Return Compressed Context
    # =====================================

    return {

        "search_type":
            strategy.value,

        "found":
            True,

        "results":
            compressed_results
    }