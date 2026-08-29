from typing import Any


def compress_documents(
        results: list[tuple[Any, float | None]],
        max_chars: int = 1200
):
    """
    Compress retrieved documents by limiting the
    amount of context returned to the final LLM.

    This is a lightweight compression step.
    It does not make another LLM call.
    """

    compressed_results = []

    remaining_chars = max_chars


    for document, score in results:

        if remaining_chars <= 0:
            break


        content = document.page_content.strip()


        # ---------------------------------
        # Keep only the available context
        # ---------------------------------

        content = content[:remaining_chars]


        if not content:
            continue


        compressed_results.append({

            "content": content,

            "source":
                document.metadata.get(
                    "source_file",
                    "unknown"
                ),

            "score": score,

            "metadata":
                document.metadata
        })


        remaining_chars -= len(content)


    return compressed_results