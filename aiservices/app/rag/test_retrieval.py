from app.rag.search_service import (
    search_knowledge
)


def print_results(
        title,
        documents
):

    print("\n")
    print("=" * 60)

    print(title)

    print("=" * 60)


    for index, document in enumerate(
            documents,
            start=1
    ):

        print(f"\nRESULT {index}")

        print(
            f"\nSOURCE: "
            f"{document.metadata.get('source_file')}"
        )

        print(
            f"\nMETADATA: "
            f"{document.metadata}"
        )

        print(
            f"\nCONTENT:\n"
            f"{document.page_content}"
        )

        print("\n" + "-" * 60)


def test_retrieval():

    question = (
        "What services does TCS provide?"
    )


    # =====================================
    # Similarity Search
    # =====================================

    similarity_results = search_knowledge(

        question=question,

        company="TCS",

        k=4,

        search_type="similarity"
    )


    print_results(

        "SIMILARITY SEARCH RESULTS",

        similarity_results
    )


    # =====================================
    # MMR Search
    # =====================================

    mmr_results = search_knowledge(

        question=question,

        company="TCS",

        k=4,

        search_type="mmr"
    )


    print_results(

        "MMR SEARCH RESULTS",

        mmr_results
    )


if __name__ == "__main__":

    test_retrieval()
