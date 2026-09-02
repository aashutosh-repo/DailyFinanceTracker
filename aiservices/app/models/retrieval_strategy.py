from enum import Enum


class RetrievalStrategy(str, Enum):

    SIMILARITY = "similarity"

    MMR = "mmr"
    HYBRID = "hybrid"