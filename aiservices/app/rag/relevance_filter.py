from typing import Any


# Initial threshold.
# We will tune this after observing real scores.

MAX_DISTANCE = 0.45


def filter_relevant_results(
        results: list[tuple[Any, float | None]]
):

    filtered_results = []


    for document, score in results:

        # ---------------------------------
        # MMR has no score
        # ---------------------------------

        if score is None:

            filtered_results.append(
                (document, score)
            )

            continue


        # ---------------------------------
        # Similarity Search
        # ---------------------------------

        if score <= MAX_DISTANCE:

            filtered_results.append(
                (document, score)
            )


    return filtered_results
