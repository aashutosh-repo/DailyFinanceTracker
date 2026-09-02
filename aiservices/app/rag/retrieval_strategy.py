from app.models.retrieval_strategy import (
    RetrievalStrategy
)


def choose_retrieval_strategy(
        question: str
) -> RetrievalStrategy:

    question_lower = question.lower()


    # =====================================
    # Broad / Exploratory Questions
    # =====================================

    broad_keywords = [

        "explain",

        "analyze",

        "analyse",

        "overview",

        "compare",

        "why",

        "how does",

        "tell me about",

        "what are the",

        "different"
    ]


    for keyword in broad_keywords:

        if keyword in question_lower:

            return RetrievalStrategy.HYBRID


    # =====================================
    # Default
    # =====================================

    return RetrievalStrategy.SIMILARITY
