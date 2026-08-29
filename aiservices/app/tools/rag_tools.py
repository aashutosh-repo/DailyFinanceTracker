from langchain_core.tools import tool

from app.rag.search_service import (
    search_knowledge
)

from app.rag.retrieval_strategy import (
    choose_retrieval_strategy
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

    print(
        f"Question: {question}"
    )

    print(
        f"Company Filter: {company}"
    )


    # =====================================
    # Choose Retrieval Strategy
    # =====================================

    strategy = choose_retrieval_strategy(
        question
    )


    print(
        f"Retrieval Strategy: {strategy.value}"
    )


    # =====================================
    # Execute Retrieval
    # =====================================

    documents = search_knowledge(

        question=question,

        company=company,

        search_type=strategy.value
    )


    print(
        f"Retrieved Documents: "
        f"{len(documents)}"
    )


    # =====================================
    # Format Results
    # =====================================

    results = []


    for index, document in enumerate(
            documents,
            start=1
    ):

        source = document.metadata.get(
            "source_file",
            "unknown"
        )


        results.append({

            "rank":
                index,

            "content":
                document.page_content,

            "source":
                source,

            "metadata":
                document.metadata
        })


        print(
            f"\nResult {index}"
        )

        print(
            f"Source: {source}"
        )

        print(
            f"Content: "
            f"{document.page_content[:250]}"
        )


    print(
        "\n===================================="
    )


    return {

        "search_type":
            strategy.value,

        "results":
            results
    }
