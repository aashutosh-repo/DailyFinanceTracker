from app.rag.retriever import retriever


def retrieve_context(
        question: str
):

    documents = retriever.invoke(
        question
    )

    context = []

    for document in documents:

        context.append({

            "content":
                document.page_content,

            "source":
                document.metadata.get(
                    "source_file"
                )
        })

    return context

if __name__ == "__main__":

    results = retrieve_context(
        "What services does TCS provide?"
    )

    for result in results:

        print("\nSOURCE:")
        print(result["source"])

        print("\nCONTENT:")
        print(result["content"])