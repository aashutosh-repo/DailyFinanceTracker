from app.rag.search_service import search_knowledge


def print_results(question, results):

    print("\n")
    print("=" * 80)
    print(f"QUESTION: {question}")
    print("=" * 80)

    for index, (document, score) in enumerate(
            results,
            start=1
    ):

        print(f"\nRESULT {index}")

        print(
            f"Score: {score}"
        )

        print(
            f"Source: "
            f"{document.metadata.get('source_file')}"
        )

        print(
            f"Company: "
            f"{document.metadata.get('company')}"
        )

        print(
            f"Content:\n"
            f"{document.page_content[:300]}"
        )

        print("-" * 80)


def test_relevance():

    test_cases = [

        {
            "question":
                "What services does TCS provide?",

            "company":
                "TCS"
        },

        {
            "question":
                "Explain the business of TCS",

            "company":
                "TCS"
        },

        {
            "question":
                "How should stock performance be analyzed?",

            "company":
                None
        },

        {
            "question":
                "What is the capital of France?",

            "company":
                None
        },

        {
            "question":
                "Who won the football world cup?",

            "company":
                None
        }
    ]


    for test_case in test_cases:

        results = search_knowledge(

            question=test_case["question"],

            company=test_case["company"],

            search_type="similarity",

            k=4
        )


        print_results(

            test_case["question"],

            results
        )


if __name__ == "__main__":

    test_relevance()
